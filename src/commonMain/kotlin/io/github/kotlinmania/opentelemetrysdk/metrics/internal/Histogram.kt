// port-lint: source opentelemetry_sdk/src/metrics/internal/histogram.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.Histogram
import io.github.kotlinmania.opentelemetrysdk.metrics.data.HistogramDataPoint
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal fun partitionPoint(
    bounds: List<Double>,
    f: Double,
): Int {
    var low = 0
    var high = bounds.size
    while (low < high) {
        val mid = (low + high) ushr 1
        if (bounds[mid] < f) {
            low = mid + 1
        } else {
            high = mid
        }
    }
    return low
}

internal data class BucketsData<T>(
    val counts: List<ULong>,
    val count: ULong,
    val total: T,
    val min: T,
    val max: T,
)

@OptIn(ExperimentalAtomicApi::class)
internal class Buckets<T> internal constructor(
    private val numberOps: NumberOps<T>,
    private val n: Int,
    initial: BucketsData<T>? = null,
) : Aggregator<Int, Pair<T, Int>> {
    private val dataRef =
        AtomicReference(
            initial ?: BucketsData(
                counts = if (n == 0) emptyList() else List(n) { 0uL },
                count = 0uL,
                total = numberOps.zero,
                min = numberOps.max,
                max = numberOps.min,
            ),
        )

    override fun update(value: Pair<T, Int>) {
        val (v, index) = value
        while (true) {
            val cur = dataRef.load()
            val newCounts =
                if (cur.counts.isEmpty()) {
                    cur.counts
                } else {
                    val copy = cur.counts.toMutableList()
                    copy[index] = copy[index] + 1uL
                    copy
                }
            val newMin = if (numberOps.compare(v, cur.min) < 0) v else cur.min
            val newMax = if (numberOps.compare(v, cur.max) > 0) v else cur.max
            val next =
                BucketsData(
                    counts = newCounts,
                    count = cur.count + 1uL,
                    total = numberOps.add(cur.total, v),
                    min = newMin,
                    max = newMax,
                )
            if (dataRef.compareAndSet(cur, next)) {
                return
            }
        }
    }

    override fun cloneAndReset(init: Int): Aggregator<Int, Pair<T, Int>> {
        val empty =
            BucketsData(
                counts = if (init == 0) emptyList() else List(init) { 0uL },
                count = 0uL,
                total = numberOps.zero,
                min = numberOps.max,
                max = numberOps.min,
            )
        val old = dataRef.exchange(empty)
        return Buckets(numberOps, init, old)
    }

    fun getData(): BucketsData<T> = dataRef.load()
}

/**
 * Summarizes a set of measurements as a histogram with explicitly defined buckets.
 */
internal class Histogram<T> internal constructor(
    val numberOps: NumberOps<T>,
    val temporality: Temporality,
    val filter: AttributeSetFilter,
    val bounds: List<Double>,
    val recordMinMax: Boolean,
    val recordSum: Boolean,
    val cardinalityLimit: Int,
) : Measure<T>,
    ComputeAggregation {
    private val bucketsCount = if (bounds.isEmpty()) 0 else bounds.size + 1
    private val valueMap: ValueMap<Buckets<T>, Int, Pair<T, Int>> =
        ValueMap(bucketsCount, cardinalityLimit) { Buckets(numberOps, it) }
    private val initTime = AggregateTimeInitiator()

    fun delta(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val time = initTime.delta()
        val dataPoints = mutableListOf<HistogramDataPoint<T>>()
        valueMap.collectAndReset(dataPoints) { attributes, aggr ->
            val b = aggr.getData()
            HistogramDataPoint(
                attributes = attributes,
                count = b.count,
                bounds = bounds,
                bucketCounts = b.counts,
                sum = if (recordSum) b.total else numberOps.zero,
                min = if (recordMinMax) b.min else null,
                max = if (recordMinMax) b.max else null,
                exemplars = emptyList(),
            )
        }

        val histData =
            Histogram(
                dataPoints = dataPoints,
                startTime = time.start,
                time = time.current,
                temporality = Temporality.Delta,
            )
        return Pair(dataPoints.size, MetricData.HistogramData(histData))
    }

    fun cumulative(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val time = initTime.cumulative()
        val dataPoints = mutableListOf<HistogramDataPoint<T>>()
        valueMap.collectReadonly(dataPoints) { attributes, aggr ->
            val b = aggr.getData()
            HistogramDataPoint(
                attributes = attributes,
                count = b.count,
                bounds = bounds,
                bucketCounts = b.counts,
                sum = if (recordSum) b.total else numberOps.zero,
                min = if (recordMinMax) b.min else null,
                max = if (recordMinMax) b.max else null,
                exemplars = emptyList(),
            )
        }

        val histData =
            Histogram(
                dataPoints = dataPoints,
                startTime = time.start,
                time = time.current,
                temporality = Temporality.Cumulative,
            )
        return Pair(dataPoints.size, MetricData.HistogramData(histData))
    }

    override fun call(
        measurement: T,
        attrs: List<KeyValue>,
    ) {
        val f = numberOps.toDouble(measurement)
        val index = partitionPoint(bounds, f)
        filter.apply(attrs) { filtered ->
            valueMap.measure(Pair(measurement, index), filtered)
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
