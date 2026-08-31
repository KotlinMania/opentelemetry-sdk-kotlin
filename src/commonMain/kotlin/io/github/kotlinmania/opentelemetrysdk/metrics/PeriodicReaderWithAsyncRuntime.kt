// port-lint: source metrics/periodic_reader_with_async_runtime.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.RuntimeChannel
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal sealed class AsyncPeriodicReaderMessage {
    data object Export : AsyncPeriodicReaderMessage()

    data class Flush(
        val response: CompletableDeferred<OTelSdkResult>,
    ) : AsyncPeriodicReaderMessage()

    data class Shutdown(
        val response: CompletableDeferred<OTelSdkResult>,
    ) : AsyncPeriodicReaderMessage()
}

/**
 * An asynchronous periodic reader that exports metrics using a background runtime.
 */
public class AsyncPeriodicReader<R : RuntimeChannel>(
    private val exporter: PushMetricExporter,
    public val interval: Duration,
    public val timeout: Duration,
    public val runtime: R,
) : MetricReader {
    private var sdkProducer: SdkProducer? = null
    private var isShutdown: Boolean = false
    private val messageChannel = Channel<AsyncPeriodicReaderMessage>(256)

    init {
        runtime.spawn {
            try {
                coroutineScope {
                    val tickerJob =
                        launch {
                            while (isActive) {
                                runtime.delay(interval)
                                messageChannel.trySend(AsyncPeriodicReaderMessage.Export)
                            }
                        }

                    try {
                        messageChannel.receiveAsFlow().collect { message ->
                            when (message) {
                                is AsyncPeriodicReaderMessage.Export -> {
                                    collectAndExport()
                                }
                                is AsyncPeriodicReaderMessage.Flush -> {
                                    val result = collectAndExport()
                                    message.response.complete(result)
                                }
                                is AsyncPeriodicReaderMessage.Shutdown -> {
                                    tickerJob.cancel()
                                    val result = collectAndExport()
                                    exporter.shutdownWithTimeout(timeout)
                                    message.response.complete(result)
                                    throw CancellationException("Shutdown")
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

    private fun collectAndExport(): OTelSdkResult {
        val producer = sdkProducer ?: return Result.success(Unit)
        val rm = ResourceMetrics(Resource.empty(), emptyList())
        val collectRes = producer.produce(rm)
        if (collectRes.isFailure) return collectRes
        if (rm.scopeMetrics.isEmpty()) return Result.success(Unit)
        return exporter.export(rm)
    }

    override fun registerPipeline(producer: SdkProducer) {
        if (sdkProducer == null) {
            sdkProducer = producer
        }
    }

    override fun collect(rm: ResourceMetrics): OTelSdkResult {
        if (isShutdown) {
            return Result.failure(OTelSdkError.InternalFailure("reader is shut down or not registered"))
        }
        val producer =
            sdkProducer
                ?: return Result.failure(OTelSdkError.InternalFailure("reader is shut down or not registered"))
        return producer.produce(rm)
    }

    override fun forceFlush(): OTelSdkResult {
        if (isShutdown) {
            return Result.failure(OTelSdkError.InternalFailure("reader is shut down or not registered"))
        }
        val deferred = CompletableDeferred<OTelSdkResult>()
        val sendRes = messageChannel.trySend(AsyncPeriodicReaderMessage.Flush(deferred))
        return if (sendRes.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(OTelSdkError.InternalFailure("Failed to send flush message"))
        }
    }

    override fun shutdown(): OTelSdkResult = shutdownWithTimeout(timeout)

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        if (isShutdown) {
            return Result.success(Unit)
        }
        isShutdown = true
        val deferred = CompletableDeferred<OTelSdkResult>()
        val sendRes = messageChannel.trySend(AsyncPeriodicReaderMessage.Shutdown(deferred))
        return if (sendRes.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(OTelSdkError.InternalFailure("Failed to send shutdown message"))
        }
    }

    override fun temporality(kind: InstrumentKind): Temporality = exporter.temporality()

    public companion object {
        public fun <R : RuntimeChannel> builder(
            exporter: PushMetricExporter,
            runtime: R,
        ): AsyncPeriodicReaderBuilder<R> = AsyncPeriodicReaderBuilder(exporter, runtime)
    }
}

public class AsyncPeriodicReaderBuilder<R : RuntimeChannel>(
    private val exporter: PushMetricExporter,
    private val runtime: R,
) {
    private var interval: Duration = 30.seconds
    private var timeout: Duration = 30.seconds

    public fun withInterval(interval: Duration): AsyncPeriodicReaderBuilder<R> {
        this.interval = interval
        return this
    }

    public fun withTimeout(timeout: Duration): AsyncPeriodicReaderBuilder<R> {
        this.timeout = timeout
        return this
    }

    public fun build(): AsyncPeriodicReader<R> =
        AsyncPeriodicReader(exporter, interval, timeout, runtime)
}
