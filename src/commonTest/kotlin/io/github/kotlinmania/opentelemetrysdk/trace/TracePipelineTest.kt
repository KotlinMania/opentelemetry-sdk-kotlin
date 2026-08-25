// port-lint: source trace/mod.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TracePipelineTest {
    @Test
    fun testTracerInSpan() {
        val exporter = InMemorySpanExporter.defaultExporter()
        val provider =
            SdkTracerProvider
                .builder()
                .withSimpleExporter(exporter)
                .build()

        val tracer = provider.tracer("test_tracer")
        tracer.inSpan("span_name") { span ->
            assertTrue(span.isRecording())
            span.updateName("span_name_updated")
            span.setAttribute(KeyValue(Key("attribute1"), Value.of("value1")))
            span.addEvent("test-event", emptyList())
            span.addLink(
                SpanContext(
                    traceId = TraceId(0u, 47u),
                    spanId = SpanId(11u),
                ),
            )
        }

        val spans = exporter.getFinishedSpans().getOrThrow()
        assertEquals(1, spans.size)
        val span = spans[0]
        assertEquals("span_name_updated", span.name)
        assertEquals("test_tracer", span.instrumentationScope.name)
        assertEquals(1, span.attributes.size)
        assertEquals(1, span.events.events.size)
        assertEquals("test-event", span.events.events[0].name)
        assertEquals(1, span.links.links.size)
        assertEquals(
            TraceId(0u, 47u),
            span.links.links[0]
                .spanContext.traceId,
        )
        assertEquals(
            SpanId(11u),
            span.links.links[0]
                .spanContext.spanId,
        )
        assertTrue(span.spanContext.traceFlags.isSampled)
        assertFalse(span.spanContext.isRemote)
        assertEquals(Status.Unset, span.status)
    }

    @Test
    fun testTracerStartAndStatusOrdering() {
        val exporter = InMemorySpanExporter.defaultExporter()
        val provider =
            SdkTracerProvider
                .builder()
                .withSimpleExporter(exporter)
                .build()

        val tracer = provider.tracer("test_tracer")
        val span = tracer.start("span_name")
        span.setAttribute(KeyValue(Key("attribute1"), Value.of("value1")))
        span.addEvent("test-event", emptyList())
        span.setStatus(Status.Error("cancelled"))
        // Ok has higher priority than Error, so Ok replaces Error
        span.setStatus(Status.Ok)
        // Error has lower priority than Ok, so Error will NOT replace Ok
        span.setStatus(Status.Error("should not overwrite ok"))
        span.end()

        // After end, further updates are ignored
        span.updateName("span_name_updated")

        val spans = exporter.getFinishedSpans().getOrThrow()
        assertEquals(1, spans.size)
        val s = spans[0]
        assertEquals("span_name", s.name)
        assertEquals(Status.Ok, s.status)
    }

    @Test
    fun testSpanBuilderWithKindAndParent() {
        val exporter = InMemorySpanExporter.defaultExporter()
        val provider =
            SdkTracerProvider
                .builder()
                .withSimpleExporter(exporter)
                .build()

        val parentContext =
            SpanContext(
                traceId = TraceId(1u, 2u),
                spanId = SpanId(3u),
                traceFlags = TraceFlags.SAMPLED,
            )

        val tracer = provider.tracer("test_tracer")
        val span =
            tracer
                .spanBuilder("server_span")
                .withKind(SpanKind.SERVER)
                .withParent(parentContext)
                .withAttribute(KeyValue(Key("k"), Value.of("v")))
                .start()

        span.end()

        val spans = exporter.getFinishedSpans().getOrThrow()
        assertEquals(1, spans.size)
        val s = spans[0]
        assertEquals(SpanKind.SERVER, s.spanKind)
        assertEquals(parentContext.traceId, s.spanContext.traceId)
        assertEquals(parentContext.spanId, s.parentSpanId)
    }

    @Test
    fun testExceedSpanLimits() {
        val exporter = InMemorySpanExporter.defaultExporter()
        val limits =
            SpanLimits(
                maxAttributesPerSpan = 2u,
                maxEventsPerSpan = 2u,
                maxLinksPerSpan = 2u,
            )
        val provider =
            SdkTracerProvider
                .builder()
                .withSpanLimits(limits)
                .withSimpleExporter(exporter)
                .build()

        val tracer = provider.tracer("limits_tracer")
        val span = tracer.start("limited_span")

        // Add 3 attributes (limit is 2)
        span.setAttribute(KeyValue(Key("a1"), Value.of("1")))
        span.setAttribute(KeyValue(Key("a2"), Value.of("2")))
        span.setAttribute(KeyValue(Key("a3"), Value.of("3")))

        // Add 3 events (limit is 2)
        span.addEvent("e1")
        span.addEvent("e2")
        span.addEvent("e3")

        // Add 3 links (limit is 2)
        span.addLink(SpanContext(TraceId(1u, 1u), SpanId(1u)))
        span.addLink(SpanContext(TraceId(1u, 2u), SpanId(2u)))
        span.addLink(SpanContext(TraceId(1u, 3u), SpanId(3u)))

        span.end()

        val spans = exporter.getFinishedSpans().getOrThrow()
        assertEquals(1, spans.size)
        val s = spans[0]
        assertEquals(2, s.attributes.size)
        assertEquals(1u, s.droppedAttributesCount)
        assertEquals(2, s.events.events.size)
        assertEquals(1u, s.events.droppedCount)
        assertEquals(2, s.links.links.size)
        assertEquals(1u, s.links.droppedCount)
    }

    @Test
    fun testBatchSpanProcessor() {
        val exporter = InMemorySpanExporter.defaultExporter()
        val batchConfig =
            BatchConfig(
                maxQueueSize = 100,
                maxExportBatchSize = 2,
            )
        val provider =
            SdkTracerProvider
                .builder()
                .withBatchExporter(exporter, batchConfig)
                .build()

        val tracer = provider.tracer("batch_tracer")
        val span1 = tracer.start("span1")
        span1.end()

        // Batch size is 2, so 1 span is still buffered
        assertEquals(0, exporter.getFinishedSpans().getOrThrow().size)

        val span2 = tracer.start("span2")
        span2.end()

        // Reached batch size 2, so exported immediately
        assertEquals(2, exporter.getFinishedSpans().getOrThrow().size)

        val span3 = tracer.start("span3")
        span3.end()

        // Force flush flushes remaining
        provider.forceFlush()
        assertEquals(3, exporter.getFinishedSpans().getOrThrow().size)
    }
}
