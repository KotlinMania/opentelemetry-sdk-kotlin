// port-lint: tests metrics/data/mod.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.data

import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class DataTest {
    @Test
    fun validateCloningDataPoints() {
        val now = Clock.System.now()
        val sumDataPoint =
            SumDataPoint(
                attributes = listOf(KeyValue("key", "value")),
                value = 0u,
                exemplars =
                    listOf(
                        Exemplar(
                            filteredAttributes = emptyList(),
                            time = now,
                            value = 0u,
                            spanId = ByteArray(8),
                            traceId = ByteArray(16),
                        ),
                    ),
            )
        assertEquals(sumDataPoint.copy(), sumDataPoint)

        val histogramDataPoint =
            HistogramDataPoint(
                attributes = listOf(KeyValue("key", "value")),
                count = 0u,
                bounds = emptyList(),
                bucketCounts = emptyList(),
                min = null,
                max = null,
                sum = 0u,
                exemplars =
                    listOf(
                        Exemplar(
                            filteredAttributes = emptyList(),
                            time = now,
                            value = 0u,
                            spanId = ByteArray(8),
                            traceId = ByteArray(16),
                        ),
                    ),
            )
        assertEquals(histogramDataPoint.copy(), histogramDataPoint)

        val exponentialHistogramDataPoint =
            ExponentialHistogramDataPoint(
                attributes = listOf(KeyValue("key", "value")),
                count = 0u,
                min = null,
                max = null,
                sum = 0u,
                scale = 0,
                zeroCount = 0u,
                positiveBucket =
                    ExponentialBucket(
                        offset = 0,
                        counts = emptyList(),
                    ),
                negativeBucket =
                    ExponentialBucket(
                        offset = 0,
                        counts = emptyList(),
                    ),
                zeroThreshold = 0.0,
                exemplars =
                    listOf(
                        Exemplar(
                            filteredAttributes = emptyList(),
                            time = now,
                            value = 0u,
                            spanId = ByteArray(8),
                            traceId = ByteArray(16),
                        ),
                    ),
            )
        assertEquals(
            exponentialHistogramDataPoint.copy(),
            exponentialHistogramDataPoint,
        )
    }
}
