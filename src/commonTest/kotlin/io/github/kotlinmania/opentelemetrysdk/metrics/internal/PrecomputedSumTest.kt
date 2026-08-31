// port-lint: tests opentelemetry_sdk/src/metrics/internal/precomputed_sum.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PrecomputedSumTest {
    @Test
    fun testPrecomputedSumCumulative() {
        val filter = AttributeSetFilter()
        val preSum =
            PrecomputedSum(
                numberOps = LongOps,
                temporality = Temporality.Cumulative,
                filter = filter,
                monotonic = true,
                cardinalityLimit = 2000,
            )

        val attr = listOf(KeyValue("worker", "1"))
        preSum.call(100L, attr)
        preSum.call(150L, attr)

        val (count, agg) = preSum.call(null)
        assertEquals(1, count)
        assertNotNull(agg)
        @Suppress("UNCHECKED_CAST")
        val sumData = ((agg as AggregatedMetrics.I64).data as MetricData.SumData<Long>).sum
        assertEquals(150L, sumData.dataPoints[0].value)
    }

    @Test
    fun testPrecomputedSumDelta() {
        val filter = AttributeSetFilter()
        val preSum =
            PrecomputedSum(
                numberOps = LongOps,
                temporality = Temporality.Delta,
                filter = filter,
                monotonic = true,
                cardinalityLimit = 2000,
            )

        val attr = listOf(KeyValue("worker", "1"))
        preSum.call(100L, attr)

        val (count1, agg1) = preSum.call(null)
        assertEquals(1, count1)
        @Suppress("UNCHECKED_CAST")
        val sumData1 = ((agg1 as AggregatedMetrics.I64).data as MetricData.SumData<Long>).sum
        assertEquals(100L, sumData1.dataPoints[0].value)

        // Second report: 150 reported -> delta should be 150 - 100 = 50
        preSum.call(150L, attr)
        val (count2, agg2) = preSum.call(null)
        assertEquals(1, count2)
        @Suppress("UNCHECKED_CAST")
        val sumData2 = ((agg2 as AggregatedMetrics.I64).data as MetricData.SumData<Long>).sum
        assertEquals(50L, sumData2.dataPoints[0].value)
    }
}
