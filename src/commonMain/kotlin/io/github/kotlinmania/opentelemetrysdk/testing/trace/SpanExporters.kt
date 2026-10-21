// port-lint: source testing/trace/span_exporters.rs
package io.github.kotlinmania.opentelemetrysdk.testing.trace

import io.github.kotlinmania.opentelemetry.InstrumentationScope
import io.github.kotlinmania.opentelemetry.SpanId
import io.github.kotlinmania.opentelemetry.TraceFlags
import io.github.kotlinmania.opentelemetry.TraceId
import io.github.kotlinmania.opentelemetry.trace.SpanContext
import io.github.kotlinmania.opentelemetry.trace.SpanKind
import io.github.kotlinmania.opentelemetry.trace.Status
import io.github.kotlinmania.opentelemetry.trace.TraceState
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.trace.SpanData
import io.github.kotlinmania.opentelemetrysdk.trace.SpanEvents
import io.github.kotlinmania.opentelemetrysdk.trace.SpanExporter
import io.github.kotlinmania.opentelemetrysdk.trace.SpanLinks
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
                traceId = TraceId.fromHex("00000000000000000000000000000001"),
                spanId = SpanId.from(1L),
                traceFlags = TraceFlags.SAMPLED,
                isRemote = false,
                traceState = TraceState.default(),
            ),
        parentSpanId = SpanId.INVALID,
        parentSpanIsRemote = false,
        spanKind = SpanKind.Internal,
        name = "opentelemetry",
        startTime = now,
        endTime = now,
        attributes = emptyList(),
        droppedAttributesCount = 0u,
        events = SpanEvents.EMPTY,
        links = SpanLinks.EMPTY,
        status = Status.Unset,
        instrumentationScope = InstrumentationScope.builder("").build(),
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
