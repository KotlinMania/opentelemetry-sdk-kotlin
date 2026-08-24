// port-lint: source trace/span_processor.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * SpanProcessor is an interface which allows hooks for span start and end method invocations.
 */
public interface SpanProcessor {
    /** Called when a Span is started. */
    public fun onStart(span: Span, parentContext: SpanContext?)

    /** Called after a Span is ended. */
    public fun onEnd(span: SpanData)

    /** Force the spans lying in the cache to be exported. */
    public fun forceFlush(): OTelSdkResult

    /** Shuts down the processor. */
    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

    /** Shuts down the processor with default timeout. */
    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    /** Sets the resource for the span processor. */
    public fun setResource(resource: Resource) {}
}

/**
 * A [SpanProcessor] that passes finished spans to the configured [SpanExporter] immediately.
 */
public class SimpleSpanProcessor(
    private val exporter: SpanExporter,
) : SpanProcessor {
    override fun onStart(span: Span, parentContext: SpanContext?) {
        // Ignored
    }

    override fun onEnd(span: SpanData) {
        if (!span.spanContext.traceFlags.isSampled) {
            return
        }
        exporter.export(listOf(span))
    }

    override fun forceFlush(): OTelSdkResult = exporter.forceFlush()

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult =
        exporter.shutdownWithTimeout(timeout)

    override fun setResource(resource: Resource) {
        exporter.setResource(resource)
    }
}

/**
 * Batch span processor configuration.
 */
public data class BatchConfig(
    public val maxQueueSize: Int = 2048,
    public val scheduledDelay: Duration = 5000.milliseconds,
    public val maxExportBatchSize: Int = 512,
    public val maxExportTimeout: Duration = 30000.milliseconds,
    public val maxConcurrentExports: Int = 1,
)

/**
 * Builder for creating [BatchConfig] instances.
 */
public class BatchConfigBuilder {
    private var maxQueueSize: Int = 2048
    private var scheduledDelay: Duration = 5000.milliseconds
    private var maxExportBatchSize: Int = 512
    private var maxExportTimeout: Duration = 30000.milliseconds
    private var maxConcurrentExports: Int = 1

    public fun withMaxQueueSize(size: Int): BatchConfigBuilder {
        this.maxQueueSize = size
        return this
    }

    public fun withScheduledDelay(delay: Duration): BatchConfigBuilder {
        this.scheduledDelay = delay
        return this
    }

    public fun withMaxExportBatchSize(size: Int): BatchConfigBuilder {
        this.maxExportBatchSize = size
        return this
    }

    public fun withMaxExportTimeout(timeout: Duration): BatchConfigBuilder {
        this.maxExportTimeout = timeout
        return this
    }

    public fun withMaxConcurrentExports(max: Int): BatchConfigBuilder {
        this.maxConcurrentExports = max
        return this
    }

    public fun build(): BatchConfig = BatchConfig(
        maxQueueSize = maxQueueSize,
        scheduledDelay = scheduledDelay,
        maxExportBatchSize = maxExportBatchSize,
        maxExportTimeout = maxExportTimeout,
        maxConcurrentExports = maxConcurrentExports,
    )
}

/**
 * Batch span processor that buffers and batches spans before exporting.
 */
@OptIn(ExperimentalAtomicApi::class)
public class BatchSpanProcessor(
    private val exporter: SpanExporter,
    private val config: BatchConfig = BatchConfig(),
) : SpanProcessor {
    private val queueRef = AtomicReference<List<SpanData>>(emptyList())
    private val isShutdownRef = AtomicBoolean(false)

    override fun onStart(span: Span, parentContext: SpanContext?) {
        // Ignored
    }

    override fun onEnd(span: SpanData) {
        if (isShutdownRef.load() || !span.spanContext.traceFlags.isSampled) {
            return
        }
        while (true) {
            val current = queueRef.load()
            if (current.size >= config.maxQueueSize) {
                // Queue full: drop span
                return
            }
            val next = current + span
            if (queueRef.compareAndSet(current, next)) {
                if (next.size >= config.maxExportBatchSize) {
                    flushBatch()
                }
                break
            }
        }
    }

    private fun flushBatch() {
        var batch: List<SpanData> = emptyList()
        while (true) {
            val current = queueRef.load()
            if (current.isEmpty()) return
            batch = current.take(config.maxExportBatchSize)
            val next = current.drop(batch.size)
            if (queueRef.compareAndSet(current, next)) {
                break
            }
        }
        if (batch.isNotEmpty()) {
            exporter.export(batch)
        }
    }

    override fun forceFlush(): OTelSdkResult {
        while (true) {
            var batch: List<SpanData> = emptyList()
            while (true) {
                val current = queueRef.load()
                if (current.isEmpty()) break
                batch = current.take(config.maxExportBatchSize)
                val next = current.drop(batch.size)
                if (queueRef.compareAndSet(current, next)) {
                    break
                }
            }
            if (batch.isEmpty()) break
            exporter.export(batch)
        }
        return exporter.forceFlush()
    }

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        isShutdownRef.store(true)
        forceFlush()
        return exporter.shutdownWithTimeout(timeout)
    }

    override fun setResource(resource: Resource) {
        exporter.setResource(resource)
    }

    public companion object {
        public fun builder(exporter: SpanExporter): BatchSpanProcessorBuilder =
            BatchSpanProcessorBuilder(exporter)
    }
}

public class BatchSpanProcessorBuilder(
    private val exporter: SpanExporter,
) {
    private var config: BatchConfig = BatchConfig()

    public fun withBatchConfig(config: BatchConfig): BatchSpanProcessorBuilder {
        this.config = config
        return this
    }

    public fun build(): BatchSpanProcessor = BatchSpanProcessor(exporter, config)
}
