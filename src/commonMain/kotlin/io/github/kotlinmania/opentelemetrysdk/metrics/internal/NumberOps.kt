package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData

/**
 * Number operations abstraction providing arithmetic, min/max constants,
 * and metric data conversions for generic metric aggregation.
 */
internal interface NumberOps<T> {
    val zero: T
    val min: T
    val max: T
    val typeName: String

    fun add(
        a: T,
        b: T,
    ): T

    fun sub(
        a: T,
        b: T,
    ): T

    fun compare(
        a: T,
        b: T,
    ): Int

    fun toDouble(v: T): Double

    fun newAtomicTracker(init: T = zero): AtomicTracker<T>

    fun makeAggregatedMetrics(data: MetricData<T>): AggregatedMetrics

    fun extractMetricData(data: AggregatedMetrics): MetricData<T>?
}

internal object LongOps : NumberOps<Long> {
    override val zero: Long = 0L
    override val min: Long = Long.MIN_VALUE
    override val max: Long = Long.MAX_VALUE
    override val typeName: String = "Long"

    override fun add(
        a: Long,
        b: Long,
    ): Long = a + b

    override fun sub(
        a: Long,
        b: Long,
    ): Long = a - b

    override fun compare(
        a: Long,
        b: Long,
    ): Int = a.compareTo(b)

    override fun toDouble(v: Long): Double = v.toDouble()

    override fun newAtomicTracker(init: Long): AtomicTracker<Long> = AtomicLongTracker(init)

    override fun makeAggregatedMetrics(data: MetricData<Long>): AggregatedMetrics = AggregatedMetrics.I64(data)

    @Suppress("UNCHECKED_CAST")
    override fun extractMetricData(data: AggregatedMetrics): MetricData<Long>? =
        when (data) {
            is AggregatedMetrics.I64 -> data.data as? MetricData<Long>
            else -> null
        }
}

internal object ULongOps : NumberOps<ULong> {
    override val zero: ULong = 0uL
    override val min: ULong = ULong.MIN_VALUE
    override val max: ULong = ULong.MAX_VALUE
    override val typeName: String = "ULong"

    override fun add(
        a: ULong,
        b: ULong,
    ): ULong = a + b

    override fun sub(
        a: ULong,
        b: ULong,
    ): ULong = a - b

    override fun compare(
        a: ULong,
        b: ULong,
    ): Int = a.compareTo(b)

    override fun toDouble(v: ULong): Double = v.toDouble()

    override fun newAtomicTracker(init: ULong): AtomicTracker<ULong> = AtomicULongTracker(init)

    override fun makeAggregatedMetrics(data: MetricData<ULong>): AggregatedMetrics = AggregatedMetrics.U64(data)

    @Suppress("UNCHECKED_CAST")
    override fun extractMetricData(data: AggregatedMetrics): MetricData<ULong>? =
        when (data) {
            is AggregatedMetrics.U64 -> data.data as? MetricData<ULong>
            else -> null
        }
}

internal object DoubleOps : NumberOps<Double> {
    override val zero: Double = 0.0
    override val min: Double = Double.NEGATIVE_INFINITY
    override val max: Double = Double.POSITIVE_INFINITY
    override val typeName: String = "Double"

    override fun add(
        a: Double,
        b: Double,
    ): Double = a + b

    override fun sub(
        a: Double,
        b: Double,
    ): Double = a - b

    override fun compare(
        a: Double,
        b: Double,
    ): Int = a.compareTo(b)

    override fun toDouble(v: Double): Double = v

    override fun newAtomicTracker(init: Double): AtomicTracker<Double> = AtomicDoubleTracker(init)

    override fun makeAggregatedMetrics(data: MetricData<Double>): AggregatedMetrics = AggregatedMetrics.F64(data)

    @Suppress("UNCHECKED_CAST")
    override fun extractMetricData(data: AggregatedMetrics): MetricData<Double>? =
        when (data) {
            is AggregatedMetrics.F64 -> data.data as? MetricData<Double>
            else -> null
        }
}
