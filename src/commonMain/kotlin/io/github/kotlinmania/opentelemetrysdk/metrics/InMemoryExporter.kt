// port-lint: source opentelemetry_sdk/src/metrics/in_memory_exporter.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import kotlin.time.Duration

/**
 * An in-memory metrics exporter that stores metrics data in memory.
 */
public class InMemoryMetricExporter internal constructor(
    private val temporality: Temporality,
) : PushMetricExporter {
    private val metricsList = mutableListOf<ResourceMetrics>()

    /**
     * Returns the finished metrics as a list of [ResourceMetrics].
     */
    public fun getFinishedMetrics(): Result<List<ResourceMetrics>> = Result.success(metricsList.toList())

    /**
     * Clears the internal storage of finished metrics.
     */
    public fun reset() {
        metricsList.clear()
    }

    override fun export(metrics: ResourceMetrics): OTelSdkResult {
        metricsList.add(metrics)
        return Result.success(Unit)
    }

    override fun forceFlush(): OTelSdkResult = Result.success(Unit)

    override fun shutdown(): OTelSdkResult = Result.success(Unit)

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

    override fun temporality(): Temporality = temporality

    public companion object {
        /**
         * Creates a new builder for [InMemoryMetricExporter].
         */
        public fun builder(): InMemoryMetricExporterBuilder = InMemoryMetricExporterBuilder()

        /**
         * Creates a default [InMemoryMetricExporter].
         */
        public fun default(): InMemoryMetricExporter = builder().build()
    }
}

/**
 * Builder for [InMemoryMetricExporter].
 */
public class InMemoryMetricExporterBuilder {
    private var temporality: Temporality = Temporality.Cumulative

    /**
     * Set the [Temporality] of the exporter.
     */
    public fun withTemporality(temporality: Temporality): InMemoryMetricExporterBuilder {
        this.temporality = temporality
        return this
    }

    /**
     * Creates a new instance of [InMemoryMetricExporter].
     */
    public fun build(): InMemoryMetricExporter = InMemoryMetricExporter(temporality)
}
