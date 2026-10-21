// port-lint: source trace/tracer.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetry.Context
import io.github.kotlinmania.opentelemetry.InstrumentationScope
import io.github.kotlinmania.opentelemetry.KeyValue
import io.github.kotlinmania.opentelemetry.trace.SpanBuilder
import io.github.kotlinmania.opentelemetry.trace.SpanContext
import io.github.kotlinmania.opentelemetry.trace.Tracer
import io.github.kotlinmania.opentelemetry.trace.withSpan

/**
 * Tracer implementation to create and manage spans.
 */
public class SdkTracer internal constructor(
    public val instrumentationScope: InstrumentationScope,
    internal val provider: SdkTracerProvider,
) : Tracer {
    public fun provider(): SdkTracerProvider = provider

    public fun idGenerator(): IdGenerator = provider.config().idGenerator

    public fun shouldSample(): ShouldSample = provider.config().sampler

    override fun spanBuilder(name: String): SpanBuilder = SpanBuilder.fromName(name)

    override fun start(name: String): Span = Context.mapCurrent { cx -> startWithContext(name, cx) }

    override fun startWithContext(name: String, parentCx: Context): Span =
        buildWithContext(SpanBuilder.fromName(name), parentCx)

    override fun build(builder: SpanBuilder): Span =
        Context.mapCurrent { cx -> buildWithContext(builder, cx) }

    override fun buildWithContext(
        builder: SpanBuilder,
        parentCx: Context,
    ): Span {
        if (parentCx.isTelemetrySuppressed()) {
            return Span(
                spanContext = SpanContext.NONE,
                initialData = null,
                tracer = this,
                spanLimits = provider.config().spanLimits,
            )
        }
        return provider.startSpanFromBuilder(builder, this, parentCx)
    }

    override fun <T> inSpan(name: String, f: (Context) -> T): T {
        val span = start(name)
        val cx = Context.current().withSpan(span)
        val guard = cx.attach()
        return try {
            f(cx)
        } finally {
            span.end()
            guard.close()
        }
    }

    public companion object {
        public fun new(scope: InstrumentationScope, provider: SdkTracerProvider): SdkTracer =
            SdkTracer(scope, provider)
    }
}

public fun SpanBuilder.withAttribute(attribute: KeyValue): SpanBuilder =
    withAttributes(listOf(attribute))
