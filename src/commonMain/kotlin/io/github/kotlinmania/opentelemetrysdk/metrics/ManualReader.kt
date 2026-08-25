// port-lint: source metrics/manual_reader.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import kotlin.time.Duration

/**
 * A simple [MetricReader] that allows an application to read metrics on demand.
 */
public class ManualReader internal constructor(
    private val temporality: Temporality,
) : MetricReader {
    private var sdkProducer: SdkProducer? = null
    private var isShutdown: Boolean = false

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

    override fun forceFlush(): OTelSdkResult = Result.success(Unit)

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        sdkProducer = null
        isShutdown = true
        return Result.success(Unit)
    }

    override fun temporality(kind: InstrumentKind): Temporality = kind.temporalityPreference(temporality)

    public companion object {
        /**
         * Configuration for this reader.
         */
        public fun builder(): ManualReaderBuilder = ManualReaderBuilder()
    }
}

/**
 * Configuration for a [ManualReader].
 */
public class ManualReaderBuilder {
    private var temporality: Temporality = Temporality.Cumulative

    /**
     * Set the [Temporality] of the exporter.
     */
    public fun withTemporality(temporality: Temporality): ManualReaderBuilder {
        this.temporality = temporality
        return this
    }

    /**
     * Create a new [ManualReader] from this configuration.
     */
    public fun build(): ManualReader = ManualReader(temporality)
}
