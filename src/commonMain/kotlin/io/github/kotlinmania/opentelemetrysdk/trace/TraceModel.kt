package io.github.kotlinmania.opentelemetrysdk.trace

/**
 * 16-byte identifier for a trace.
 */
public data class TraceId(
    public val high: ULong = 0u,
    public val low: ULong = 0u,
) {
    public val isZero: Boolean get() = high == 0uL && low == 0uL

    public companion object {
        public val INVALID: TraceId = TraceId(0u, 0u)
    }
}

/**
 * 8-byte identifier for a span.
 */
public data class SpanId(
    public val value: ULong = 0u,
) {
    public val isZero: Boolean get() = value == 0uL

    public companion object {
        public val INVALID: SpanId = SpanId(0u)
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
    public companion object {
        public val DEFAULT: TraceState = TraceState(emptyList())
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
public sealed interface Status {
    public data object Unset : Status
    public data object Ok : Status
    public data class Error(public val message: String = "") : Status
}
