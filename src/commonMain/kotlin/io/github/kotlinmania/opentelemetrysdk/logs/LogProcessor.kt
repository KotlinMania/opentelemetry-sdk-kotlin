// port-lint: source opentelemetry_sdk/src/logs/log_processor.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Interface for log record processing and exporting.
 */
public interface LogProcessor {
    /**
     * Called when a log record is ready to be processed and exported.
     */
    public fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope)

    /**
     * Force the logs lying in the cache to be exported.
     */
    public fun forceFlush(): OTelSdkResult

    /**
     * Shuts down the processor with a timeout.
     */
    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

    /**
     * Shuts down the processor with default timeout.
     */
    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    /**
     * Check if logging is enabled.
     */
    public fun eventEnabled(level: Severity, target: String, name: String? = null): Boolean = true

    /**
     * Set the resource for the log processor.
     */
    public fun setResource(resource: Resource) {}
}
