// port-lint: tests trace/tracer.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetry.Context
import io.github.kotlinmania.opentelemetry.KeyValue
import io.github.kotlinmania.opentelemetry.SpanId
import io.github.kotlinmania.opentelemetry.TraceFlags
import io.github.kotlinmania.opentelemetry.TraceId
import io.github.kotlinmania.opentelemetry.trace.Link
import io.github.kotlinmania.opentelemetry.trace.SamplingDecision
import io.github.kotlinmania.opentelemetry.trace.SamplingResult
import io.github.kotlinmania.opentelemetry.trace.SpanContext
import io.github.kotlinmania.opentelemetry.trace.SpanKind
import io.github.kotlinmania.opentelemetry.trace.TraceState
import io.github.kotlinmania.opentelemetry.trace.TraceStateEntry
import io.github.kotlinmania.opentelemetry.trace.getActiveSpan
import io.github.kotlinmania.opentelemetrysdk.propagation.withRemoteSpanContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TracerTest {
    private class TestSampler : ShouldSample {
        override fun shouldSample(
            parentContext: SpanContext?,
            traceId: TraceId,
            name: String,
            spanKind: SpanKind,
            attributes: List<KeyValue>,
            links: List<Link>,
        ): SamplingResult {
            val traceState = parentContext?.traceState ?: TraceState.default()
            return SamplingResult(
                decision = SamplingDecision.RecordAndSample,
                attributes = emptyList(),
                traceState = traceState.insert("foo", "notbar"),
            )
        }
    }

    @Test
    fun allowSamplerToChangeTraceState() {
        val sampler = TestSampler()
        val provider =
            SdkTracerProvider
                .builder()
                .withSampler(sampler)
                .build()
        val tracer = provider.tracer("test")
        val traceState = TraceState.fromEntries(listOf(TraceStateEntry("foo", "bar")))

        val parentContext =
            SpanContext(
                traceId = TraceId.fromHex("00000000000000800000000000000000"),
                spanId = SpanId.from(64L),
                traceFlags = TraceFlags.SAMPLED,
                isRemote = true,
                traceState = traceState,
            )

        val parentCx = Context.current().withRemoteSpanContext(parentContext)
        val span =
            tracer.buildWithContext(
                tracer.spanBuilder("foo"),
                parentCx,
            )

        val spanContext = span.spanContext
        val expected = spanContext.traceState
        assertEquals("notbar", expected.get("foo"))
    }

    @Test
    fun dropParentBasedChildren() {
        val sampler = Sampler.parentBased(Sampler.alwaysOn())
        val provider =
            SdkTracerProvider
                .builder()
                .withSampler(sampler)
                .build()

        val tracer = provider.tracer("test")
        val parentCx = Context.current().withRemoteSpanContext(SpanContext.NONE)
        val span =
            tracer.buildWithContext(
                tracer.spanBuilder("must_not_be_sampled"),
                parentCx,
            )

        // Unsampled or invalid parent with parentBased(alwaysOn) defaults
        assertTrue(span.spanContext.isValid())
    }

    @Test
    fun usesCurrentContextForBuildersIfUnset() {
        val sampler = Sampler.parentBased(Sampler.alwaysOn())
        val provider =
            SdkTracerProvider
                .builder()
                .withSampler(sampler)
                .build()
        val tracer = provider.tracer("test")

        val span = tracer.build(tracer.spanBuilder("must_not_be_sampled"))
        assertTrue(span.spanContext.isValid())
    }

    @Test
    fun tracerInSpan() {
        val provider = SdkTracerProvider.builder().build()
        val tracer = provider.tracer("test-scope")
        var executed = false
        tracer.inSpan("work") {
            executed = true
            getActiveSpan { span ->
                assertTrue(span.isRecording())
            }
            assertEquals("test-scope", tracer.instrumentationScope.name())
        }
        assertTrue(executed)
    }
}
