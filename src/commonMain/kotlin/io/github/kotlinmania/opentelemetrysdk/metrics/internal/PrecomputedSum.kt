// port-lint: source opentelemetry_sdk/src/metrics/internal/precomputed_sum.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.metrics.data.Sum
import io.github.kotlinmania.opentelemetrysdk.metrics.data.SumDataPoint
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Summarizes a set of pre-computed sums as their arithmetic sum.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class PrecomputedSum<T> internal constructor(
    val numberOps: NumberOps<T>,
    val temporality: Temporality,
    val filter: AttributeSetFilter,
    val monotonic: Boolean,
    val cardinalityLimit: Int,
) : Measure<T>,
    ComputeAggregation {
    private val valueMap: ValueMap<Assign<T>, Unit, T> = ValueMap(Unit, cardinalityLimit) { Assign(numberOps) }
    private val initTime = AggregateTimeInitiator()
    private val reported = AtomicReference<PersistentMap<List<KeyValue>, T>>(persistentMapOf())

    fun delta(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val time = initTime.delta()
        val dataPoints = mutableListOf<SumDataPoint<T>>()
        var oldReported: PersistentMap<List<KeyValue>, T>

        while (true) {
            oldReported = reported.load()
            var nextReported = persistentMapOf<List<KeyValue>, T>()
            dataPoints.clear()
            valueMap.collectAndReset(dataPoints) { attributes, aggr ->
                val value = aggr.value.getValue()
                nextReported = nextReported.putting(attributes, value)
                val prev = oldReported[attributes] ?: numberOps.zero
                val delta = numberOps.sub(value, prev)
                SumDataPoint(
                    attributes = attributes,
                    value = delta,
                    exemplars = emptyList(),
                )
            }
            if (reported.compareAndSet(oldReported, nextReported)) {
                break
            }
        }

        val sumData =
            Sum(
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
            Sum(
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
