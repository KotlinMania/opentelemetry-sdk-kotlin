// port-lint: source metrics/internal/mod.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal val STREAM_OVERFLOW_ATTRIBUTES: List<KeyValue> = listOf(KeyValue("otel.metric.overflow", true))

internal fun sortAndDedup(attributes: List<KeyValue>): List<KeyValue> {
    if (attributes.size <= 1) return attributes
    val sorted = attributes.sortedBy { it.key.name }
    val result = mutableListOf<KeyValue>()
    for (kv in sorted) {
        if (result.isEmpty() || result.last().key != kv.key) {
            result.add(kv)
        }
    }
    return result
}

/**
 * Marks a type that can have a value added and retrieved atomically.
 */
internal interface AtomicTracker<T> {
    fun store(value: T)

    fun add(value: T)

    fun getValue(): T

    fun getAndResetValue(): T
}

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicLongTracker(
    init: Long = 0L,
) : AtomicTracker<Long> {
    private val ref = AtomicLong(init)

    override fun store(value: Long) {
        ref.store(value)
    }

    override fun add(value: Long) {
        ref.addAndFetch(value)
    }

    override fun getValue(): Long = ref.load()

    override fun getAndResetValue(): Long = ref.exchange(0L)
}

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicULongTracker(
    init: ULong = 0uL,
) : AtomicTracker<ULong> {
    private val ref = AtomicLong(init.toLong())

    override fun store(value: ULong) {
        ref.store(value.toLong())
    }

    override fun add(value: ULong) {
        ref.addAndFetch(value.toLong())
    }

    override fun getValue(): ULong = ref.load().toULong()

    override fun getAndResetValue(): ULong = ref.exchange(0L).toULong()
}

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicDoubleTracker(
    init: Double = 0.0,
) : AtomicTracker<Double> {
    private val ref = AtomicLong(init.toRawBits())

    override fun store(value: Double) {
        ref.store(value.toRawBits())
    }

    override fun add(value: Double) {
        while (true) {
            val currentBits = ref.load()
            val currentVal = Double.fromBits(currentBits)
            val newVal = currentVal + value
            val newBits = newVal.toRawBits()
            if (ref.compareAndSet(currentBits, newBits)) {
                return
            }
        }
    }

    override fun getValue(): Double = Double.fromBits(ref.load())

    override fun getAndResetValue(): Double = Double.fromBits(ref.exchange(0.0.toRawBits()))
}

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

/**
 * An aggregator that can receive measurements and produce summaries.
 */
internal interface Aggregator<InitConfig, PreComputedValue> {
    fun update(value: PreComputedValue)

    fun cloneAndReset(init: InitConfig): Aggregator<InitConfig, PreComputedValue>
}

/**
 * Maps attribute sets to individual aggregators with lock-free concurrent updates
 * and cardinality limits.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class ValueMap<A : Aggregator<InitConfig, PreComputedValue>, InitConfig, PreComputedValue>(
    val config: InitConfig,
    val cardinalityLimit: Int,
    private val factory: (InitConfig) -> A,
) {
    private val count = AtomicInt(0)
    private val trackers = AtomicReference<PersistentMap<List<KeyValue>, A>>(persistentMapOf())
    private val noAttributeTracker: A = factory(config)
    private val hasNoAttributeValue = AtomicBoolean(false)

    private fun isUnderCardinalityLimit(): Boolean = count.load() < cardinalityLimit

    fun measure(
        value: PreComputedValue,
        attributes: List<KeyValue>,
    ) {
        if (attributes.isEmpty()) {
            noAttributeTracker.update(value)
            hasNoAttributeValue.store(true)
            return
        }

        // Fast path: check current map for provided order or sorted order
        val currentMap = trackers.load()
        val existing = currentMap[attributes]
        if (existing != null) {
            existing.update(value)
            return
        }

        val sortedAttrs = sortAndDedup(attributes)
        val existingSorted = currentMap[sortedAttrs]
        if (existingSorted != null) {
            existingSorted.update(value)
            return
        }

        // Slow path: update map in CAS loop
        while (true) {
            val map = trackers.load()
            val t1 = map[attributes]
            if (t1 != null) {
                t1.update(value)
                return
            }
            val t2 = map[sortedAttrs]
            if (t2 != null) {
                t2.update(value)
                return
            }

            if (isUnderCardinalityLimit()) {
                val newTracker = factory(config)
                newTracker.update(value)
                val updatedMap = map.putting(attributes, newTracker).putting(sortedAttrs, newTracker)
                if (trackers.compareAndSet(map, updatedMap)) {
                    count.addAndFetch(1)
                    return
                }
            } else {
                val overflowAttrs = STREAM_OVERFLOW_ATTRIBUTES
                val overflowTracker = map[overflowAttrs]
                if (overflowTracker != null) {
                    overflowTracker.update(value)
                    return
                }
                val newOverflowTracker = factory(config)
                newOverflowTracker.update(value)
                val updatedMap = map.putting(overflowAttrs, newOverflowTracker)
                if (trackers.compareAndSet(map, updatedMap)) {
                    return
                }
            }
        }
    }

    /**
     * Iterate through all attribute sets and populate DataPoints in readonly mode (Cumulative).
     */
    fun <Res> collectReadonly(
        dest: MutableList<Res>,
        mapFn: (List<KeyValue>, A) -> Res,
    ) {
        dest.clear()
        if (hasNoAttributeValue.load()) {
            dest.add(mapFn(emptyList<KeyValue>(), noAttributeTracker))
        }

        val currentMap = trackers.load()
        val seen = HashSet<A>()
        for ((attrs, tracker) in currentMap) {
            if (seen.add(tracker)) {
                dest.add(mapFn(attrs, tracker))
            }
        }
    }

    /**
     * Iterate through all attribute sets, populate DataPoints and reset (Delta).
     */
    @Suppress("UNCHECKED_CAST")
    fun <Res> collectAndReset(
        dest: MutableList<Res>,
        mapFn: (List<KeyValue>, A) -> Res,
    ) {
        dest.clear()
        if (hasNoAttributeValue.exchange(false)) {
            val resetNoAttr = noAttributeTracker.cloneAndReset(config) as A
            dest.add(mapFn(emptyList<KeyValue>(), resetNoAttr))
        }

        val oldMap = trackers.exchange(persistentMapOf())
        count.store(0)

        val seen = HashSet<A>()
        for ((attrs, tracker) in oldMap) {
            if (seen.add(tracker)) {
                val resetTracker = tracker.cloneAndReset(config) as A
                dest.add(mapFn(attrs, resetTracker))
            }
        }
    }
}
