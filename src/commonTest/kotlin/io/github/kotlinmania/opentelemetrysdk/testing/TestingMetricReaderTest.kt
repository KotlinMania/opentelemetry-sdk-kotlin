package io.github.kotlinmania.opentelemetrysdk.testing

import io.github.kotlinmania.opentelemetrysdk.metrics.InstrumentKind
import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import io.github.kotlinmania.opentelemetrysdk.testing.metrics.TestMetricReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class TestingMetricReaderTest {
    @Test
    fun testTestMetricReaderLifecycle() {
        val reader = TestMetricReader.new()
        assertFalse(reader.isShutdown())

        val rm = ResourceMetrics(Resource.empty(), emptyList())
        val collectRes = reader.collect(rm)
        assertTrue(collectRes.isSuccess)

        val flushRes = reader.forceFlush()
        assertTrue(flushRes.isSuccess)

        assertEquals(Temporality.Cumulative, reader.temporality(InstrumentKind.Counter))

        val shutdownRes = reader.shutdownWithTimeout(1.seconds)
        assertTrue(shutdownRes.isSuccess)
        assertTrue(reader.isShutdown())
    }
}
