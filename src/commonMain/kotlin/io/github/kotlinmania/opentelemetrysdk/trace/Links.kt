// port-lint: source trace/links.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue

/**
 * A link to another span.
 */
public data class Link(
    public val spanContext: SpanContext,
    public val attributes: List<KeyValue> = emptyList(),
    public val droppedAttributesCount: UInt = 0u,
) {
    public companion object {
        public fun withContext(spanContext: SpanContext): Link = Link(spanContext)
    }
}

/**
 * Stores span links along with dropped count.
 */
public data class SpanLinks(
    /** The links stored as a list. Could be empty if there are no links. */
    public val links: List<Link> = emptyList(),
    /** The number of links dropped from the span. */
    public val droppedCount: UInt = 0u,
) : Iterable<Link> {
    override fun iterator(): Iterator<Link> = links.iterator()

    public val size: Int get() = links.size

    public val isEmpty: Boolean get() = links.isEmpty()

    public operator fun get(index: Int): Link = links[index]

    /**
     * Returns a new [SpanLinks] with the given link appended.
     */
    public fun withAddedLink(link: Link): SpanLinks =
        copy(links = links + link)

    public companion object {
        public val EMPTY: SpanLinks = SpanLinks()
    }
}
