// port-lint: tests opentelemetry_sdk/src/metrics/internal/exponential_histogram.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.metrics.data.AggregatedMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ExponentialHistogramDataPoint
import io.github.kotlinmania.opentelemetrysdk.metrics.data.MetricData
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExponentialHistogramTest {
    @Test
    fun testFrexp() {
        val (frac, exp) = frexp(1.0)
        assertEquals(0.5, frac)
        assertEquals(1, exp)

        val (frac2, exp2) = frexp(2.0)
        assertEquals(0.5, frac2)
        assertEquals(2, exp2)
    }

    @Test
    fun testExpoBucketsRecord() {
        val b = ExpoBuckets()
        b.record(5)
        assertEquals(5, b.startBin)
        assertEquals(listOf(1uL), b.counts)

        b.record(5)
        assertEquals(listOf(2uL), b.counts)

        b.record(7)
        assertEquals(5, b.startBin)
        assertEquals(listOf(2uL, 0uL, 1uL), b.counts)

        b.record(3)
        assertEquals(3, b.startBin)
        assertEquals(listOf(1uL, 0uL, 2uL, 0uL, 1uL), b.counts)
    }

    @Test
    fun testExpoHistogramCumulative() {
        val filter = AttributeSetFilter()
        val hist =
            ExpoHistogram(
                numberOps = DoubleOps,
                temporality = Temporality.Cumulative,
                filter = filter,
                maxSize = 160u,
                maxScale = 20,
                recordMinMax = true,
                recordSum = true,
                cardinalityLimit = 2000,
            )

        val attr = listOf(KeyValue("test", "expo"))
        hist.call(2.0, attr)
        hist.call(4.0, attr)
        hist.call(1.0, attr)

        val (count, agg) = hist.call(null)
        assertEquals(1, count)
        assertNotNull(agg)
        assertTrue(agg is AggregatedMetrics.F64)

        val metricData = agg.data
        assertTrue(metricData is MetricData.ExponentialHistogramData<*>)
        @Suppress("UNCHECKED_CAST")
        val histData = (metricData as MetricData.ExponentialHistogramData<Double>).exponentialHistogram
        val dp: ExponentialHistogramDataPoint<Double> = histData.dataPoints[0]

        assertEquals(3uL, dp.count)
        assertEquals(7.0, dp.sum)
        assertEquals(1.0, dp.min)
        assertEquals(4.0, dp.max)
        assertEquals(3uL, dp.positiveBucket.counts.fold(0uL) { acc: ULong, c: ULong -> acc + c })
    }
}
