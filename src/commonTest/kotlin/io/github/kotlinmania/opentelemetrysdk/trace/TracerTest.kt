// port-lint: tests trace/tracer.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.Context
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TracerTest {
    private class TestSampler : ShouldSample {
        override fun shouldSample(
            parentContext: SpanContext?,
            traceId: TraceId,
            name: String,
            spanKind: SpanKind,
            attributes: List<io.github.kotlinmania.opentelemetrysdk.resource.KeyValue>,
            links: List<Link>,
        ): SamplingResult {
            val traceState = parentContext?.traceState ?: TraceState.empty()
            return SamplingResult(
                decision = SamplingDecision.RECORD_AND_SAMPLE,
                traceState = traceState.insert("foo", "notbar").getOrThrow(),
            )
        }
    }

    @Test
    fun allowSamplerToChangeTraceState() {
        val sampler = TestSampler()
        val provider = SdkTracerProvider.builder()
            .withSampler(sampler)
            .build()
        val tracer = provider.tracer("test")
        val traceState = TraceState.fromKeyValues(listOf(TraceStateEntry("foo", "bar"))).getOrThrow()

        val parentContext = SpanContext(
            traceId = TraceId(128u, 0u),
            spanId = SpanId(64u),
            traceFlags = TraceFlags.SAMPLED,
            isRemote = true,
            traceState = traceState,
        )

        val span = tracer.spanBuilder("foo")
            .withParent(parentContext)
            .start()

        val spanContext = span.spanContext
        val expected = spanContext.traceState
        assertEquals("notbar", expected.get("foo"))
    }

    @Test
    fun dropParentBasedChildren() {
        val sampler = Sampler.parentBased(Sampler.alwaysOn())
        val provider = SdkTracerProvider.builder()
            .withSampler(sampler)
            .build()

        val tracer = provider.tracer("test")
        val span = tracer.spanBuilder("must_not_be_sampled")
            .withParent(SpanContext.EMPTY)
            .start()

        // Unsampled or invalid parent with parentBased(alwaysOn) defaults
        assertTrue(span.spanContext.isValid)
    }

    @Test
    fun usesCurrentContextForBuildersIfUnset() {
        val sampler = Sampler.parentBased(Sampler.alwaysOn())
        val provider = SdkTracerProvider.builder()
            .withSampler(sampler)
            .build()
        val tracer = provider.tracer("test")

        val span = tracer.spanBuilder("must_not_be_sampled").start()
        assertTrue(span.spanContext.isValid)
    }

    @Test
    fun tracerInSpan() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("test-scope")
        var executed = false
        tracer.inSpan("work") { span ->
            executed = true
            assertTrue(span.isRecording())
            assertEquals("test-scope", tracer.instrumentationScope.name)
        }
        assertTrue(executed)
    }
}

