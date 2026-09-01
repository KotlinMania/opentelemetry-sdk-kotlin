// port-lint: tests trace/span.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class SpanTest {
    private fun initTracerAndData(): Pair<SdkTracer, SpanData> {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("opentelemetry")
        val now = Clock.System.now()
        val data =
            SpanData(
                spanContext = SpanContext.EMPTY,
                parentSpanId = SpanId.INVALID,
                parentSpanIsRemote = false,
                spanKind = SpanKind.INTERNAL,
                name = "opentelemetry",
                startTime = now,
                endTime = now,
                attributes = emptyList(),
                droppedAttributesCount = 0u,
                events = SpanEvents.EMPTY,
                links = SpanLinks.EMPTY,
                status = Status.Unset,
                instrumentationScope = InstrumentationScope.EMPTY,
            )
        return Pair(tracer, data)
    }

    private fun createSpan(): Span {
        val (tracer, data) = initTracerAndData()
        return Span.new(
            spanContext = SpanContext.EMPTY,
            data = data,
            tracer = tracer,
            spanLimits = SpanLimits.DEFAULT,
        )
    }

    @Test
    fun createSpanWithoutData() {
        val (tracer, _) = initTracerAndData()
        val span =
            Span.new(
                spanContext = SpanContext.EMPTY,
                data = null,
                tracer = tracer,
                spanLimits = SpanLimits.DEFAULT,
            )
        var hasData = false
        span.withData { hasData = true }
        assertFalse(hasData)
    }

    @Test
    fun createSpanWithDataMut() {
        val (tracer, data) = initTracerAndData()
        val span =
            Span.new(
                spanContext = SpanContext.EMPTY,
                data = data,
                tracer = tracer,
                spanLimits = SpanLimits.DEFAULT,
            )
        span.withData { d ->
            assertEquals(data, d)
        }
    }

    @Test
    fun addEvent() {
        val span = createSpan()
        val name = "some_event"
        val attributes = listOf(KeyValue("k", "v"))
        span.addEvent(name, attributes)
        span.withData { data ->
            val event = data.events.firstOrNull()
            assertNotNull(event)
            assertEquals(name, event.name)
            assertEquals(attributes, event.attributes)
        }
    }

    @Test
    fun addEventWithTimestamp() {
        val span = createSpan()
        val name = "some_event"
        val attributes = listOf(KeyValue("k", "v"))
        val timestamp = Clock.System.now()
        span.addEventWithTimestamp(name, timestamp, attributes)
        span.withData { data ->
            val event = data.events.firstOrNull()
            assertNotNull(event)
            assertEquals(timestamp, event.timestamp)
            assertEquals(name, event.name)
            assertEquals(attributes, event.attributes)
        }
    }

    @Test
    fun recordError() {
        val span = createSpan()
        val err = Exception("something broke")
        span.recordError(err)
        span.withData { data ->
            val event = data.events.firstOrNull()
            assertNotNull(event)
            assertEquals("exception", event.name)
            assertEquals(listOf(KeyValue("exception.message", "something broke")), event.attributes)
        }
    }

    @Test
    fun setAttribute() {
        val span = createSpan()
        val attr = KeyValue("k", "v")
        span.setAttribute(attr)
        span.withData { data ->
            val matching = data.attributes.filter { it.key == attr.key }
            assertEquals(1, matching.size)
            assertEquals(attr.value, matching[0].value)
        }
    }

    @Test
    fun setAttributes() {
        val span = createSpan()
        val attributes = listOf(KeyValue("k1", "v1"), KeyValue("k2", "v2"))
        span.setAttributes(attributes)
        span.withData { data ->
            assertEquals(2, data.attributes.size)
        }
    }

    @Test
    fun setStatus() {
        run {
            val span = createSpan()
            val status = Status.Ok
            span.setStatus(status)
            span.withData { assertEquals(status, it.status) }
        }
        run {
            val span = createSpan()
            val status = Status.Unset
            span.setStatus(status)
            span.withData { assertEquals(status, it.status) }
        }
        run {
            val span = createSpan()
            val status = Status.error("error")
            span.setStatus(status)
            span.withData { assertEquals(status, it.status) }
        }
        run {
            val span = createSpan()
            span.setStatus(Status.Ok)
            span.setStatus(Status.error("error"))
            span.withData { assertEquals(Status.Ok, it.status) }
        }
        run {
            val span = createSpan()
            span.setStatus(Status.Unset)
            span.setStatus(Status.error("error"))
            span.withData { assertEquals(Status.error("error"), it.status) }
        }
    }

    @Test
    fun updateName() {
        val span = createSpan()
        val name = "new_name"
        span.updateName(name)
        span.withData { assertEquals(name, it.name) }
    }

    @Test
    fun end() {
        val span = createSpan()
        span.end()
    }

    @Test
    fun endWithTimestamp() {
        val span = createSpan()
        val timestamp = Clock.System.now()
        span.endWithTimestamp(timestamp)
        assertFalse(span.isRecording())
    }

    @Test
    fun allowsToGetSpanContextAfterEnd() {
        val span = createSpan()
        span.end()
        assertEquals(SpanContext.EMPTY, span.spanContext)
    }

    @Test
    fun endOnlyOnce() {
        val span = createSpan()
        val timestamp = Clock.System.now()
        span.endWithTimestamp(timestamp)
        span.endWithTimestamp(timestamp + 10.seconds)
        assertFalse(span.isRecording())
    }

    @Test
    fun noopAfterEnd() {
        val span = createSpan()
        span.end()
        span.addEvent("some_event", listOf(KeyValue("k", "v")))
        span.addEventWithTimestamp("some_event", Clock.System.now(), listOf(KeyValue("k", "v")))
        span.recordError(Exception("OTHER"))
        span.setAttribute(KeyValue("k", "v"))
        span.setStatus(Status.error("ERROR"))
        span.updateName("new_name")
        assertFalse(span.isRecording())
        assertEquals(null, span.withData { it })
    }

    @Test
    fun isRecordingTrueWhenNotEnded() {
        val span = createSpan()
        assertTrue(span.isRecording())
    }

    @Test
    fun isRecordingFalseAfterEnd() {
        val span = createSpan()
        span.end()
        assertFalse(span.isRecording())
    }

    @Test
    fun exceedSpanAttributesLimit() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("opentelemetry-test")

        val initialAttributes =
            (0..DEFAULT_MAX_ATTRIBUTES_PER_SPAN.toInt()).map { i ->
                KeyValue("key $i", i.toString())
            }
        val span = tracer.spanBuilder("test_span").withAttributes(initialAttributes).start()
        span.setAttribute(KeyValue("key3", "value3"))
        span.setAttributes(listOf(KeyValue("foo", "1"), KeyValue("bar", "2")))

        val actualSpan = span.withData { it }!!
        assertEquals(DEFAULT_MAX_ATTRIBUTES_PER_SPAN.toInt(), actualSpan.attributes.size)
        assertEquals(4u, actualSpan.droppedAttributesCount)
    }

    @Test
    fun exceedEventAttributesLimit() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("opentelemetry-test")

        val event1Attrs =
            (0 until (DEFAULT_MAX_ATTRIBUTES_PER_EVENT.toInt() * 2)).map { i ->
                KeyValue("key $i", i.toString())
            }
        val event1 = Event("test event", Clock.System.now(), event1Attrs)

        val span = tracer.spanBuilder("test").withEvents(listOf(event1)).start()
        span.addEvent("another test event", event1Attrs)

        val eventQueue = span.withData { it.events }!!
        val eventList = eventQueue.events.take(2)
        assertEquals(2, eventList.size)
        assertEquals(128, eventList[0].attributes.size)
        assertEquals(128, eventList[1].attributes.size)
    }

    @Test
    fun exceedLinkAttributesLimit() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("opentelemetry-test")

        val linkAttrs =
            (0 until (DEFAULT_MAX_ATTRIBUTES_PER_LINK.toInt() * 2)).map { i ->
                KeyValue("key $i", i.toString())
            }
        val link = Link(SpanContext.EMPTY, linkAttrs)

        val span = tracer.spanBuilder("test").withLinks(listOf(link)).start()
        val linkQueue = span.withData { it.links }!!
        val processedLink = linkQueue.links.first()
        assertEquals(128, processedLink.attributes.size)
    }

    @Test
    fun exceedSpanLinksLimit() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("opentelemetry-test")

        val links =
            (0 until (DEFAULT_MAX_LINKS_PER_SPAN.toInt() * 2)).map {
                Link(SpanContext.EMPTY)
            }
        val span = tracer.spanBuilder("test").withLinks(links).start()
        span.addLink(SpanContext.EMPTY)

        val linkQueue = span.withData { it.links }!!
        assertEquals(DEFAULT_MAX_LINKS_PER_SPAN.toInt(), linkQueue.links.size)
    }

    @Test
    fun exceedSpanEventsLimit() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("opentelemetry-test")

        val events =
            (0 until (DEFAULT_MAX_EVENT_PER_SPAN.toInt() * 2)).map {
                Event("test event", Clock.System.now())
            }
        val span = tracer.spanBuilder("test").withEvents(events).start()
        span.addEvent("test event again, after span builder")
        span.addEvent("test event once again, after span builder")

        val spanEvents = span.withData { it.events }!!
        assertEquals(DEFAULT_MAX_EVENT_PER_SPAN.toInt(), spanEvents.events.size)
    }

    @Test
    fun testSpanExportedData() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("test")

        val span = tracer.start("test_span")
        span.addEvent("test_event")
        span.setStatus(Status.error(""))

        val exportedData = span.exportedData()
        assertNotNull(exportedData)
        val res = provider.shutdown()
        assertTrue(res.isSuccess)
        val droppedSpan = tracer.start("span_with_dropped_provider")
        assertEquals(null, droppedSpan.exportedData())
    }
}
