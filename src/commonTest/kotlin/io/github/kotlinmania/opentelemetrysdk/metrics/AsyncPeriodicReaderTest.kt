package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.Tokio
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class AsyncPeriodicReaderTest {
    @Test
    fun testAsyncPeriodicReader() {
        val exporter = InMemoryMetricExporter.default()
        val runtime = Tokio()
        val reader =
            AsyncPeriodicReader
                .builder(exporter, runtime)
                .withInterval(10.seconds)
                .withTimeout(5.seconds)
                .build()

        val producer =
            object : SdkProducer {
                override fun produce(rm: ResourceMetrics): io.github.kotlinmania.opentelemetrysdk.OTelSdkResult =
                    Result.success(Unit)
            }
        reader.registerPipeline(producer)

        val rm = ResourceMetrics(Resource.empty(), emptyList())
        assertTrue(reader.collect(rm).isSuccess)
        assertTrue(reader.forceFlush().isSuccess)
        assertEquals(Temporality.Cumulative, reader.temporality(InstrumentKind.Counter))
        assertTrue(reader.shutdownWithTimeout(1.seconds).isSuccess)
    }
}
