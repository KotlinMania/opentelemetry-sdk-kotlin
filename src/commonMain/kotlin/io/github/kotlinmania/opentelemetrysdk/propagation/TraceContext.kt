// port-lint: source propagation/trace_context.rs
package io.github.kotlinmania.opentelemetrysdk.propagation

import io.github.kotlinmania.opentelemetry.Context
import io.github.kotlinmania.opentelemetry.propagation.Extractor
import io.github.kotlinmania.opentelemetry.propagation.FieldIter
import io.github.kotlinmania.opentelemetry.propagation.Injector
import io.github.kotlinmania.opentelemetry.propagation.TextMapPropagator
import io.github.kotlinmania.opentelemetrysdk.trace.Span
import io.github.kotlinmania.opentelemetrysdk.trace.SpanContext
import io.github.kotlinmania.opentelemetrysdk.trace.SpanId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceFlags
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceState
import io.github.kotlinmania.opentelemetrysdk.trace.TraceStateEntry

/**
 * Extension helpers on [Context] for remote span contexts.
 */
public const val REMOTE_SPAN_CONTEXT_KEY: String = "opentelemetry.remote_span_context"
public const val SPAN_CONTEXT_KEY: String = "opentelemetry.active_span"

private data class RemoteSpanContextHolder(
    val spanContext: SpanContext,
)

private data class ActiveSpanHolder(
    val span: Span,
)

public fun Context.withRemoteSpanContext(spanContext: SpanContext): Context =
    withValue(RemoteSpanContextHolder(spanContext))

public fun Context.spanContext(): SpanContext? =
    get<RemoteSpanContextHolder>()?.spanContext
        ?: get<ActiveSpanHolder>()?.span?.spanContext

public fun Context.withSpan(span: Span): Context =
    withValue(ActiveSpanHolder(span))

/**
 * Propagates `SpanContext`s in W3C TraceContext format under `traceparent` and `tracestate` headers.
 */
public class TraceContextPropagator : TextMapPropagator {
    public fun extractSpanContext(extractor: Extractor): SpanContext? {
        val rawHeader = extractor.get(TRACEPARENT_HEADER)?.trim() ?: return null
        val headerValue = if (rawHeader.endsWith('-')) rawHeader.substring(0, rawHeader.length - 1) else rawHeader
        val parts = headerValue.split('-')
        if (parts.size < 4) return null

        val versionStr = parts[0]
        if (versionStr.length != 2) return null
        val version = versionStr.toIntOrNull(16) ?: return null
        if (version > MAX_VERSION) return null
        if (version == 0 && parts.size != 4) return null

        val traceIdStr = parts[1]
        if (traceIdStr.length != 32 || traceIdStr.any { it in 'A'..'Z' }) return null
        val traceId = TraceId.fromHexString(traceIdStr)
        if (traceId.isZero) return null

        val spanIdStr = parts[2]
        if (spanIdStr.length != 16 || spanIdStr.any { it in 'A'..'Z' }) return null
        val spanId = SpanId.fromHexString(spanIdStr)
        if (spanId.isZero) return null

        val optsStr = parts[3]
        if (optsStr.length != 2 || optsStr.any { it in 'A'..'Z' }) return null
        val opts = optsStr.toIntOrNull(16) ?: return null
        if (version == 0 && (opts > 2 || opts < 0)) return null

        val traceFlags = if ((opts and 1) != 0) TraceFlags.SAMPLED else TraceFlags.DEFAULT

        val traceStateStr = extractor.get(TRACESTATE_HEADER)
        val traceState =
            if (traceStateStr != null) {
                parseTraceState(traceStateStr)
            } else {
                TraceState.DEFAULT
            }

        val spanContext = SpanContext.new(traceId, spanId, traceFlags, isRemote = true, traceState = traceState)
        return if (spanContext.isValid) spanContext else null
    }

    override fun injectContext(cx: Context, injector: Injector) {
        val spanContext = cx.spanContext() ?: return
        if (spanContext.isValid) {
            val flags = if (spanContext.isSampled) 1 else 0
            val flagsHex = flags.toString(16).padStart(2, '0')
            val headerValue = "00-${spanContext.traceId.toHexString()}-${spanContext.spanId.toHexString()}-$flagsHex"
            injector.set(TRACEPARENT_HEADER, headerValue)
            val stateHeader = spanContext.traceState.entries.joinToString(",") { "${it.key}=${it.value}" }
            if (stateHeader.isNotEmpty()) {
                injector.set(TRACESTATE_HEADER, stateHeader)
            }
        }
    }

    override fun extractWithContext(cx: Context, extractor: Extractor): Context {
        val spanContext = extractSpanContext(extractor)
        return if (spanContext != null) {
            cx.withRemoteSpanContext(spanContext)
        } else {
            cx
        }
    }

    override fun fields(): FieldIter = FieldIter.new(listOf(TRACEPARENT_HEADER, TRACESTATE_HEADER))

    public companion object {
        public const val SUPPORTED_VERSION: Int = 0
        public const val MAX_VERSION: Int = 254
        public const val TRACEPARENT_HEADER: String = "traceparent"
        public const val TRACESTATE_HEADER: String = "tracestate"

        public fun new(): TraceContextPropagator = TraceContextPropagator()

        public fun default(): TraceContextPropagator = new()

        public fun traceContextHeaderFields(): List<String> = listOf(TRACEPARENT_HEADER, TRACESTATE_HEADER)

        private fun parseTraceState(traceStateStr: String): TraceState {
            if (traceStateStr.isBlank()) return TraceState.DEFAULT
            val entries =
                traceStateStr.split(',').mapNotNull { part ->
                    val trimmed = part.trim()
                    val eq = trimmed.indexOf('=')
                    if (eq > 0) {
                        val key = trimmed.substring(0, eq).trim()
                        val value = trimmed.substring(eq + 1).trim()
                        TraceStateEntry(key, value)
                    } else {
                        null
                    }
                }
            return TraceState(entries)
        }
    }
}
