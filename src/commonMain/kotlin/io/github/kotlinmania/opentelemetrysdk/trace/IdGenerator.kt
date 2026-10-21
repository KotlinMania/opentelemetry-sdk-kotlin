package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetry.SpanId
import io.github.kotlinmania.opentelemetry.TraceId
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
        val bytes = ByteArray(16)
        while (bytes.all { it == 0.toByte() }) {
            Random.nextBytes(bytes)
        }
        return TraceId.fromBytes(bytes)
    }

    override fun newSpanId(): SpanId {
        var value = Random.nextLong()
        while (value == 0L) {
            value = Random.nextLong()
        }
        return SpanId(value)
    }

    public companion object {
        public val DEFAULT: RandomIdGenerator = RandomIdGenerator()
    }
}
