package io.github.kotlinmania.opentelemetrysdk.testing

import io.github.kotlinmania.opentelemetrysdk.testing.trace.NoopSpanExporter
import io.github.kotlinmania.opentelemetrysdk.testing.trace.newTestExportSpanData
import io.github.kotlinmania.opentelemetrysdk.testing.trace.newTokioTestExporter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class TestingSpanExportersTest {
    @Test
    fun testNewTestExportSpanData() {
        val spanData = newTestExportSpanData()
        assertEquals("opentelemetry", spanData.name)
        assertTrue(spanData.spanContext.isValid)
        assertTrue(spanData.spanContext.isSampled)
    }

    @Test
    fun testTokioSpanExporter() {
        val (exporter, exportRx, shutdownRx) = newTokioTestExporter()
        val spanData = newTestExportSpanData()

        val exportRes = exporter.export(listOf(spanData))
        assertTrue(exportRes.isSuccess)

        val received = exportRx.tryReceive().getOrNull()
        assertNotNull(received)
        assertEquals("opentelemetry", received.name)

        val shutdownRes = exporter.shutdownWithTimeout(1.seconds)
        assertTrue(shutdownRes.isSuccess)

        val shutdownReceived = shutdownRx.tryReceive().getOrNull()
        assertNotNull(shutdownReceived)
    }

    @Test
    fun testNoopSpanExporter() {
        val exporter = NoopSpanExporter.new()
        val spanData = newTestExportSpanData()
        assertTrue(exporter.export(listOf(spanData)).isSuccess)
    }
}
