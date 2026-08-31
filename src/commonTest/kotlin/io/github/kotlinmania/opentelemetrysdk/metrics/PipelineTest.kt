// port-lint: tests opentelemetry_sdk/src/metrics/pipeline.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PipelineTest {
    @Test
    fun testSdkMeterCounterProduce() {
        val reader = ManualReader.builder().build()
        val provider =
            SdkMeterProvider
                .builder()
                .withResource(Resource.builder().withServiceName("test-service").build())
                .withReader(reader)
                .build()

        val meter = provider.meter("my-test-meter")
        val counter = meter.counter("test_counter", "a test counter", "1")

        counter.add(10L, listOf(KeyValue("key1", "val1")))
        counter.add(20L, listOf(KeyValue("key1", "val1")))
        counter.add(5L, listOf(KeyValue("key1", "val2")))

        val rm = ResourceMetrics(Resource.builder().build(), emptyList())
        val produceResult = provider.pipelines.pipes[0].produce(rm)
        assertTrue(produceResult.isSuccess)

        assertEquals(1, rm.scopeMetrics.size)
        val sm = rm.scopeMetrics[0]
        assertEquals("my-test-meter", sm.scope.name)
        assertEquals(1, sm.metrics.size)

        val metric = sm.metrics[0]
        assertEquals("test_counter", metric.name)
        assertEquals("a test counter", metric.description())
        assertEquals("1", metric.unit)

        val agg = metric.data
        assertTrue(agg is AggregatedMetrics.I64)
        @Suppress("UNCHECKED_CAST")
        val sumData = (agg.data as MetricData.SumData<Long>).sum
        assertEquals(2, sumData.dataPoints.size)
    }

    @Test
    fun testSdkMeterViewRenaming() {
        val reader = ManualReader.builder().build()
        val view =
            View { inst ->
                if (inst.name == "old_counter") {
                    Stream
                        .builder()
                        .withName("new_counter")
                        .withDescription("renamed description")
                        .build()
                        .getOrNull()
                } else {
                    null
                }
            }

        val provider =
            SdkMeterProvider
                .builder()
                .withReader(reader)
                .withView(view)
                .build()

        val meter = provider.meter("view-meter")
        val counter = meter.counter("old_counter", "old description", "1")
        counter.add(100L)

        val rm = ResourceMetrics(Resource.builder().build(), emptyList())
        provider.pipelines.pipes[0].produce(rm)

        val metric = rm.scopeMetrics[0].metrics[0]
        assertEquals("new_counter", metric.name)
        assertEquals("renamed description", metric.description())
    }
}
