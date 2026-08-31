// port-lint: source opentelemetry_sdk/src/metrics/internal/last_value.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.Gauge
import io.github.kotlinmania.opentelemetrysdk.metrics.data.GaugeDataPoint
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.time.Clock

internal class Assign<T> internal constructor(
    private val numberOps: NumberOps<T>,
    val value: AtomicTracker<T> = numberOps.newAtomicTracker(),
) : Aggregator<Unit, T> {
    override fun update(value: T) {
        this.value.store(value)
    }

    override fun cloneAndReset(init: Unit): Aggregator<Unit, T> =
        Assign(
            numberOps = numberOps,
            value = numberOps.newAtomicTracker(value.getValue()),
        )
}

/**
 * Summarizes a set of measurements as their last recorded value.
 */
internal class LastValue<T> internal constructor(
    val numberOps: NumberOps<T>,
    val temporality: Temporality,
    val filter: AttributeSetFilter,
    val cardinalityLimit: Int,
) : Measure<T>,
    ComputeAggregation {
    private val valueMap: ValueMap<Assign<T>, Unit, T> = ValueMap(Unit, cardinalityLimit) { Assign(numberOps) }

    fun delta(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val dataPoints = mutableListOf<GaugeDataPoint<T>>()
        valueMap.collectAndReset(dataPoints) { attributes, aggr ->
            GaugeDataPoint(
                attributes = attributes,
                value = aggr.value.getValue(),
                exemplars = emptyList(),
            )
        }

        val gaugeData =
            Gauge(
                dataPoints = dataPoints,
                time = Clock.System.now(),
            )
        return Pair(dataPoints.size, MetricData.GaugeData(gaugeData))
    }

    fun cumulative(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val dataPoints = mutableListOf<GaugeDataPoint<T>>()
        valueMap.collectReadonly(dataPoints) { attributes, aggr ->
            GaugeDataPoint(
                attributes = attributes,
                value = aggr.value.getValue(),
                exemplars = emptyList(),
            )
        }

        val gaugeData =
            Gauge(
                dataPoints = dataPoints,
                time = Clock.System.now(),
            )
        return Pair(dataPoints.size, MetricData.GaugeData(gaugeData))
    }

    override fun call(
        measurement: T,
        attrs: List<KeyValue>,
    ) {
        filter.apply(attrs) { filtered ->
            valueMap.measure(measurement, filtered)
        }
    }

    override fun call(dest: AggregatedMetrics?): Pair<Int, AggregatedMetrics?> {
        val existingData = dest?.let { numberOps.extractMetricData(it) }
        val (len, newMetricData) =
            when (temporality) {
                Temporality.Delta -> delta(existingData)
                else -> cumulative(existingData)
            }
        return Pair(len, newMetricData?.let { numberOps.makeAggregatedMetrics(it) })
    }
}
