// port-lint: source opentelemetry_sdk/src/metrics/exporter.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Exporter handles the delivery of metric data to external receivers.
 *
 * This is the final component in the metric push pipeline.
 */
public interface PushMetricExporter {
    /**
     * Export serializes and transmits metric data to a receiver.
     */
    public fun export(metrics: ResourceMetrics): OTelSdkResult

    /**
     * Flushes any metric data held by an exporter.
     */
    public fun forceFlush(): OTelSdkResult = Result.success(Unit)

    /**
     * Releases any held computational resources with a given timeout.
     */
    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

    /**
     * Releases any held computational resources with the default timeout (5s).
     */
    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    /**
     * Access the [Temporality] of the metric exporter.
     */
    public fun temporality(): Temporality
}
