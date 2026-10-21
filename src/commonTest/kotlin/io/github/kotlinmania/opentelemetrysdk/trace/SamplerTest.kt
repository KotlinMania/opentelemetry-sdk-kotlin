// port-lint: tests trace/sampler.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetry.SpanId
import io.github.kotlinmania.opentelemetry.TraceFlags
import io.github.kotlinmania.opentelemetry.TraceId
import io.github.kotlinmania.opentelemetry.trace.SamplingDecision
import io.github.kotlinmania.opentelemetry.trace.SpanContext
import io.github.kotlinmania.opentelemetry.trace.SpanKind
import kotlin.test.Test
import kotlin.test.assertEquals

class SamplerTest {
    @Test
    fun testAlwaysOnAndAlwaysOff() {
        val onResult =
            Sampler.AlwaysOn.shouldSample(
                parentContext = null,
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                name = "test",
                spanKind = SpanKind.Internal,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RecordAndSample, onResult.decision)

        val offResult =
            Sampler.AlwaysOff.shouldSample(
                parentContext = null,
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                name = "test",
                spanKind = SpanKind.Internal,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.Drop, offResult.decision)
    }

    @Test
    fun testParentBasedSampler() {
        val parentSampler = Sampler.ParentBased(Sampler.AlwaysOn)

        // No parent: delegates to AlwaysOn
        val noParentResult =
            parentSampler.shouldSample(
                parentContext = null,
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                name = "test",
                spanKind = SpanKind.Internal,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RecordAndSample, noParentResult.decision)

        // Sampled parent: should sample
        val sampledParent =
            SpanContext(
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                spanId = SpanId.from(1L),
                traceFlags = TraceFlags.SAMPLED,
            )
        val sampledResult =
            parentSampler.shouldSample(
                parentContext = sampledParent,
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                name = "test",
                spanKind = SpanKind.Internal,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RecordAndSample, sampledResult.decision)

        // Unsampled parent: should drop
        val unsampledParent =
            SpanContext(
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                spanId = SpanId.from(1L),
                traceFlags = TraceFlags.NOT_SAMPLED,
            )
        val unsampledResult =
            parentSampler.shouldSample(
                parentContext = unsampledParent,
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                name = "test",
                spanKind = SpanKind.Internal,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.Drop, unsampledResult.decision)
    }

    @Test
    fun testTraceIdRatioBased() {
        val samplerOn = Sampler.TraceIdRatioBased(1.0)
        val resultOn =
            samplerOn.shouldSample(
                parentContext = null,
                traceId = TraceId.fromHex("00000000000000000000000000000064"),
                name = "test",
                spanKind = SpanKind.Internal,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.RecordAndSample, resultOn.decision)

        val samplerOff = Sampler.TraceIdRatioBased(0.0)
        val resultOff =
            samplerOff.shouldSample(
                parentContext = null,
                traceId = TraceId.fromHex("00000000000000000000000000000064"),
                name = "test",
                spanKind = SpanKind.Internal,
                attributes = emptyList(),
                links = emptyList(),
            )
        assertEquals(SamplingDecision.Drop, resultOff.decision)
    }
}
