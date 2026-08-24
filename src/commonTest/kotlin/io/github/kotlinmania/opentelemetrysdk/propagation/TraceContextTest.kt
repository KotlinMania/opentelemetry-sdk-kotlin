// port-lint: tests propagation/trace_context.rs
package io.github.kotlinmania.opentelemetrysdk.propagation

import io.github.kotlinmania.opentelemetrysdk.Context
import io.github.kotlinmania.opentelemetrysdk.trace.SpanContext
import io.github.kotlinmania.opentelemetrysdk.trace.SpanId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceFlags
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceState
import io.github.kotlinmania.opentelemetrysdk.trace.TraceStateEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TraceContextTest {

    private class MapCarrier(
        private val map: MutableMap<String, String> = mutableMapOf(),
    ) : Extractor, Injector {
        override fun get(key: String): String? = map[key]
        override fun keys(): List<String> = map.keys.toList()
        override fun set(key: String, value: String) {
            map[key] = value
        }
    }

    @Test
    fun extractW3c() {
        val propagator = TraceContextPropagator()

        val traceParent = "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01"
        val traceState = "foo=bar"
        val carrier = MapCarrier(mutableMapOf(
            TraceContextPropagator.TRACEPARENT_HEADER to traceParent,
            TraceContextPropagator.TRACESTATE_HEADER to traceState,
        ))

        val context = propagator.extract(carrier)
        val spanContext = context.spanContext()
        assertTrue(spanContext != null)
        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", spanContext.traceId.toHexString())
        assertEquals("00f067aa0ba902b7", spanContext.spanId.toHexString())
        assertTrue(spanContext.isSampled)
        assertEquals("bar", spanContext.traceState.get("foo"))
    }

    @Test
    fun extractW3cTraceState() {
        val propagator = TraceContextPropagator()
        val carrier = MapCarrier(mutableMapOf(
            TraceContextPropagator.TRACEPARENT_HEADER to "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00",
            TraceContextPropagator.TRACESTATE_HEADER to "foo=bar,baz=qux",
        ))

        val context = propagator.extract(carrier)
        val spanContext = context.spanContext()
        assertTrue(spanContext != null)
        assertEquals("bar", spanContext.traceState.get("foo"))
        assertEquals("qux", spanContext.traceState.get("baz"))
    }

    @Test
    fun extractW3cRejectInvalid() {
        val propagator = TraceContextPropagator()
        val invalidParents = listOf(
            "0000-00000000000000000000000000000000-0000000000000000-01",
            "00-ab00000000000000000000000000000000-cd00000000000000-01",
            "00-ab000000000000000000000000000000-cd0000000000000000-01",
            "00-ab000000000000000000000000000000-cd00000000000000-0100",
            "qw-00000000000000000000000000000000-0000000000000000-01",
            "00-qw000000000000000000000000000000-cd00000000000000-01",
            "00-ab000000000000000000000000000000-qw00000000000000-01",
            "00-ab000000000000000000000000000000-cd00000000000000-qw",
            "A0-00000000000000000000000000000000-0000000000000000-01",
            "00-AB000000000000000000000000000000-cd00000000000000-01",
            "00-ab000000000000000000000000000000-CD00000000000000-01",
            "00-ab000000000000000000000000000000-cd00000000000000-A1",
            "00-00000000000000000000000000000000-0000000000000000-01",
            "00-ab000000000000000000000000000000-cd00000000000000-09",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-",
        )

        for (invalid in invalidParents) {
            val carrier = MapCarrier(mutableMapOf(TraceContextPropagator.TRACEPARENT_HEADER to invalid))
            val spanContext = propagator.extractSpanContext(carrier)
            assertNull(spanContext, "Expected $invalid to be rejected")
        }
    }

    @Test
    fun injectW3c() {
        val propagator = TraceContextPropagator()
        val traceId = TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736")
        val spanId = SpanId.fromHexString("00f067aa0ba902b7")
        val spanContext = SpanContext.new(
            traceId,
            spanId,
            TraceFlags.SAMPLED,
            isRemote = true,
            traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
        )

        val context = Context().withRemoteSpanContext(spanContext)
        val carrier = MapCarrier()
        propagator.injectContext(context, carrier)

        assertEquals("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01", carrier.get(TraceContextPropagator.TRACEPARENT_HEADER))
        assertEquals("foo=bar", carrier.get(TraceContextPropagator.TRACESTATE_HEADER))
    }

    @Test
    fun injectW3cTraceState() {
        val propagator = TraceContextPropagator()
        val traceId = TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736")
        val spanId = SpanId.fromHexString("00f067aa0ba902b7")
        val spanContext = SpanContext.new(
            traceId,
            spanId,
            TraceFlags.DEFAULT,
            isRemote = true,
            traceState = TraceState(listOf(TraceStateEntry("a", "1"), TraceStateEntry("b", "2"))),
        )

        val context = Context().withRemoteSpanContext(spanContext)
        val carrier = MapCarrier()
        propagator.injectContext(context, carrier)

        assertEquals("00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00", carrier.get(TraceContextPropagator.TRACEPARENT_HEADER))
        assertEquals("a=1,b=2", carrier.get(TraceContextPropagator.TRACESTATE_HEADER))
    }
}
