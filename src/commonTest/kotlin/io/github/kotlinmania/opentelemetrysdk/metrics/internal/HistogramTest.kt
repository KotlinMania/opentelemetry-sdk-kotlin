// port-lint: tests metrics/internal/histogram.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.HistogramDataPoint
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HistogramTest {
    @Test
    fun testPartitionPoint() {
        val bounds = listOf(0.0, 5.0, 10.0, 25.0, 50.0)
        assertEquals(0, partitionPoint(bounds, -1.0))
        assertEquals(0, partitionPoint(bounds, 0.0))
        assertEquals(1, partitionPoint(bounds, 4.9))
        assertEquals(1, partitionPoint(bounds, 5.0))
        assertEquals(4, partitionPoint(bounds, 50.0))
        assertEquals(5, partitionPoint(bounds, 100.0))
    }

    @Test
    fun testExplicitBucketHistogramCumulative() {
        val filter = AttributeSetFilter()
        val bounds = listOf(0.0, 10.0, 20.0)
        val hist =
            Histogram(
                numberOps = DoubleOps,
                temporality = Temporality.Cumulative,
                filter = filter,
                bounds = bounds,
                recordMinMax = true,
                recordSum = true,
                cardinalityLimit = 2000,
            )

        val attr = listOf(KeyValue("method", "GET"))

        hist.call(5.0, attr) // falls in bucket 1 (0.0..10.0)
        hist.call(15.0, attr) // falls in bucket 2 (10.0..20.0)
        hist.call(25.0, attr) // falls in bucket 3 (>20.0)
        hist.call(-5.0, attr) // falls in bucket 0 (<0.0)

        val (count, agg) = hist.call(null)
        assertEquals(1, count)
        assertNotNull(agg)
        assertTrue(agg is AggregatedMetrics.F64)

        val metricData = agg.data
        assertTrue(metricData is MetricData.HistogramData<*>)
        @Suppress("UNCHECKED_CAST")
        val histData = (metricData as MetricData.HistogramData<Double>).histogram
        val dp: HistogramDataPoint<Double> = histData.dataPoints[0]

        assertEquals(4uL, dp.count)
        assertEquals(40.0, dp.sum)
        assertEquals(-5.0, dp.min)
        assertEquals(25.0, dp.max)
        assertEquals(listOf(1uL, 1uL, 1uL, 1uL), dp.bucketCounts)
        assertEquals(bounds, dp.bounds)
    }
}
