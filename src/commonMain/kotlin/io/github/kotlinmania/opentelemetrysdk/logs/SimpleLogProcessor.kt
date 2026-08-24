// port-lint: source logs/simple_log_processor.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.Context
import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

/**
 * A [LogProcessor] that forwards log records to the exporter immediately.
 */
@OptIn(ExperimentalAtomicApi::class)
public class SimpleLogProcessor(
    private val exporter: LogExporter,
) : LogProcessor {
    private val isShutdownRef = AtomicBoolean(false)

    override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
        val guard = Context.enterTelemetrySuppressedScope()
        try {
            if (isShutdownRef.load()) {
                return
            }
            exporter.export(LogBatch.new(listOf(OwnedLogData(record = record, instrumentation = instrumentation))))
        } finally {
            guard.close()
        }
    }

    override fun forceFlush(): OTelSdkResult = Result.success(Unit)

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        isShutdownRef.store(true)
        return exporter.shutdownWithTimeout(timeout)
    }

    override fun setResource(resource: Resource) {
        exporter.setResource(resource)
    }

    override fun eventEnabled(level: Severity, target: String, name: String?): Boolean =
        exporter.eventEnabled(level, target, name)

    public companion object {
        public fun new(exporter: LogExporter): SimpleLogProcessor = SimpleLogProcessor(exporter)
    }
}
