// port-lint: source metrics/periodic_reader.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A [MetricReader] that periodically collects and exports metrics at a configurable interval.
 */
public class PeriodicReader<E : PushMetricExporter> internal constructor(
    private val exporter: E,
    private val interval: Duration,
) : MetricReader {
    private var sdkProducer: SdkProducer? = null
    private var isShutdown: Boolean = false

    /** The interval between exports. */
    public val intervalDuration: Duration get() = interval

    /** The underlying exporter. */
    public val pushExporter: E get() = exporter

    override fun registerPipeline(producer: SdkProducer) {
        if (sdkProducer == null) {
            sdkProducer = producer
        }
    }

    override fun collect(rm: ResourceMetrics): OTelSdkResult {
        if (isShutdown) {
            return Result.failure(OTelSdkError.InternalFailure("reader is shut down or not registered"))
        }
        val producer =
            sdkProducer
                ?: return Result.failure(OTelSdkError.InternalFailure("reader is shut down or not registered"))
        return producer.produce(rm)
    }

    override fun forceFlush(): OTelSdkResult = exporter.forceFlush()

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        isShutdown = true
        sdkProducer = null
        return exporter.shutdownWithTimeout(timeout)
    }

    override fun temporality(kind: InstrumentKind): Temporality = exporter.temporality()

    public companion object {
        /**
         * Creates a new [PeriodicReaderBuilder] with the given exporter.
         */
        public fun <E : PushMetricExporter> builder(exporter: E): PeriodicReaderBuilder<E> =
            PeriodicReaderBuilder(exporter)
    }
}

/**
 * Configuration options for [PeriodicReader].
 */
public class PeriodicReaderBuilder<E : PushMetricExporter>(
    private val exporter: E,
) {
    private var interval: Duration = 60.seconds

    /**
     * Configures the intervening time between exports for a [PeriodicReader].
     */
    public fun withInterval(interval: Duration): PeriodicReaderBuilder<E> {
        if (!interval.isNegative() && interval != Duration.ZERO) {
            this.interval = interval
        }
        return this
    }

    /**
     * Creates a [PeriodicReader] with the given config.
     */
    public fun build(): PeriodicReader<E> = PeriodicReader(exporter, interval)
}
