// port-lint: source trace/span.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetry.KeyValue
import io.github.kotlinmania.opentelemetry.trace.Event
import io.github.kotlinmania.opentelemetry.trace.Link
import io.github.kotlinmania.opentelemetry.trace.SpanContext
import io.github.kotlinmania.opentelemetry.trace.Status
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Single operation within a trace.
 */
@OptIn(ExperimentalAtomicApi::class)
public class Span internal constructor(
    public val spanContext: SpanContext,
    initialData: SpanData?,
    private val tracer: SdkTracer,
    private val spanLimits: SpanLimits,
) : io.github.kotlinmania.opentelemetry.trace.Span {
    private val dataRef = AtomicReference(initialData)

    override fun spanContext(): SpanContext = spanContext

    override fun isRecording(): Boolean = dataRef.load() != null

    override fun setAttribute(attribute: KeyValue) {
        val spanAttributeLimit = spanLimits.maxAttributesPerSpan.toInt()
        while (true) {
            val current = dataRef.load() ?: return
            val next =
                if (current.attributes.size < spanAttributeLimit) {
                    current.copy(attributes = current.attributes + attribute)
                } else {
                    current.copy(droppedAttributesCount = current.droppedAttributesCount + 1u)
                }
            if (dataRef.compareAndSet(current, next)) {
                break
            }
        }
    }

    override fun setAttributes(attributes: Iterable<KeyValue>) {
        for (attr in attributes) {
            setAttribute(attr)
        }
    }

    override fun addEvent(name: String, attributes: List<KeyValue>) {
        addEventWithTimestamp(name, Clock.System.now(), attributes)
    }

    override fun addEventWithTimestamp(
        name: String,
        timestamp: Instant,
        attributes: List<KeyValue>,
    ) {
        val maxEvents = spanLimits.maxEventsPerSpan.toInt()
        val maxAttrsPerEvent = spanLimits.maxAttributesPerEvent.toInt()
        while (true) {
            val current = dataRef.load() ?: return
            val droppedAttrs = if (attributes.size > maxAttrsPerEvent) (attributes.size - maxAttrsPerEvent).toUInt() else 0u
            val cappedAttrs = attributes.take(maxAttrsPerEvent)
            val event = Event(name, timestamp, cappedAttrs, droppedAttrs)

            val nextEvents =
                if (current.events.events.size < maxEvents) {
                    SpanEvents(current.events.events + event, current.events.droppedCount)
                } else {
                    SpanEvents(current.events.events, current.events.droppedCount + 1u)
                }
            val next = current.copy(events = nextEvents)
            if (dataRef.compareAndSet(current, next)) {
                break
            }
        }
    }

    override fun addLink(spanContext: SpanContext, attributes: List<KeyValue>) {
        val maxLinks = spanLimits.maxLinksPerSpan.toInt()
        val maxAttrsPerLink = spanLimits.maxAttributesPerLink.toInt()
        while (true) {
            val current = dataRef.load() ?: return
            val droppedAttrs = if (attributes.size > maxAttrsPerLink) (attributes.size - maxAttrsPerLink).toUInt() else 0u
            val cappedAttrs = attributes.take(maxAttrsPerLink)
            val link = Link(spanContext, cappedAttrs, droppedAttrs)

            val nextLinks =
                if (current.links.links.size < maxLinks) {
                    SpanLinks(current.links.links + link, current.links.droppedCount)
                } else {
                    SpanLinks(current.links.links, current.links.droppedCount + 1u)
                }
            val next = current.copy(links = nextLinks)
            if (dataRef.compareAndSet(current, next)) {
                break
            }
        }
    }

    override fun setStatus(status: Status) {
        while (true) {
            val current = dataRef.load() ?: return
            if (status > current.status) {
                val next = current.copy(status = status)
                if (dataRef.compareAndSet(current, next)) {
                    break
                }
            } else {
                break
            }
        }
    }

    override fun updateName(newName: String) {
        while (true) {
            val current = dataRef.load() ?: return
            val next = current.copy(name = newName)
            if (dataRef.compareAndSet(current, next)) {
                break
            }
        }
    }

    override fun endWithTimestamp(timestamp: Instant) {
        ensureEndedAndExported(timestamp)
    }

    override fun end() {
        ensureEndedAndExported(null)
    }

    override fun recordError(err: Throwable) {
        val message = err.message ?: err.toString()
        val attributes = listOf(KeyValue("exception.message", message))
        addEventWithTimestamp("exception", Clock.System.now(), attributes)
    }

    public fun recordError(message: String) {
        val attributes = listOf(KeyValue("exception.message", message))
        addEventWithTimestamp("exception", Clock.System.now(), attributes)
    }

    internal fun <T> withData(block: (SpanData) -> T): T? {
        val current = dataRef.load() ?: return null
        return block(current)
    }

    public fun exportedData(): SpanData? {
        val current = dataRef.load() ?: return null
        val provider = tracer.provider
        if (provider.isShutdown()) {
            return null
        }
        return current
    }

    private fun ensureEndedAndExported(timestamp: Instant?) {
        var spanData: SpanData? = null
        while (true) {
            val current = dataRef.load() ?: return
            if (dataRef.compareAndSet(current, null)) {
                spanData = current
                break
            }
        }
        val provider = tracer.provider
        if (provider.isShutdown()) {
            return
        }
        val endTime = timestamp ?: if (spanData.endTime == spanData.startTime) Clock.System.now() else spanData.endTime
        val finalData =
            spanData.copy(
                endTime = endTime,
                instrumentationScope = tracer.instrumentationScope,
            )
        for (processor in provider.spanProcessors()) {
            processor.onEnd(finalData)
        }
    }

    public companion object {
        public fun new(
            spanContext: SpanContext,
            data: SpanData?,
            tracer: SdkTracer,
            spanLimits: SpanLimits = SpanLimits.DEFAULT,
        ): Span = Span(spanContext, data, tracer, spanLimits)
    }
}

internal fun buildExportData(
    data: SpanData,
    spanContext: SpanContext,
    tracer: SdkTracer,
): SpanData =
    data.copy(
        spanContext = spanContext,
        instrumentationScope = tracer.instrumentationScope,
    )
