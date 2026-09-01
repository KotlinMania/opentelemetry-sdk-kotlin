// port-lint: source metrics/internal/sum.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.metrics.data.SumDataPoint
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue

internal class Increment<T> internal constructor(
    private val numberOps: NumberOps<T>,
    val value: AtomicTracker<T> = numberOps.newAtomicTracker(),
) : Aggregator<Unit, T> {
    override fun update(value: T) {
        this.value.add(value)
    }

    override fun cloneAndReset(init: Unit): Aggregator<Unit, T> =
        Increment(
            numberOps = numberOps,
            value = numberOps.newAtomicTracker(value.getAndResetValue()),
        )
}

/**
 * Summarizes a set of measurements made as their arithmetic sum.
 */
internal class Sum<T> internal constructor(
    val numberOps: NumberOps<T>,
    val temporality: Temporality,
    val filter: AttributeSetFilter,
    val monotonic: Boolean,
    val cardinalityLimit: Int,
) : Measure<T>,
    ComputeAggregation {
    private val valueMap: ValueMap<Increment<T>, Unit, T> = ValueMap(Unit, cardinalityLimit) { Increment(numberOps) }
    private val initTime = AggregateTimeInitiator()

    fun delta(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val time = initTime.delta()
        val dataPoints = mutableListOf<SumDataPoint<T>>()
        valueMap.collectAndReset(dataPoints) { attributes, aggr ->
            SumDataPoint(
                attributes = attributes,
                value = aggr.value.getValue(),
                exemplars = emptyList(),
            )
        }

        val sumData =
            io.github.kotlinmania.opentelemetrysdk.metrics.data.Sum(
                dataPoints = dataPoints,
                startTime = time.start,
                time = time.current,
                temporality = Temporality.Delta,
                isMonotonic = monotonic,
            )
        return Pair(dataPoints.size, MetricData.SumData(sumData))
    }

    fun cumulative(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val time = initTime.cumulative()
        val dataPoints = mutableListOf<SumDataPoint<T>>()
        valueMap.collectReadonly(dataPoints) { attributes, aggr ->
            SumDataPoint(
                attributes = attributes,
                value = aggr.value.getValue(),
                exemplars = emptyList(),
            )
        }

        val sumData =
            io.github.kotlinmania.opentelemetrysdk.metrics.data.Sum(
                dataPoints = dataPoints,
                startTime = time.start,
                time = time.current,
                temporality = Temporality.Cumulative,
                isMonotonic = monotonic,
            )
        return Pair(dataPoints.size, MetricData.SumData(sumData))
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
