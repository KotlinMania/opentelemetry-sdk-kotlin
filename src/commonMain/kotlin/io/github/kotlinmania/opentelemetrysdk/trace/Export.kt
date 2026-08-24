// port-lint: source trace/export.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Span data contains all the information collected by a Span and can be used
 * by exporters as a standard input.
 */
public data class SpanData(
    public val spanContext: SpanContext,
    public val parentSpanId: SpanId = SpanId.INVALID,
    public val parentSpanIsRemote: Boolean = false,
    public val spanKind: SpanKind = SpanKind.INTERNAL,
    public val name: String,
    public val startTime: Instant,
    public val endTime: Instant,
    public val attributes: List<KeyValue> = emptyList(),
    public val droppedAttributesCount: UInt = 0u,
    public val events: SpanEvents = SpanEvents.EMPTY,
    public val links: SpanLinks = SpanLinks.EMPTY,
    public val status: Status = Status.Unset,
    public val instrumentationScope: InstrumentationScope = InstrumentationScope.EMPTY,
)

/**
 * SpanExporter defines the interface that protocol-specific exporters must implement.
 */
public interface SpanExporter {
    /**
     * Exports a batch of readable spans.
     */
    public fun export(batch: List<SpanData>): OTelSdkResult

    /**
     * Shuts down the exporter with timeout.
     */
    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

    /**
     * Shuts down the exporter with default timeout.
     */
    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    /**
     * Force the export of any spans prior to this call.
     */
    public fun forceFlush(): OTelSdkResult = Result.success(Unit)

    /**
     * Sets the resource for the exporter.
     */
    public fun setResource(resource: Resource) {}
}
