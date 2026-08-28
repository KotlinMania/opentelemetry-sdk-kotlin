// port-lint: source logs/concurrent_log_processor.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.time.Duration

/**
 * A concurrent log processor calls exporter's export method on each emit. This
 * processor does not buffer logs.
 */
public class SimpleConcurrentLogProcessor(
    private val exporter: LogExporter,
) : LogProcessor {
    override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
        val entry = OwnedLogData(record = record.clone(), instrumentation = instrumentation)
        exporter.export(LogBatch.new(listOf(entry)))
    }

    override fun forceFlush(): OTelSdkResult = Result.success(Unit)

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult =
        exporter.shutdownWithTimeout(timeout)

    override fun eventEnabled(level: Severity, target: String, name: String?): Boolean =
        exporter.eventEnabled(level, target, name)

    override fun setResource(resource: Resource) {
        exporter.setResource(resource)
    }

    public companion object {
        public fun new(exporter: LogExporter): SimpleConcurrentLogProcessor =
            SimpleConcurrentLogProcessor(exporter)
    }
}
