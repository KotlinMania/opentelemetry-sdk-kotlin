// port-lint: tests metrics/in_memory_exporter.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InMemoryExporterTest {
    @Test
    fun inMemoryExporterBasicOperations() {
        val exporter = InMemoryMetricExporter.default()
        assertEquals(Temporality.Cumulative, exporter.temporality())

        val rm = ResourceMetrics()
        val exportResult = exporter.export(rm)
        assertTrue(exportResult.isSuccess)

        val finished = exporter.getFinishedMetrics()
        assertTrue(finished.isSuccess)
        assertEquals(1, finished.getOrNull()?.size)

        exporter.reset()
        val finishedAfterReset = exporter.getFinishedMetrics()
        assertTrue(finishedAfterReset.isSuccess)
        assertEquals(0, finishedAfterReset.getOrNull()?.size)
    }

    @Test
    fun inMemoryExporterWithTemporality() {
        val exporter =
            InMemoryMetricExporter
                .builder()
                .withTemporality(Temporality.Delta)
                .build()
        assertEquals(Temporality.Delta, exporter.temporality())
    }
}
