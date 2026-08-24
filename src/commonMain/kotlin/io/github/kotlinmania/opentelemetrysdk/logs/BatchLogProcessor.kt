// port-lint: source logs/batch_log_processor.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Configuration for [BatchLogProcessor].
 */
public data class BatchConfig(
    public val maxQueueSize: Int = 2048,
    public val scheduledDelay: Duration = 1000.milliseconds,
    public val maxExportBatchSize: Int = 512,
    public val maxExportTimeout: Duration = 30000.milliseconds,
)

/**
 * Builder for creating [BatchConfig] instances.
 */
public class BatchConfigBuilder {
    private var maxQueueSize: Int = 2048
    private var scheduledDelay: Duration = 1000.milliseconds
    private var maxExportBatchSize: Int = 512
    private var maxExportTimeout: Duration = 30000.milliseconds

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

    public fun build(): BatchConfig =
        BatchConfig(
            maxQueueSize = maxQueueSize,
            scheduledDelay = scheduledDelay,
            maxExportBatchSize = maxExportBatchSize,
            maxExportTimeout = maxExportTimeout,
        )

    public companion object {
        public fun new(): BatchConfigBuilder = BatchConfigBuilder()
    }
}

/**
 * A [LogProcessor] that collects logs in a buffer and exports them in batches.
 */
@OptIn(ExperimentalAtomicApi::class)
public class BatchLogProcessor(
    private val exporter: LogExporter,
    private val config: BatchConfig = BatchConfig(),
) : LogProcessor {
    private val queueRef = AtomicReference<List<OwnedLogData>>(emptyList())
    private val isShutdownRef = AtomicBoolean(false)

    override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
        if (isShutdownRef.load()) {
            return
        }
        val entry = OwnedLogData(record = record.clone(), instrumentation = instrumentation)
        while (true) {
            val current = queueRef.load()
            if (current.size >= config.maxQueueSize) {
                // Buffer full: drop log record
                return
            }
            val next = current + entry
            if (queueRef.compareAndSet(current, next)) {
                if (next.size >= config.maxExportBatchSize) {
                    flushBatch()
                }
                break
            }
        }
    }

    private fun flushBatch() {
        var batch: List<OwnedLogData> = emptyList()
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
            exporter.export(LogBatch.new(batch))
        }
    }

    override fun forceFlush(): OTelSdkResult {
        while (true) {
            var batch: List<OwnedLogData> = emptyList()
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
            exporter.export(LogBatch.new(batch))
        }
        return Result.success(Unit)
    }

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        isShutdownRef.store(true)
        forceFlush()
        return exporter.shutdownWithTimeout(timeout)
    }

    override fun setResource(resource: Resource) {
        exporter.setResource(resource)
    }

    override fun eventEnabled(level: Severity, target: String, name: String?): Boolean =
        exporter.eventEnabled(level, target, name)

    public companion object {
        public fun builder(exporter: LogExporter): BatchLogProcessorBuilder =
            BatchLogProcessorBuilder(exporter)
    }
}

/**
 * Builder for [BatchLogProcessor].
 */
public class BatchLogProcessorBuilder(
    private val exporter: LogExporter,
) {
    private var config: BatchConfig = BatchConfig()

    public fun withBatchConfig(config: BatchConfig): BatchLogProcessorBuilder {
        this.config = config
        return this
    }

    public fun build(): BatchLogProcessor = BatchLogProcessor(exporter, config)
}
