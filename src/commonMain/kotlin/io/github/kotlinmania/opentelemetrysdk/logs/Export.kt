// port-lint: source opentelemetry_sdk/src/logs/export.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A batch of log records to be exported by a [LogExporter].
 */
public data class LogBatch(
    public val records: List<OwnedLogData>,
) : Iterable<OwnedLogData> {
    public fun iter(): Iterator<OwnedLogData> = records.iterator()

    override fun iterator(): Iterator<OwnedLogData> = iter()

    public companion object {
        public fun new(records: List<OwnedLogData>): LogBatch =
            LogBatch(records)
    }
}

/**
 * Interface that log exporters should implement.
 */
public interface LogExporter {
    /**
     * Exports a batch of log records and their associated instrumentation scopes.
     */
    public fun export(batch: LogBatch): OTelSdkResult

    /**
     * Shuts down the exporter with a timeout.
     */
    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

    /**
     * Shuts down the exporter with a default timeout.
     */
    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    /**
     * Check if logs are enabled.
     */
    public fun eventEnabled(level: Severity, target: String, name: String? = null): Boolean = true

    /**
     * Set the resource for the exporter.
     */
    public fun setResource(resource: Resource) {}
}
