// port-lint: source testing/metrics/metric_reader.rs
package io.github.kotlinmania.opentelemetrysdk.testing.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.InstrumentKind
import io.github.kotlinmania.opentelemetrysdk.metrics.MetricReader
import io.github.kotlinmania.opentelemetrysdk.metrics.SdkProducer
import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

/**
 * MetricReader implementation for testing.
 */
@OptIn(ExperimentalAtomicApi::class)
public class TestMetricReader : MetricReader {
    private val isShutdownRef = AtomicBoolean(false)

    /**
     * Check if the reader is shutdown.
     */
    public fun isShutdown(): Boolean = isShutdownRef.load()

    override fun registerPipeline(producer: SdkProducer) {}

    override fun collect(rm: ResourceMetrics): OTelSdkResult = Result.success(Unit)

    override fun forceFlush(): OTelSdkResult = Result.success(Unit)

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        val result = forceFlush()
        isShutdownRef.store(true)
        return result
    }

    override fun temporality(kind: InstrumentKind): Temporality = Temporality.Cumulative

    public companion object {
        public fun new(): TestMetricReader = TestMetricReader()

        public fun default(): TestMetricReader = new()
    }
}
