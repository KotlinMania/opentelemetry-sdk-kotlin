package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
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
