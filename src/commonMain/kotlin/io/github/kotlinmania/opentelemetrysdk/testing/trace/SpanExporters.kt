// port-lint: source testing/trace/span_exporters.rs
package io.github.kotlinmania.opentelemetrysdk.testing.trace

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.trace.SpanContext
import io.github.kotlinmania.opentelemetrysdk.trace.SpanData
import io.github.kotlinmania.opentelemetrysdk.trace.SpanEvents
import io.github.kotlinmania.opentelemetrysdk.trace.SpanExporter
import io.github.kotlinmania.opentelemetrysdk.trace.SpanId
import io.github.kotlinmania.opentelemetrysdk.trace.SpanKind
import io.github.kotlinmania.opentelemetrysdk.trace.SpanLinks
import io.github.kotlinmania.opentelemetrysdk.trace.Status
import io.github.kotlinmania.opentelemetrysdk.trace.TraceFlags
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.time.Clock
import kotlin.time.Duration

/**
 * Creates new span data for testing.
 */
public fun newTestExportSpanData(): SpanData {
    val now = Clock.System.now()
    return SpanData(
        spanContext =
            SpanContext(
                traceId = TraceId.from(1uL),
                spanId = SpanId.from(1uL),
                traceFlags = TraceFlags.SAMPLED,
                isRemote = false,
                traceState = TraceState.DEFAULT,
            ),
        parentSpanId = SpanId.INVALID,
        parentSpanIsRemote = false,
        spanKind = SpanKind.INTERNAL,
        name = "opentelemetry",
        startTime = now,
        endTime = now,
        attributes = emptyList(),
        droppedAttributesCount = 0u,
        events = SpanEvents.EMPTY,
        links = SpanLinks.EMPTY,
        status = Status.Unset,
        instrumentationScope = InstrumentationScope.EMPTY,
    )
}

/**
 * Span exporter implementation that forwards spans to a coroutine channel.
 */
internal class TokioSpanExporter(
    private val txExport: SendChannel<SpanData>,
    private val txShutdown: SendChannel<Unit>,
) : SpanExporter {
    override fun export(batch: List<SpanData>): OTelSdkResult {
        for (span in batch) {
            val res = txExport.trySend(span)
            if (res.isFailure) {
                return Result.failure(Exception("Export failed: channel send error"))
            }
        }
        return Result.success(Unit)
    }

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        val res = txShutdown.trySend(Unit)
        return if (res.isSuccess) {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Failed to send shutdown signal"))
        }
    }
}

/**
 * Creates a test exporter with export and shutdown receiver channels.
 */
internal fun newTokioTestExporter(): Triple<SpanExporter, ReceiveChannel<SpanData>, ReceiveChannel<Unit>> {
    val exportChannel = Channel<SpanData>(Channel.UNLIMITED)
    val shutdownChannel = Channel<Unit>(Channel.UNLIMITED)
    val exporter = TokioSpanExporter(exportChannel, shutdownChannel)
    return Triple(exporter, exportChannel, shutdownChannel)
}

/**
 * A no-op instance of [SpanExporter].
 */
public class NoopSpanExporter : SpanExporter {
    override fun export(batch: List<SpanData>): OTelSdkResult = Result.success(Unit)

    public companion object {
        public fun new(): NoopSpanExporter = NoopSpanExporter()
    }
}
