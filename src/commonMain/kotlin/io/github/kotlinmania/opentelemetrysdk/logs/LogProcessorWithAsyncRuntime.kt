// port-lint: source logs/log_processor_with_async_runtime.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
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

internal sealed class AsyncBatchLogMessage {
    data class ExportLog(
        val record: SdkLogRecord,
        val instrumentation: InstrumentationScope,
    ) : AsyncBatchLogMessage()

    data class Flush(
        val response: CompletableDeferred<OTelSdkResult>?,
    ) : AsyncBatchLogMessage()

    data class Shutdown(
        val response: CompletableDeferred<OTelSdkResult>,
    ) : AsyncBatchLogMessage()

    data class SetResource(
        val resource: Resource,
    ) : AsyncBatchLogMessage()
}

/**
 * A [LogProcessor] that asynchronously buffers log records and reports
 * them at a preconfigured interval using a provided [RuntimeChannel].
 */
@OptIn(ExperimentalAtomicApi::class)
public class AsyncBatchLogProcessor<R : RuntimeChannel>(
    private val exporter: LogExporter,
    public val config: BatchConfig,
    public val runtime: R,
) : LogProcessor {
    private val sender: Sender<AsyncBatchLogMessage>
    private val droppedLogsCount = AtomicInt(0)

    init {
        val (tx, rx) = runtime.batchMessageChannel<AsyncBatchLogMessage>(config.maxQueueSize)
        this.sender = tx

        runtime.spawn {
            val buffer = mutableListOf<OwnedLogData>()
            try {
                coroutineScope {
                    val tickerJob =
                        launch {
                            while (isActive) {
                                runtime.delay(config.scheduledDelay)
                                tx.trySend(AsyncBatchLogMessage.Flush(null))
                            }
                        }

                    try {
                        rx.asFlow().collect { message ->
                            when (message) {
                                is AsyncBatchLogMessage.ExportLog -> {
                                    buffer.add(OwnedLogData(record = message.record, instrumentation = message.instrumentation))
                                    if (buffer.size >= config.maxExportBatchSize) {
                                        val batch = buffer.toList()
                                        buffer.clear()
                                        exporter.export(LogBatch.new(batch))
                                    }
                                }
                                is AsyncBatchLogMessage.Flush -> {
                                    val result =
                                        if (buffer.isNotEmpty()) {
                                            val batch = buffer.toList()
                                            buffer.clear()
                                            exporter.export(LogBatch.new(batch))
                                        } else {
                                            Result.success(Unit)
                                        }
                                    message.response?.complete(result)
                                }
                                is AsyncBatchLogMessage.Shutdown -> {
                                    tickerJob.cancel()
                                    val result =
                                        if (buffer.isNotEmpty()) {
                                            val batch = buffer.toList()
                                            buffer.clear()
                                            exporter.export(LogBatch.new(batch))
                                        } else {
                                            Result.success(Unit)
                                        }
                                    exporter.shutdown()
                                    message.response.complete(result)
                                    throw CancellationException("Shutdown")
                                }
                                is AsyncBatchLogMessage.SetResource -> {
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

    override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
        val res = sender.trySend(AsyncBatchLogMessage.ExportLog(record, instrumentation))
        if (res.isFailure) {
            droppedLogsCount.addAndFetch(1)
        }
    }

    override fun forceFlush(): OTelSdkResult {
        val deferred = CompletableDeferred<OTelSdkResult>()
        val sendRes = sender.trySend(AsyncBatchLogMessage.Flush(deferred))
        return if (sendRes.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to send flush message"))
        }
    }

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        val deferred = CompletableDeferred<OTelSdkResult>()
        val sendRes = sender.trySend(AsyncBatchLogMessage.Shutdown(deferred))
        return if (sendRes.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to send shutdown message"))
        }
    }

    override fun setResource(resource: Resource) {
        sender.trySend(AsyncBatchLogMessage.SetResource(resource))
    }

    public companion object {
        public fun <R : RuntimeChannel> builder(
            exporter: LogExporter,
            runtime: R,
        ): AsyncBatchLogProcessorBuilder<R> = AsyncBatchLogProcessorBuilder(exporter, runtime)
    }
}

public class AsyncBatchLogProcessorBuilder<R : RuntimeChannel>(
    private val exporter: LogExporter,
    private val runtime: R,
) {
    private var config: BatchConfig = BatchConfig()

    public fun withBatchConfig(config: BatchConfig): AsyncBatchLogProcessorBuilder<R> {
        this.config = config
        return this
    }

    public fun build(): AsyncBatchLogProcessor<R> =
        AsyncBatchLogProcessor(exporter, config, runtime)
}
