// port-lint: tests opentelemetry_sdk/src/metrics/internal/last_value.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LastValueTest {
    @Test
    fun testLastValueCumulative() {
        val filter = AttributeSetFilter()
        val lastValue =
            LastValue(
                numberOps = DoubleOps,
                temporality = Temporality.Cumulative,
                filter = filter,
                cardinalityLimit = 2000,
            )

        val attr = listOf(KeyValue("key", "val"))

        lastValue.call(10.0, attr)
        lastValue.call(25.5, attr)
        lastValue.call(18.0, attr)

        val (count, agg) = lastValue.call(null)
        assertEquals(1, count)
        assertNotNull(agg)
        assertTrue(agg is AggregatedMetrics.F64)

        val metricData = agg.data
        assertTrue(metricData is MetricData.GaugeData<*>)
        @Suppress("UNCHECKED_CAST")
        val gaugeData = (metricData as MetricData.GaugeData<Double>).gauge
        assertEquals(1, gaugeData.dataPoints.size)
        assertEquals(18.0, gaugeData.dataPoints[0].value)
    }

    @Test
    fun testLastValueDelta() {
        val filter = AttributeSetFilter()
        val lastValue =
            LastValue(
                numberOps = LongOps,
                temporality = Temporality.Delta,
                filter = filter,
                cardinalityLimit = 2000,
            )

        val attr = listOf(KeyValue("env", "prod"))

        lastValue.call(42L, attr)

        val (count1, agg1) = lastValue.call(null)
        assertEquals(1, count1)
        @Suppress("UNCHECKED_CAST")
        val gaugeData1 = ((agg1 as AggregatedMetrics.I64).data as MetricData.GaugeData<Long>).gauge
        assertEquals(42L, gaugeData1.dataPoints[0].value)

        // After delta collection, should reset
        val (count2, _) = lastValue.call(null)
        assertEquals(0, count2)
    }
}
