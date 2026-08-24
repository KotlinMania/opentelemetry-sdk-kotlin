// port-lint: source logs/in_memory_exporter.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

/**
 * Represents a single log event without resource context.
 */
public data class OwnedLogData(
    public val record: SdkLogRecord,
    public val instrumentation: InstrumentationScope,
)

/**
 * Associates a [SdkLogRecord] with a [Resource] and [InstrumentationScope].
 */
public data class LogDataWithResource(
    public val record: SdkLogRecord,
    public val instrumentation: InstrumentationScope,
    public val resource: Resource,
)

/**
 * An in-memory logs exporter that stores logs data in memory.
 */
@OptIn(ExperimentalAtomicApi::class)
public class InMemoryLogExporter internal constructor(
    private val shouldResetOnShutdown: Boolean = true,
) : LogExporter {
    private val logsRef = AtomicReference<List<OwnedLogData>>(emptyList())
    private val resourceRef = AtomicReference(Resource.builder().build())
    private val shutdownCalledRef = AtomicBoolean(false)

    public fun isShutdownCalled(): Boolean = shutdownCalledRef.load()

    public fun getEmittedLogs(): Result<List<LogDataWithResource>> {
        val currentLogs = logsRef.load()
        val currentResource = resourceRef.load()
        val mapped = currentLogs.map { logData ->
            LogDataWithResource(
                record = logData.record.clone(),
                instrumentation = logData.instrumentation,
                resource = currentResource,
            )
        }
        return Result.success(mapped)
    }

    public fun reset() {
        logsRef.store(emptyList())
    }

    override fun export(batch: LogBatch): OTelSdkResult {
        val newEntries = batch.records.map { logData ->
            OwnedLogData(logData.record.clone(), logData.instrumentation)
        }
        while (true) {
            val current = logsRef.load()
            val next = current + newEntries
            if (logsRef.compareAndSet(current, next)) {
                break
            }
        }
        return Result.success(Unit)
    }

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        shutdownCalledRef.store(true)
        if (shouldResetOnShutdown) {
            reset()
        }
        return Result.success(Unit)
    }

    override fun setResource(resource: Resource) {
        resourceRef.store(resource)
    }

    public companion object {
        public fun builder(): InMemoryLogExporterBuilder = InMemoryLogExporterBuilder()
        public fun defaultExporter(): InMemoryLogExporter = builder().build()
    }
}

/**
 * Builder for [InMemoryLogExporter].
 */
public class InMemoryLogExporterBuilder {
    private var resetOnShutdown: Boolean = true

    public fun resetOnShutdown(reset: Boolean): InMemoryLogExporterBuilder {
        this.resetOnShutdown = reset
        return this
    }

    public fun keepRecordsOnShutdown(): InMemoryLogExporterBuilder {
        this.resetOnShutdown = false
        return this
    }

    public fun build(): InMemoryLogExporter = InMemoryLogExporter(resetOnShutdown)

    public companion object {
        public fun new(): InMemoryLogExporterBuilder = InMemoryLogExporterBuilder()
    }
}
