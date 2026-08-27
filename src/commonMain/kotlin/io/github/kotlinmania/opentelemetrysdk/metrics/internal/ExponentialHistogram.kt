// port-lint: source metrics/internal/exponential_histogram.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ExponentialBucket
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ExponentialHistogram
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ExponentialHistogramDataPoint
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

internal const val EXPO_MAX_SCALE: Byte = 20
internal const val EXPO_MIN_SCALE: Byte = -10

private const val LOG2_E: Double = 1.4426950408889634

private val SCALE_FACTORS: DoubleArray by lazy {
    DoubleArray(21) { i ->
        LOG2_E * 2.0.pow(i.toDouble())
    }
}

internal fun frexp(x: Double): Pair<Double, Int> {
    var y = x.toRawBits()
    val ee = ((y ushr 52) and 0x7ffL).toInt()

    if (ee == 0) {
        if (x != 0.0) {
            val x1p64 = Double.fromBits(0x43f0000000000000L)
            val (rx, re) = frexp(x * x1p64)
            return Pair(rx, re - 64)
        }
        return Pair(x, 0)
    } else if (ee == 0x7ff) {
        return Pair(x, 0)
    }

    val e = ee - 0x3fe
    y = y and 0x800fffffffffffffUL.toLong()
    y = y or 0x3fe0000000000000L

    return Pair(Double.fromBits(y), e)
}

internal fun scaleChange(
    maxSize: Int,
    bin: Int,
    startBin: Int,
    length: Int,
): UInt {
    if (length == 0) {
        return 0u
    }

    var low = startBin
    var high = bin
    if (startBin >= bin) {
        low = bin
        high = startBin + length - 1
    }

    var count = 0u
    while (high - low >= maxSize) {
        low = low shr 1
        high = high shr 1
        count += 1u

        if (count > (EXPO_MAX_SCALE - EXPO_MIN_SCALE).toUInt()) {
            return count
        }
    }

    return count
}

internal class ExpoBuckets(
    var startBin: Int = 0,
    var counts: MutableList<ULong> = mutableListOf(),
) {
    fun record(bin: Int) {
        if (counts.isEmpty()) {
            counts = mutableListOf(1uL)
            startBin = bin
            return
        }

        val endBin = startBin + counts.size - 1

        if (bin in startBin..endBin) {
            val idx = bin - startBin
            counts[idx] = counts[idx] + 1uL
            return
        }

        if (bin < startBin) {
            val newSize = endBin - bin + 1
            val zeroes = MutableList(newSize) { 0uL }
            val shift = startBin - bin
            for (i in counts.indices) {
                zeroes[shift + i] = counts[i]
            }
            counts = zeroes
            counts[0] = 1uL
            startBin = bin
        } else {
            val neededSize = bin - startBin + 1
            while (counts.size < neededSize) {
                counts.add(0uL)
            }
            counts[bin - startBin] = 1uL
        }
    }

    fun downscale(delta: UInt) {
        if (counts.size <= 1 || delta < 1u) {
            startBin = startBin shr delta.toInt()
            return
        }

        val steps = 1 shl delta.toInt()
        var offset = startBin % steps
        offset = (offset + steps) % steps

        for (i in 1 until counts.size) {
            val idx = i + offset
            val targetIdx = idx / steps
            if (idx % steps == 0) {
                counts[targetIdx] = counts[i]
            } else {
                counts[targetIdx] = counts[targetIdx] + counts[i]
            }
        }

        val lastIdx = (counts.size - 1 + offset) / steps
        counts = counts.subList(0, lastIdx + 1).toMutableList()
        startBin = startBin shr delta.toInt()
    }

    fun copy(): ExpoBuckets = ExpoBuckets(startBin, counts.toMutableList())
}

internal data class BucketConfig(
    val maxSize: Int,
    val maxScale: Byte,
)

internal class ExpoHistogramDataPoint<T> internal constructor(
    private val numberOps: NumberOps<T>,
    val maxSize: Int,
    var count: ULong = 0uL,
    var min: T = numberOps.max,
    var max: T = numberOps.min,
    var sum: T = numberOps.zero,
    var scale: Byte = 0,
    val posBuckets: ExpoBuckets = ExpoBuckets(),
    val negBuckets: ExpoBuckets = ExpoBuckets(),
    var zeroCount: ULong = 0uL,
) {
    internal constructor(numberOps: NumberOps<T>, config: BucketConfig) : this(
        numberOps = numberOps,
        maxSize = config.maxSize,
        scale = config.maxScale,
    )

    fun record(v: T) {
        count += 1uL

        if (numberOps.compare(v, min) < 0) {
            min = v
        }
        if (numberOps.compare(v, max) > 0) {
            max = v
        }
        sum = numberOps.add(sum, v)

        val floatV = numberOps.toDouble(v)
        val absV = abs(floatV)

        if (absV == 0.0) {
            zeroCount += 1uL
            return
        }

        var bin = getBin(absV)
        val vIsNegative = numberOps.compare(v, numberOps.zero) < 0

        val bucket = if (vIsNegative) negBuckets else posBuckets
        val scaleDelta = scaleChange(maxSize, bin, bucket.startBin, bucket.counts.size)

        if (scaleDelta > 0u) {
            if ((scale - scaleDelta.toInt()) < EXPO_MIN_SCALE) {
                return
            }
            scale = (scale - scaleDelta.toInt()).toByte()
            posBuckets.downscale(scaleDelta)
            negBuckets.downscale(scaleDelta)

            bin = getBin(absV)
        }

        if (vIsNegative) {
            negBuckets.record(bin)
        } else {
            posBuckets.record(bin)
        }
    }

    fun getBin(v: Double): Int {
        val (frac, exp) = frexp(v)
        if (scale <= 0) {
            var correction = 1
            if (frac == 0.5) {
                correction = 2
            }
            return (exp - correction) shr -scale.toInt()
        }
        return (exp shl scale.toInt()) + (ln(frac) * SCALE_FACTORS[scale.toInt()]).toInt() - 1
    }

    fun copy(): ExpoHistogramDataPoint<T> =
        ExpoHistogramDataPoint(
            numberOps = numberOps,
            maxSize = maxSize,
            count = count,
            min = min,
            max = max,
            sum = sum,
            scale = scale,
            posBuckets = posBuckets.copy(),
            negBuckets = negBuckets.copy(),
            zeroCount = zeroCount,
        )
}

@OptIn(ExperimentalAtomicApi::class)
internal class MutexExpoDataPoint<T> internal constructor(
    private val numberOps: NumberOps<T>,
    private val config: BucketConfig,
    initial: ExpoHistogramDataPoint<T>? = null,
) : Aggregator<BucketConfig, T> {
    private val pointRef =
        AtomicReference(
            initial ?: ExpoHistogramDataPoint(numberOps, config),
        )

    override fun update(value: T) {
        while (true) {
            val cur = pointRef.load()
            val copy = cur.copy()
            copy.record(value)
            if (pointRef.compareAndSet(cur, copy)) {
                return
            }
        }
    }

    override fun cloneAndReset(init: BucketConfig): Aggregator<BucketConfig, T> {
        val empty = ExpoHistogramDataPoint(numberOps, init)
        val old = pointRef.exchange(empty)
        return MutexExpoDataPoint(numberOps, init, old)
    }

    fun getData(): ExpoHistogramDataPoint<T> = pointRef.load()
}

/**
 * Summarizes a set of measurements as an exponential histogram.
 */
internal class ExpoHistogram<T> internal constructor(
    val numberOps: NumberOps<T>,
    val temporality: Temporality,
    val filter: AttributeSetFilter,
    val maxSize: UInt,
    val maxScale: Byte,
    val recordMinMax: Boolean,
    val recordSum: Boolean,
    val cardinalityLimit: Int,
) : Measure<T>,
    ComputeAggregation {
    private val bucketConfig = BucketConfig(maxSize = maxSize.toInt(), maxScale = maxScale)
    private val valueMap: ValueMap<MutexExpoDataPoint<T>, BucketConfig, T> =
        ValueMap(bucketConfig, cardinalityLimit) { MutexExpoDataPoint(numberOps, it) }
    private val initTime = AggregateTimeInitiator()

    fun delta(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val time = initTime.delta()
        val dataPoints = mutableListOf<ExponentialHistogramDataPoint<T>>()
        valueMap.collectAndReset(dataPoints) { attributes, aggr ->
            val b = aggr.getData()
            ExponentialHistogramDataPoint(
                attributes = attributes,
                count = b.count,
                min = if (recordMinMax) b.min else null,
                max = if (recordMinMax) b.max else null,
                sum = if (recordSum) b.sum else numberOps.zero,
                scale = b.scale,
                zeroCount = b.zeroCount,
                positiveBucket =
                    ExponentialBucket(
                        offset = b.posBuckets.startBin,
                        counts = b.posBuckets.counts,
                    ),
                negativeBucket =
                    ExponentialBucket(
                        offset = b.negBuckets.startBin,
                        counts = b.negBuckets.counts,
                    ),
                zeroThreshold = 0.0,
                exemplars = emptyList(),
            )
        }

        val histData =
            ExponentialHistogram(
                dataPoints = dataPoints,
                startTime = time.start,
                time = time.current,
                temporality = Temporality.Delta,
            )
        return Pair(dataPoints.size, MetricData.ExponentialHistogramData(histData))
    }

    fun cumulative(dest: MetricData<T>?): Pair<Int, MetricData<T>?> {
        val time = initTime.cumulative()
        val dataPoints = mutableListOf<ExponentialHistogramDataPoint<T>>()
        valueMap.collectReadonly(dataPoints) { attributes, aggr ->
            val b = aggr.getData()
            ExponentialHistogramDataPoint(
                attributes = attributes,
                count = b.count,
                min = if (recordMinMax) b.min else null,
                max = if (recordMinMax) b.max else null,
                sum = if (recordSum) b.sum else numberOps.zero,
                scale = b.scale,
                zeroCount = b.zeroCount,
                positiveBucket =
                    ExponentialBucket(
                        offset = b.posBuckets.startBin,
                        counts = b.posBuckets.counts,
                    ),
                negativeBucket =
                    ExponentialBucket(
                        offset = b.negBuckets.startBin,
                        counts = b.negBuckets.counts,
                    ),
                zeroThreshold = 0.0,
                exemplars = emptyList(),
            )
        }

        val histData =
            ExponentialHistogram(
                dataPoints = dataPoints,
                startTime = time.start,
                time = time.current,
                temporality = Temporality.Cumulative,
            )
        return Pair(dataPoints.size, MetricData.ExponentialHistogramData(histData))
    }

    override fun call(
        measurement: T,
        attrs: List<KeyValue>,
    ) {
        val f = numberOps.toDouble(measurement)
        if (!f.isFinite()) return
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
