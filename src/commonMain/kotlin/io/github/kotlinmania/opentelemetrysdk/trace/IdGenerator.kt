// port-lint: source trace/id_generator/mod.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import kotlin.random.Random

/**
 * Interface for generating IDs.
 */
public interface IdGenerator {
    /** Generate a new [TraceId]. */
    public fun newTraceId(): TraceId

    /** Generate a new [SpanId]. */
    public fun newSpanId(): SpanId
}

/**
 * Default [IdGenerator] implementation using random numbers.
 */
public class RandomIdGenerator : IdGenerator {
    override fun newTraceId(): TraceId {
        var high = Random.nextLong().toULong()
        var low = Random.nextLong().toULong()
        while (high == 0uL && low == 0uL) {
            high = Random.nextLong().toULong()
            low = Random.nextLong().toULong()
        }
        return TraceId(high, low)
    }

    override fun newSpanId(): SpanId {
        var value = Random.nextLong().toULong()
        while (value == 0uL) {
            value = Random.nextLong().toULong()
        }
        return SpanId(value)
    }

    public companion object {
        public val DEFAULT: RandomIdGenerator = RandomIdGenerator()
    }
}
