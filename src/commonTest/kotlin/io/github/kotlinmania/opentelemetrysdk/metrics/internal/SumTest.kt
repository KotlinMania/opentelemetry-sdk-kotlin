// port-lint: tests metrics/internal/sum.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.metrics.data.SumDataPoint
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SumTest {
    @Test
    fun testSumCumulativeLong() {
        val filter = AttributeSetFilter()
        val sum =
            Sum(
                numberOps = LongOps,
                temporality = Temporality.Cumulative,
                filter = filter,
                monotonic = true,
                cardinalityLimit = 2000,
            )

        val attr1 = listOf(KeyValue("key", "val1"))
        val attr2 = listOf(KeyValue("key", "val2"))

        sum.call(10L, attr1)
        sum.call(5L, attr2)
        sum.call(20L, attr1)

        val (count, agg) = sum.call(null)
        assertEquals(2, count)
        assertNotNull(agg)
        assertTrue(agg is AggregatedMetrics.I64)

        val metricData = agg.data
        assertTrue(metricData is MetricData.SumData<*>)
        @Suppress("UNCHECKED_CAST")
        val sumData = (metricData as MetricData.SumData<Long>).sum
        assertEquals(Temporality.Cumulative, sumData.temporality)
        assertTrue(sumData.isMonotonic)
        assertEquals(2, sumData.dataPoints.size)

        val dp1 = sumData.dataPoints.find { it.attributes == attr1 }
        assertNotNull(dp1)
        assertEquals(30L, dp1.value)

        val dp2 = sumData.dataPoints.find { it.attributes == attr2 }
        assertNotNull(dp2)
        assertEquals(5L, dp2.value)
    }

    @Test
    fun testSumDeltaDouble() {
        val filter = AttributeSetFilter()
        val sum =
            Sum(
                numberOps = DoubleOps,
                temporality = Temporality.Delta,
                filter = filter,
                monotonic = false,
                cardinalityLimit = 2000,
            )

        val attr = listOf(KeyValue("k", "v"))

        sum.call(1.5, attr)
        sum.call(2.5, attr)

        val (count1, agg1) = sum.call(null)
        assertEquals(1, count1)
        @Suppress("UNCHECKED_CAST")
        val sumData1 = ((agg1 as AggregatedMetrics.F64).data as MetricData.SumData<Double>).sum
        assertEquals(4.0, sumData1.dataPoints[0].value)

        // Delta reset check: next collection without calls should be empty
        val (count2, _) = sum.call(null)
        assertEquals(0, count2)

        // Now record again
        sum.call(10.0, attr)
        val (count3, agg3) = sum.call(null)
        assertEquals(1, count3)
        @Suppress("UNCHECKED_CAST")
        val sumData3 = ((agg3 as AggregatedMetrics.F64).data as MetricData.SumData<Double>).sum
        assertEquals(10.0, sumData3.dataPoints[0].value)
    }

    @Test
    fun testSumNoAttributes() {
        val filter = AttributeSetFilter()
        val sum =
            Sum(
                numberOps = LongOps,
                temporality = Temporality.Cumulative,
                filter = filter,
                monotonic = true,
                cardinalityLimit = 2000,
            )

        sum.call(100L, emptyList())
        sum.call(50L, emptyList())

        val (count, agg) = sum.call(null)
        assertEquals(1, count)
        @Suppress("UNCHECKED_CAST")
        val sumData = ((agg as AggregatedMetrics.I64).data as MetricData.SumData<Long>).sum
        assertEquals(150L, sumData.dataPoints[0].value)
        assertTrue(sumData.dataPoints[0].attributes.isEmpty())
    }
}
