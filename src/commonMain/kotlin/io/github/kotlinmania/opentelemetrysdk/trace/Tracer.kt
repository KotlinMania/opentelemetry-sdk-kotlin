// port-lint: source trace/tracer.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.time.Instant

/**
 * Tracer implementation to create and manage spans.
 */
public class SdkTracer internal constructor(
    public val instrumentationScope: InstrumentationScope,
    internal val provider: SdkTracerProvider,
) {
    public fun provider(): SdkTracerProvider = provider

    public fun spanBuilder(name: String): SpanBuilder = SpanBuilder(name, this)

    public fun start(name: String): Span = spanBuilder(name).start()

    public inline fun <T> inSpan(name: String, block: (Span) -> T): T {
        val span = start(name)
        try {
            return block(span)
        } finally {
            span.end()
        }
    }
}

/**
 * Builder for creating and starting a [Span].
 */
public class SpanBuilder(
    public var name: String,
    private val tracer: SdkTracer,
) {
    public var parentContext: SpanContext? = null
    public var spanKind: SpanKind = SpanKind.INTERNAL
    public var startTime: Instant? = null
    private val attributesList: MutableList<KeyValue> = mutableListOf()
    private val eventsList: MutableList<Event> = mutableListOf()
    private val linksList: MutableList<Link> = mutableListOf()
    public val attributes: List<KeyValue> get() = attributesList
    public val events: List<Event> get() = eventsList
    public val links: List<Link> get() = linksList
    public var status: Status = Status.Unset

    public fun withParent(parentContext: SpanContext): SpanBuilder {
        this.parentContext = parentContext
        return this
    }

    public fun withKind(kind: SpanKind): SpanBuilder {
        this.spanKind = kind
        return this
    }

    public fun withStartTime(startTime: Instant): SpanBuilder {
        this.startTime = startTime
        return this
    }

    public fun withAttribute(attribute: KeyValue): SpanBuilder {
        this.attributesList.add(attribute)
        return this
    }

    public fun withAttributes(attributes: Iterable<KeyValue>): SpanBuilder {
        this.attributesList.addAll(attributes)
        return this
    }

    public fun withEvents(events: Iterable<Event>): SpanBuilder {
        this.eventsList.addAll(events)
        return this
    }

    public fun withLinks(links: Iterable<Link>): SpanBuilder {
        this.linksList.addAll(links)
        return this
    }

    public fun withStatus(status: Status): SpanBuilder {
        this.status = status
        return this
    }

    public fun start(): Span = tracer.provider.startSpanFromBuilder(this, tracer)
}
