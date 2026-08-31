// port-lint: source opentelemetry_sdk/src/trace/sampler/jaeger_remote/sampler.rs
package io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote

import io.github.kotlinmania.opentelemetrysdk.RuntimeChannel
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.trace.Link
import io.github.kotlinmania.opentelemetrysdk.trace.Sampler
import io.github.kotlinmania.opentelemetrysdk.trace.SamplingResult
import io.github.kotlinmania.opentelemetrysdk.trace.ShouldSample
import io.github.kotlinmania.opentelemetrysdk.trace.SpanContext
import io.github.kotlinmania.opentelemetrysdk.trace.SpanKind
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

public const val DEFAULT_REMOTE_SAMPLER_ENDPOINT: String = "http://localhost:5778/sampling"

/**
 * Sampler that fetches the sampling configuration from remotes.
 */
public class JaegerRemoteSampler internal constructor(
    internal val inner: Inner,
    public val defaultSampler: ShouldSample,
) : ShouldSample {
    override fun shouldSample(
        parentContext: SpanContext?,
        traceId: TraceId,
        name: String,
        spanKind: SpanKind,
        attributes: List<KeyValue>,
        links: List<Link>,
    ): SamplingResult {
        val res = inner.shouldSample(parentContext, traceId, name)
        return res ?: defaultSampler.shouldSample(
            parentContext,
            traceId,
            name,
            spanKind,
            attributes,
            links,
        )
    }

    public companion object {
        public fun <R : RuntimeChannel> builder(
            runtime: R,
            serviceName: String,
        ): JaegerRemoteSamplerBuilder<R> = JaegerRemoteSamplerBuilder(runtime, serviceName)
    }
}

/**
 * Builder for [JaegerRemoteSampler].
 */
public class JaegerRemoteSamplerBuilder<R : RuntimeChannel>(
    public val runtime: R,
    public val serviceName: String,
) {
    public var updateInterval: Duration = 5.minutes
    public var endpoint: String = DEFAULT_REMOTE_SAMPLER_ENDPOINT
    public var defaultSampler: ShouldSample = Sampler.AlwaysOn
    public var leakyBucketSize: Double = 100.0

    public fun withUpdateInterval(interval: Duration): JaegerRemoteSamplerBuilder<R> {
        this.updateInterval = interval
        return this
    }

    public fun withEndpoint(endpoint: String): JaegerRemoteSamplerBuilder<R> {
        this.endpoint = endpoint
        return this
    }

    public fun withDefaultSampler(sampler: ShouldSample): JaegerRemoteSamplerBuilder<R> {
        this.defaultSampler = sampler
        return this
    }

    public fun withLeakyBucketSize(size: Double): JaegerRemoteSamplerBuilder<R> {
        this.leakyBucketSize = size
        return this
    }

    public fun build(): JaegerRemoteSampler {
        val inner = Inner(leakyBucketSize = leakyBucketSize)
        return JaegerRemoteSampler(inner, defaultSampler)
    }
}
