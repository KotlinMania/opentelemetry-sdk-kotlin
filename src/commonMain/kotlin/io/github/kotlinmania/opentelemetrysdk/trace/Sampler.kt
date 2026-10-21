// port-lint: source trace/sampler.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetry.KeyValue
import io.github.kotlinmania.opentelemetry.TraceId
import io.github.kotlinmania.opentelemetry.trace.Link
import io.github.kotlinmania.opentelemetry.trace.SamplingDecision
import io.github.kotlinmania.opentelemetry.trace.SamplingResult
import io.github.kotlinmania.opentelemetry.trace.SpanContext
import io.github.kotlinmania.opentelemetry.trace.SpanKind
import io.github.kotlinmania.opentelemetry.trace.TraceState

/**
 * The [ShouldSample] interface allows implementations to provide samplers
 * which will return a [SamplingResult] based on information available just before
 * the span was created.
 */
public interface ShouldSample {
    /**
     * Determines whether a span should be sampled.
     */
    public fun shouldSample(
        parentContext: SpanContext?,
        traceId: TraceId,
        name: String,
        spanKind: SpanKind,
        attributes: List<KeyValue>,
        links: List<Link>,
    ): SamplingResult
}

/**
 * Default sampling options.
 */
public sealed class Sampler : ShouldSample {
    /**
     * Always samples every span.
     */
    public data object AlwaysOn : Sampler() {
        override fun shouldSample(
            parentContext: SpanContext?,
            traceId: TraceId,
            name: String,
            spanKind: SpanKind,
            attributes: List<KeyValue>,
            links: List<Link>,
        ): SamplingResult =
            SamplingResult(
                decision = SamplingDecision.RecordAndSample,
                traceState = parentContext?.traceState ?: TraceState.NONE,
            )
    }

    /**
     * Never samples any span.
     */
    public data object AlwaysOff : Sampler() {
        override fun shouldSample(
            parentContext: SpanContext?,
            traceId: TraceId,
            name: String,
            spanKind: SpanKind,
            attributes: List<KeyValue>,
            links: List<Link>,
        ): SamplingResult =
            SamplingResult(
                decision = SamplingDecision.Drop,
                traceState = parentContext?.traceState ?: TraceState.NONE,
            )
    }

    /**
     * Samples based on the parent span context if present, otherwise delegates to another sampler.
     */
    public data class ParentBased(
        public val delegate: ShouldSample = AlwaysOn,
    ) : Sampler() {
        override fun shouldSample(
            parentContext: SpanContext?,
            traceId: TraceId,
            name: String,
            spanKind: SpanKind,
            attributes: List<KeyValue>,
            links: List<Link>,
        ): SamplingResult {
            if (parentContext != null && parentContext.isValid()) {
                val decision =
                    if (parentContext.traceFlags.isSampled()) {
                        SamplingDecision.RecordAndSample
                    } else {
                        SamplingDecision.Drop
                    }
                return SamplingResult(
                    decision = decision,
                    traceState = parentContext.traceState,
                )
            }
            return delegate.shouldSample(parentContext, traceId, name, spanKind, attributes, links)
        }
    }

    /**
     * Samples a given ratio of spans based on the trace ID.
     */
    public data class TraceIdRatioBased(
        public val ratio: Double,
    ) : Sampler() {
        override fun shouldSample(
            parentContext: SpanContext?,
            traceId: TraceId,
            name: String,
            spanKind: SpanKind,
            attributes: List<KeyValue>,
            links: List<Link>,
        ): SamplingResult {
            val decision = sampleBasedOnProbability(ratio, traceId)
            return SamplingResult(
                decision = decision,
                traceState = parentContext?.traceState ?: TraceState.NONE,
            )
        }

        public companion object {
            /**
             * Determines sampling decision for a given probability and trace ID.
             */
            public fun sampleBasedOnProbability(prob: Double, traceId: TraceId): SamplingDecision {
                if (prob >= 1.0) {
                    return SamplingDecision.RecordAndSample
                }
                if (prob <= 0.0) {
                    return SamplingDecision.Drop
                }
                val probUpperBound = (prob * 9223372036854775807.0).toLong().toULong()
                val bytes = traceId.toBytes()
                var traceIdLow = 0uL
                for (i in 8 until 16) {
                    traceIdLow = (traceIdLow shl 8) or (bytes[i].toInt() and 0xFF).toULong()
                }
                val rnd = traceIdLow shr 1
                return if (rnd < probUpperBound) {
                    SamplingDecision.RecordAndSample
                } else {
                    SamplingDecision.Drop
                }
            }
        }
    }

    public companion object {
        public fun alwaysOn(): Sampler = AlwaysOn

        public fun alwaysOff(): Sampler = AlwaysOff

        public fun parentBased(delegate: ShouldSample = AlwaysOn): Sampler = ParentBased(delegate)

        public fun traceIdRatioBased(ratio: Double): Sampler = TraceIdRatioBased(ratio)
    }
}
