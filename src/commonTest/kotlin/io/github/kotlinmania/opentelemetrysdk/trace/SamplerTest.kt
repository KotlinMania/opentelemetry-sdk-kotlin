// port-lint: source trace/sampler.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import kotlin.test.Test
import kotlin.test.assertEquals

class SamplerTest {
    @Test
    fun testAlwaysOnAndAlwaysOff() {
        val onResult =
            Sampler.AlwaysOn.shouldSample(
                parentContext = null,
                traceId = TraceId(1u, 1u),
                name = "test",
                spanKind = SpanKind.INTERNAL,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RECORD_AND_SAMPLE, onResult.decision)

        val offResult =
            Sampler.AlwaysOff.shouldSample(
                parentContext = null,
                traceId = TraceId(1u, 1u),
                name = "test",
                spanKind = SpanKind.INTERNAL,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.DROP, offResult.decision)
    }

    @Test
    fun testParentBasedSampler() {
        val parentSampler = Sampler.ParentBased(Sampler.AlwaysOn)

        // No parent: delegates to AlwaysOn
        val noParentResult =
            parentSampler.shouldSample(
                parentContext = null,
                traceId = TraceId(1u, 1u),
                name = "test",
                spanKind = SpanKind.INTERNAL,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RECORD_AND_SAMPLE, noParentResult.decision)

        // Sampled parent: should sample
        val sampledParent =
            SpanContext(
                traceId = TraceId(1u, 1u),
                spanId = SpanId(1u),
                traceFlags = TraceFlags.SAMPLED,
            )
        val sampledResult =
            parentSampler.shouldSample(
                parentContext = sampledParent,
                traceId = TraceId(1u, 1u),
                name = "test",
                spanKind = SpanKind.INTERNAL,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RECORD_AND_SAMPLE, sampledResult.decision)

        // Unsampled parent: should drop
        val unsampledParent =
            SpanContext(
                traceId = TraceId(1u, 1u),
                spanId = SpanId(1u),
                traceFlags = TraceFlags.DEFAULT,
            )
        val unsampledResult =
            parentSampler.shouldSample(
                parentContext = unsampledParent,
                traceId = TraceId(1u, 1u),
                name = "test",
                spanKind = SpanKind.INTERNAL,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.DROP, unsampledResult.decision)
    }

    @Test
    fun testTraceIdRatioBased() {
        val samplerOn = Sampler.TraceIdRatioBased(1.0)
        val resultOn =
            samplerOn.shouldSample(
                parentContext = null,
                traceId = TraceId(0u, 100u),
                name = "test",
                spanKind = SpanKind.INTERNAL,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RECORD_AND_SAMPLE, resultOn.decision)

        val samplerOff = Sampler.TraceIdRatioBased(0.0)
        val resultOff =
            samplerOff.shouldSample(
                parentContext = null,
                traceId = TraceId(0u, 100u),
                name = "test",
                spanKind = SpanKind.INTERNAL,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.DROP, resultOff.decision)
    }
}
