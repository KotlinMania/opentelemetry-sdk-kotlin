package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.Tokio
import io.github.kotlinmania.opentelemetrysdk.testing.trace.newTestExportSpanData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class AsyncBatchSpanProcessorTest {
    @Test
    fun testAsyncBatchSpanProcessor() {
        val exporter = InMemorySpanExporter()
        val runtime = Tokio()
        val processor =
            AsyncBatchSpanProcessor
                .builder(exporter, runtime)
                .withBatchConfig(BatchConfig(maxExportBatchSize = 1))
                .build()

        val span = newTestExportSpanData()
        processor.onEnd(span)

        assertTrue(processor.forceFlush().isSuccess)
        assertTrue(processor.shutdownWithTimeout(1.seconds).isSuccess)
    }

    @Test
    fun testSpanCountExporter() {
        val exporter = SpanCountExporter.new()
        assertEquals(0, exporter.spanCount.load())

        val span = newTestExportSpanData()
        val exportRes = exporter.export(listOf(span, span))
        assertTrue(exportRes.isSuccess)
        assertEquals(2, exporter.spanCount.load())
    }
}
