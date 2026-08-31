// port-lint: source opentelemetry_sdk/src/trace/span_limit.rs
package io.github.kotlinmania.opentelemetrysdk.trace

/**
 * Default maximum count of events per span.
 */
public const val DEFAULT_MAX_EVENT_PER_SPAN: UInt = 128u

/**
 * Default maximum count of attributes per span.
 */
public const val DEFAULT_MAX_ATTRIBUTES_PER_SPAN: UInt = 128u

/**
 * Default maximum count of links per span.
 */
public const val DEFAULT_MAX_LINKS_PER_SPAN: UInt = 128u

/**
 * Default maximum count of attributes per event.
 */
public const val DEFAULT_MAX_ATTRIBUTES_PER_EVENT: UInt = 128u

/**
 * Default maximum count of attributes per link.
 */
public const val DEFAULT_MAX_ATTRIBUTES_PER_LINK: UInt = 128u

/**
 * Span limit configuration to keep attributes, events and links to a span in a reasonable number.
 *
 * Erroneous code can add unintended attributes, events, and links to a span. If these collections
 * are unbounded, they can quickly exhaust available memory, resulting in crashes that are
 * difficult to recover from safely.
 */
public data class SpanLimits(
    /** The max events that can be added to a `Span`. */
    public val maxEventsPerSpan: UInt = DEFAULT_MAX_EVENT_PER_SPAN,
    /** The max attributes that can be added to a `Span`. */
    public val maxAttributesPerSpan: UInt = DEFAULT_MAX_ATTRIBUTES_PER_SPAN,
    /** The max links that can be added to a `Span`. */
    public val maxLinksPerSpan: UInt = DEFAULT_MAX_LINKS_PER_SPAN,
    /** The max attributes that can be added into an `Event`. */
    public val maxAttributesPerEvent: UInt = DEFAULT_MAX_ATTRIBUTES_PER_EVENT,
    /** The max attributes that can be added into a `Link`. */
    public val maxAttributesPerLink: UInt = DEFAULT_MAX_ATTRIBUTES_PER_LINK,
) {
    public companion object {
        /**
         * Default [SpanLimits] instance.
         */
        public val DEFAULT: SpanLimits = SpanLimits()

        /**
         * Creates a [SpanLimits] with default limits.
         */
        public fun defaultLimits(): SpanLimits = DEFAULT

        public fun default(): SpanLimits = DEFAULT

        public fun new(): SpanLimits = DEFAULT
    }
}
