// port-lint: source metrics/internal/aggregate.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Receives measurements to be aggregated.
 */
internal interface Measure<T> {
    fun call(
        measurement: T,
        attrs: List<KeyValue>,
    )
}

/**
 * Stores the aggregate of measurements into the aggregation and returns the number
 * of aggregate data-points output.
 */
internal interface ComputeAggregation {
    /**
     * Compute the new aggregation and store in dest or create new.
     * Returns (number of data points, aggregated metrics).
     */
    fun call(dest: AggregatedMetrics?): Pair<Int, AggregatedMetrics?>
}

/**
 * Separate measure and collect functions for an aggregate.
 */
internal class AggregateFns<T>(
    val measure: Measure<T>,
    val collect: ComputeAggregation,
)

internal class AggregateTime(
    val start: Instant,
    val current: Instant,
)

/**
 * Initialized [AggregateTime] for specific [Temporality].
 */
@OptIn(ExperimentalAtomicApi::class)
internal class AggregateTimeInitiator {
    private val startRef = AtomicReference(Clock.System.now())

    fun delta(): AggregateTime {
        val currentTime = Clock.System.now()
        val startTime = startRef.exchange(currentTime)
        return AggregateTime(start = startTime, current = currentTime)
    }

    fun cumulative(): AggregateTime {
        val currentTime = Clock.System.now()
        val startTime = startRef.load()
        return AggregateTime(start = startTime, current = currentTime)
    }
}

/**
 * Applies filter on provided attribute set. No-op if filter is not set.
 */
internal class AttributeSetFilter(
    private val filter: ((KeyValue) -> Boolean)? = null,
) {
    fun apply(
        attrs: List<KeyValue>,
        run: (List<KeyValue>) -> Unit,
    ) {
        val f = filter
        if (f != null) {
            val filteredAttrs = attrs.filter(f)
            run(filteredAttrs)
        } else {
            run(attrs)
        }
    }
}

/**
 * Builds aggregate functions for a given [NumberOps].
 */
internal class AggregateBuilder<T> internal constructor(
    val numberOps: NumberOps<T>,
    val temporality: Temporality,
    val filter: AttributeSetFilter,
    val cardinalityLimit: Int,
) {
    internal constructor(
        numberOps: NumberOps<T>,
        temporality: Temporality,
        filter: ((KeyValue) -> Boolean)?,
        cardinalityLimit: Int,
    ) : this(numberOps, temporality, AttributeSetFilter(filter), cardinalityLimit)

    /**
     * Builds a last-value aggregate function input and output.
     */
    fun lastValue(overwriteTemporality: Temporality? = null): AggregateFns<T> {
        val inst =
            LastValue(
                numberOps = numberOps,
                temporality = overwriteTemporality ?: temporality,
                filter = filter,
                cardinalityLimit = cardinalityLimit,
            )
        return AggregateFns(measure = inst, collect = inst)
    }

    /**
     * Builds a precomputed sum aggregate function input and output.
     */
    fun precomputedSum(monotonic: Boolean): AggregateFns<T> {
        val inst =
            PrecomputedSum(
                numberOps = numberOps,
                temporality = temporality,
                filter = filter,
                monotonic = monotonic,
                cardinalityLimit = cardinalityLimit,
            )
        return AggregateFns(measure = inst, collect = inst)
    }

    /**
     * Builds a sum aggregate function input and output.
     */
    fun sum(monotonic: Boolean): AggregateFns<T> {
        val inst =
            Sum(
                numberOps = numberOps,
                temporality = temporality,
                filter = filter,
                monotonic = monotonic,
                cardinalityLimit = cardinalityLimit,
            )
        return AggregateFns(measure = inst, collect = inst)
    }

    /**
     * Builds a histogram aggregate function input and output.
     */
    fun explicitBucketHistogram(
        boundaries: List<Double>,
        recordMinMax: Boolean,
        recordSum: Boolean,
    ): AggregateFns<T> {
        val inst =
            Histogram(
                numberOps = numberOps,
                temporality = temporality,
                filter = filter,
                bounds = boundaries,
                recordMinMax = recordMinMax,
                recordSum = recordSum,
                cardinalityLimit = cardinalityLimit,
            )
        return AggregateFns(measure = inst, collect = inst)
    }

    /**
     * Builds an exponential histogram aggregate function input and output.
     */
    fun exponentialBucketHistogram(
        maxSize: UInt,
        maxScale: Byte,
        recordMinMax: Boolean,
        recordSum: Boolean,
    ): AggregateFns<T> {
        val inst =
            ExpoHistogram(
                numberOps = numberOps,
                temporality = temporality,
                filter = filter,
                maxSize = maxSize,
                maxScale = maxScale,
                recordMinMax = recordMinMax,
                recordSum = recordSum,
                cardinalityLimit = cardinalityLimit,
            )
        return AggregateFns(measure = inst, collect = inst)
    }
}
