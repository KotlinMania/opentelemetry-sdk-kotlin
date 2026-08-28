// port-lint: source trace/span_processor_with_async_runtime.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.RuntimeChannel
import io.github.kotlinmania.opentelemetrysdk.Sender
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

internal sealed class AsyncBatchSpanMessage {
    data class ExportSpan(
        val span: SpanData,
    ) : AsyncBatchSpanMessage()

    data class Flush(
        val response: CompletableDeferred<OTelSdkResult>?,
    ) : AsyncBatchSpanMessage()

    data class Shutdown(
        val response: CompletableDeferred<OTelSdkResult>,
    ) : AsyncBatchSpanMessage()

    data class SetResource(
        val resource: Resource,
    ) : AsyncBatchSpanMessage()
}

/**
 * A [SpanProcessor] that asynchronously buffers finished spans and reports
 * them at a preconfigured interval using a provided [RuntimeChannel].
 */
@OptIn(ExperimentalAtomicApi::class)
public class AsyncBatchSpanProcessor<R : RuntimeChannel>(
    private val exporter: SpanExporter,
    public val config: BatchConfig,
    public val runtime: R,
) : SpanProcessor {
    private val sender: Sender<AsyncBatchSpanMessage>
    private val droppedSpansCount = AtomicInt(0)

    init {
        val (tx, rx) = runtime.batchMessageChannel<AsyncBatchSpanMessage>(config.maxQueueSize)
        this.sender = tx

        runtime.spawn {
            val buffer = mutableListOf<SpanData>()
            try {
                coroutineScope {
                    val tickerJob =
                        launch {
                            while (isActive) {
                                runtime.delay(config.scheduledDelay)
                                tx.trySend(AsyncBatchSpanMessage.Flush(null))
                            }
                        }

                    try {
                        rx.asFlow().collect { message ->
                            when (message) {
                                is AsyncBatchSpanMessage.ExportSpan -> {
                                    buffer.add(message.span)
                                    if (buffer.size >= config.maxExportBatchSize) {
                                        val batch = buffer.toList()
                                        buffer.clear()
                                        exporter.export(batch)
                                    }
                                }
                                is AsyncBatchSpanMessage.Flush -> {
                                    val result =
                                        if (buffer.isNotEmpty()) {
                                            val batch = buffer.toList()
                                            buffer.clear()
                                            exporter.export(batch)
                                        } else {
                                            Result.success(Unit)
                                        }
                                    message.response?.complete(result)
                                }
                                is AsyncBatchSpanMessage.Shutdown -> {
                                    tickerJob.cancel()
                                    val result =
                                        if (buffer.isNotEmpty()) {
                                            val batch = buffer.toList()
                                            buffer.clear()
                                            exporter.export(batch)
                                        } else {
                                            Result.success(Unit)
                                        }
                                    exporter.shutdown()
                                    message.response.complete(result)
                                    throw CancellationException("Shutdown")
                                }
                                is AsyncBatchSpanMessage.SetResource -> {
                                    exporter.setResource(message.resource)
                                }
                            }
                        }
                    } catch (_: CancellationException) {
                        // Normal shutdown
                    } finally {
                        tickerJob.cancel()
                    }
                }
            } catch (_: Exception) {
                // Task cancelled or finished
            }
        }
    }

    override fun onStart(span: Span, parentContext: SpanContext?) {
        // Ignored
    }

    override fun onEnd(span: SpanData) {
        if (!span.spanContext.traceFlags.isSampled) {
            return
        }
        val res = sender.trySend(AsyncBatchSpanMessage.ExportSpan(span))
        if (res.isFailure) {
            droppedSpansCount.addAndFetch(1)
        }
    }

    override fun forceFlush(): OTelSdkResult {
        val deferred = CompletableDeferred<OTelSdkResult>()
        val sendRes = sender.trySend(AsyncBatchSpanMessage.Flush(deferred))
        return if (sendRes.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to send flush message"))
        }
    }

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        val deferred = CompletableDeferred<OTelSdkResult>()
        val sendRes = sender.trySend(AsyncBatchSpanMessage.Shutdown(deferred))
        return if (sendRes.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to send shutdown message"))
        }
    }

    override fun setResource(resource: Resource) {
        sender.trySend(AsyncBatchSpanMessage.SetResource(resource))
    }

    public companion object {
        public fun <R : RuntimeChannel> builder(
            exporter: SpanExporter,
            runtime: R,
        ): AsyncBatchSpanProcessorBuilder<R> = AsyncBatchSpanProcessorBuilder(exporter, runtime)
    }
}

public class AsyncBatchSpanProcessorBuilder<R : RuntimeChannel>(
    private val exporter: SpanExporter,
    private val runtime: R,
) {
    private var config: BatchConfig = BatchConfig()

    public fun withBatchConfig(config: BatchConfig): AsyncBatchSpanProcessorBuilder<R> {
        this.config = config
        return this
    }

    public fun build(): AsyncBatchSpanProcessor<R> =
        AsyncBatchSpanProcessor(exporter, config, runtime)
}
