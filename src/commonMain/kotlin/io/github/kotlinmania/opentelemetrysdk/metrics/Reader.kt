// port-lint: source opentelemetry_sdk/src/metrics/reader.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * The interface used between the SDK and an exporter.
 */
public interface MetricReader {
    /**
     * Registers a [MetricReader] with an [SdkProducer].
     */
    public fun registerPipeline(producer: SdkProducer)

    /**
     * Gathers and returns all metric data related to the [MetricReader] from the SDK.
     */
    public fun collect(rm: ResourceMetrics): OTelSdkResult

    /**
     * Flushes all metric measurements held in an export pipeline.
     */
    public fun forceFlush(): OTelSdkResult = Result.success(Unit)

    /**
     * Flushes all metric measurements and releases held computational resources.
     */
    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

    /**
     * Shutdown with default timeout of 5 seconds.
     */
    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    /**
     * The output temporality, a function of instrument kind.
     */
    public fun temporality(kind: InstrumentKind): Temporality
}

/**
 * Produces metrics for a [MetricReader].
 */
public interface SdkProducer {
    /**
     * Returns aggregated metrics from a single collection.
     */
    public fun produce(rm: ResourceMetrics): OTelSdkResult
}
