// port-lint: source trace/events.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.time.Instant

/**
 * An event recorded on a span.
 */
public data class Event(
    public val name: String,
    public val timestamp: Instant,
    public val attributes: List<KeyValue> = emptyList(),
    public val droppedAttributesCount: UInt = 0u,
) {
    public companion object {
        public fun withName(name: String): Event =
            Event(
                name = name,
                timestamp =
                    kotlin.time.Clock.System
                        .now(),
            )
    }
}

/**
 * Stores span events along with dropped count.
 */
public data class SpanEvents(
    /** The events stored as a list. Could be empty if there are no events. */
    public val events: List<Event> = emptyList(),
    /** The number of Events dropped from the span. */
    public val droppedCount: UInt = 0u,
) : Iterable<Event> {
    override fun iterator(): Iterator<Event> = events.iterator()

    public val size: Int get() = events.size

    public val isEmpty: Boolean get() = events.isEmpty()

    public operator fun get(index: Int): Event = events[index]

    /**
     * Returns a new [SpanEvents] with the given event appended.
     */
    public fun withAddedEvent(event: Event): SpanEvents =
        copy(events = events + event)

    public fun addEvent(event: Event): SpanEvents = withAddedEvent(event)

    public fun intoIter(): Iterator<Event> = iterator()

    public fun deref(): List<Event> = events

    public companion object {
        public val EMPTY: SpanEvents = SpanEvents()

        public fun default(): SpanEvents = EMPTY

        public fun new(): SpanEvents = EMPTY
    }
}
