// port-lint: source opentelemetry_sdk/src/metrics/meter_provider.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Handles the creation and coordination of Meters.
 *
 * All Meters created by a [SdkMeterProvider] will be associated with the same
 * [Resource], have the same views applied to them, and have their produced
 * metric telemetry passed to the configured [MetricReader]s.
 */
@OptIn(ExperimentalAtomicApi::class)
public class SdkMeterProvider internal constructor(
    public val resource: Resource,
    public val readers: List<MetricReader>,
    public val views: List<View>,
) {
    private var isShutdown: Boolean = false
    internal val pipelines: Pipelines = Pipelines.create(resource, readers, views)
    internal val viewCache: AtomicReference<PersistentMap<String, InstrumentId>> =
        AtomicReference(persistentMapOf())

    /**
     * Creates a new [SdkMeter] with the given name.
     */
    public fun meter(name: String): SdkMeter {
        val scope =
            InstrumentationScope
                .builder(name)
                .build()
        return meterWithScope(scope)
    }

    /**
     * Creates a new [SdkMeter] with the given [InstrumentationScope].
     */
    public fun meterWithScope(scope: InstrumentationScope): SdkMeter =
        SdkMeter(scope, pipelines, viewCache)

    /**
     * Flushes all pending telemetry.
     */
    public fun forceFlush(): OTelSdkResult {
        if (isShutdown) {
            return Result.failure(OTelSdkError.AlreadyShutdown)
        }
        return pipelines.forceFlush()
    }

    /**
     * Shuts down the meter provider flushing all pending telemetry and releasing
     * any held computational resources with a timeout.
     */
    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        if (isShutdown) {
            return Result.failure(OTelSdkError.AlreadyShutdown)
        }
        isShutdown = true
        return pipelines.shutdown()
    }

    /**
     * Shuts down the meter provider with default timeout (5s).
     */
    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    public companion object {
        /**
         * Returns a default [MeterProviderBuilder].
         */
        public fun builder(): MeterProviderBuilder = MeterProviderBuilder()

        /**
         * Creates a default [SdkMeterProvider].
         */
        public fun default(): SdkMeterProvider = builder().build()
    }
}

/**
 * Configuration options for a [SdkMeterProvider].
 */
public class MeterProviderBuilder {
    private var resource: Resource? = null
    private val readers = mutableListOf<MetricReader>()
    private val views = mutableListOf<View>()

    /**
     * Associates a [Resource] with a [SdkMeterProvider].
     */
    public fun withResource(resource: Resource): MeterProviderBuilder {
        this.resource = this.resource?.merge(resource) ?: resource
        return this
    }

    /**
     * Associates a [MetricReader] with a [SdkMeterProvider].
     */
    public fun withReader(reader: MetricReader): MeterProviderBuilder {
        this.readers.add(reader)
        return this
    }

    /**
     * Adds a [PushMetricExporter] to the [SdkMeterProvider] using a [PeriodicReader].
     */
    public fun <E : PushMetricExporter> withPeriodicExporter(exporter: E): MeterProviderBuilder {
        val reader = PeriodicReader.builder(exporter).build()
        this.readers.add(reader)
        return this
    }

    /**
     * Adds a [View] to the [SdkMeterProvider].
     */
    public fun withView(view: View): MeterProviderBuilder {
        this.views.add(view)
        return this
    }

    /**
     * Construct a new [SdkMeterProvider] with this configuration.
     */
    public fun build(): SdkMeterProvider =
        SdkMeterProvider(
            resource = resource ?: Resource.builder().build(),
            readers = readers.toList(),
            views = views.toList(),
        )
}
