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
    ) : Extractor,
        Injector {
        override fun get(key: String): String? = map[key]

        override fun keys(): List<String> = map.keys.toList()

        override fun set(key: String, value: String) {
            map[key] = value
        }
    }

    private fun extractData(): List<Triple<String, String, SpanContext>> =
        listOf(
            Triple(
                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.DEFAULT,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.SAMPLED,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "02-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.SAMPLED,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "02-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-09",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.SAMPLED,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "02-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-08",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.DEFAULT,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "02-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-09-XYZxsf09",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.SAMPLED,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01-",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.SAMPLED,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "01-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-09-",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.SAMPLED,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
        )

    private fun extractDataInvalid(): List<Pair<String, String>> =
        listOf(
            "0000-00000000000000000000000000000000-0000000000000000-01" to "wrong version length",
            "00-ab00000000000000000000000000000000-cd00000000000000-01" to "wrong trace ID length",
            "00-ab000000000000000000000000000000-cd0000000000000000-01" to "wrong span ID length",
            "00-ab000000000000000000000000000000-cd00000000000000-0100" to "wrong trace flag length",
            "qw-00000000000000000000000000000000-0000000000000000-01" to "bogus version",
            "00-qw000000000000000000000000000000-cd00000000000000-01" to "bogus trace ID",
            "00-ab000000000000000000000000000000-qw00000000000000-01" to "bogus span ID",
            "00-ab000000000000000000000000000000-cd00000000000000-qw" to "bogus trace flag",
            "A0-00000000000000000000000000000000-0000000000000000-01" to "upper case version",
            "00-AB000000000000000000000000000000-cd00000000000000-01" to "upper case trace ID",
            "00-ab000000000000000000000000000000-CD00000000000000-01" to "upper case span ID",
            "00-ab000000000000000000000000000000-cd00000000000000-A1" to "upper case trace flag",
            "00-00000000000000000000000000000000-0000000000000000-01" to "zero trace ID and span ID",
            "00-ab000000000000000000000000000000-cd00000000000000-09" to "trace-flag unused bits set",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7" to "missing options",
            "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-" to "empty options",
        )

    private fun injectData(): List<Triple<String, String, SpanContext>> =
        listOf(
            Triple(
                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.SAMPLED,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags.DEFAULT,
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01",
                "foo=bar",
                SpanContext.new(
                    TraceId.fromHexString("4bf92f3577b34da6a3ce929d0e0e4736"),
                    SpanId.fromHexString("00f067aa0ba902b7"),
                    TraceFlags(0xffu),
                    isRemote = true,
                    traceState = TraceState(listOf(TraceStateEntry("foo", "bar"))),
                ),
            ),
            Triple(
                "",
                "",
                SpanContext.EMPTY,
            ),
        )

    @Test
    fun extractW3c() {
        val propagator = TraceContextPropagator()

        for ((traceParent, traceState, expectedContext) in extractData()) {
            val carrier =
                MapCarrier(
                    mutableMapOf(
                        TraceContextPropagator.TRACEPARENT_HEADER to traceParent,
                        TraceContextPropagator.TRACESTATE_HEADER to traceState,
                    ),
                )

            val context = propagator.extract(carrier)
            val spanContext = context.spanContext()
            assertEquals(expectedContext, spanContext)
        }
    }

    @Test
    fun extractW3cTraceState() {
        val propagator = TraceContextPropagator()
        val carrier =
            MapCarrier(
                mutableMapOf(
                    TraceContextPropagator.TRACEPARENT_HEADER to "00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-00",
                    TraceContextPropagator.TRACESTATE_HEADER to "foo=bar",
                ),
            )

        val context = propagator.extract(carrier)
        val spanContext = context.spanContext()
        assertTrue(spanContext != null)
        assertEquals("bar", spanContext.traceState.get("foo"))
    }

    @Test
    fun extractW3cRejectInvalid() {
        val propagator = TraceContextPropagator()

        for ((invalidHeader, reason) in extractDataInvalid()) {
            val carrier = MapCarrier(mutableMapOf(TraceContextPropagator.TRACEPARENT_HEADER to invalidHeader))
            val spanContext = propagator.extractSpanContext(carrier)
            assertNull(spanContext, "Expected $invalidHeader to be rejected because: $reason")
        }
    }

    @Test
    fun injectW3c() {
        val propagator = TraceContextPropagator()

        for ((expectedTraceParent, expectedTraceState, spanContext) in injectData()) {
            val context = Context().withRemoteSpanContext(spanContext)
            val carrier = MapCarrier()
            propagator.injectContext(context, carrier)

            assertEquals(expectedTraceParent, carrier.get(TraceContextPropagator.TRACEPARENT_HEADER) ?: "")
            assertEquals(expectedTraceState, carrier.get(TraceContextPropagator.TRACESTATE_HEADER) ?: "")
        }
    }

    @Test
    fun injectW3cTraceState() {
        val propagator = TraceContextPropagator()
        val state = "foo=bar"
        val carrier = MapCarrier(mutableMapOf(TraceContextPropagator.TRACESTATE_HEADER to state))
        val context = Context()
        propagator.injectContext(context, carrier)
        assertEquals(state, carrier.get(TraceContextPropagator.TRACESTATE_HEADER))
    }
}
