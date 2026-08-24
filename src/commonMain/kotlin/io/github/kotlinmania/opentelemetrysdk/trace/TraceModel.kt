// port-lint: source trace/mod.rs
package io.github.kotlinmania.opentelemetrysdk.trace

/**
 * 16-byte identifier for a trace.
 */
public data class TraceId(
    public val high: ULong = 0u,
    public val low: ULong = 0u,
) {
    public val isZero: Boolean get() = high == 0uL && low == 0uL

    public fun toHexString(): String {
        val h = high.toString(16).padStart(16, '0')
        val l = low.toString(16).padStart(16, '0')
        return "$h$l"
    }

    public companion object {
        public val INVALID: TraceId = TraceId(0u, 0u)

        public fun from(high: ULong, low: ULong): TraceId = TraceId(high, low)

        public fun from(value: ULong): TraceId = TraceId(0u, value)

        public fun fromHexString(hex: String): TraceId {
            if (hex.length != 32) return INVALID
            val h = hex.substring(0, 16).toULongOrNull(16) ?: return INVALID
            val l = hex.substring(16, 32).toULongOrNull(16) ?: return INVALID
            return TraceId(h, l)
        }
    }
}

/**
 * 8-byte identifier for a span.
 */
public data class SpanId(
    public val value: ULong = 0u,
) {
    public val isZero: Boolean get() = value == 0uL

    public fun toHexString(): String = value.toString(16).padStart(16, '0')

    public companion object {
        public val INVALID: SpanId = SpanId(0u)

        public fun from(value: ULong): SpanId = SpanId(value)

        public fun fromHexString(hex: String): SpanId {
            if (hex.length != 16) return INVALID
            val v = hex.toULongOrNull(16) ?: return INVALID
            return SpanId(v)
        }
    }
}

/**
 * Trace flags containing sampling and random bits.
 */
public data class TraceFlags(
    public val flags: UByte = 0u,
) {
    public val isSampled: Boolean get() = (flags.toUInt() and 0x01u) != 0u

    public companion object {
        public val DEFAULT: TraceFlags = TraceFlags(0u)
        public val SAMPLED: TraceFlags = TraceFlags(1u)
    }
}

/**
 * Entry in a trace state key-value list.
 */
public data class TraceStateEntry(
    public val key: String,
    public val value: String,
)

/**
 * Trace state key-value list.
 */
public data class TraceState(
    public val entries: List<TraceStateEntry> = emptyList(),
) {
    public fun get(key: String): String? = entries.find { it.key == key }?.value

    public fun with(key: String, value: String): TraceState {
        val filtered = entries.filterNot { it.key == key }
        return TraceState(listOf(TraceStateEntry(key, value)) + filtered)
    }

    public fun without(key: String): TraceState =
        TraceState(entries.filterNot { it.key == key })

    public companion object {
        public val DEFAULT: TraceState = TraceState(emptyList())

        public fun fromEntries(entries: List<TraceStateEntry>): TraceState =
            TraceState(entries)

        internal fun fromKeyValue(entries: List<Pair<String, String>>): TraceState =
            TraceState(entries.map { TraceStateEntry(it.first, it.second) })
    }
}

/**
 * Context of a span, containing trace and span identifiers, flags, and state.
 */
public data class SpanContext(
    public val traceId: TraceId = TraceId.INVALID,
    public val spanId: SpanId = SpanId.INVALID,
    public val traceFlags: TraceFlags = TraceFlags.DEFAULT,
    public val isRemote: Boolean = false,
    public val traceState: TraceState = TraceState.DEFAULT,
) {
    public val isValid: Boolean get() = !traceId.isZero && !spanId.isZero

    public val isSampled: Boolean get() = traceFlags.isSampled

    public companion object {
        public val EMPTY: SpanContext = SpanContext()

        public fun emptyContext(): SpanContext = EMPTY

        public fun new(
            traceId: TraceId,
            spanId: SpanId,
            traceFlags: TraceFlags,
            isRemote: Boolean,
            traceState: TraceState,
        ): SpanContext = SpanContext(traceId, spanId, traceFlags, isRemote, traceState)
    }
}

/**
 * Kind of a span.
 */
public enum class SpanKind {
    INTERNAL,
    SERVER,
    CLIENT,
    PRODUCER,
    CONSUMER,
}

/**
 * Status of a span.
 */
public sealed interface Status : Comparable<Status> {
    public val priority: Int

    public data object Unset : Status {
        override val priority: Int = 0
        override fun compareTo(other: Status): Int = priority.compareTo(other.priority)
    }

    public data class Error(public val message: String = "") : Status {
        override val priority: Int = 1
        override fun compareTo(other: Status): Int = priority.compareTo(other.priority)
    }

    public data object Ok : Status {
        override val priority: Int = 2
        override fun compareTo(other: Status): Int = priority.compareTo(other.priority)
    }

    public companion object {
        public fun error(message: String = ""): Status = Error(message)
    }
}
