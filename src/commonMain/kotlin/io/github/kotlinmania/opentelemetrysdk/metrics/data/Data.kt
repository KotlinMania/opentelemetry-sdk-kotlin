// port-lint: source metrics/data/mod.rs
package io.github.kotlinmania.opentelemetrysdk.metrics.data

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.metrics.Temporality
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.time.Instant

/**
 * A collection of [ScopeMetrics] and the associated [Resource] that created them.
 */
public data class ResourceMetrics(
    public val resource: Resource = Resource.empty(),
    public val scopeMetrics: List<ScopeMetrics> = emptyList(),
) {
    public companion object {
        public fun default(): ResourceMetrics = ResourceMetrics()
    }
}

/**
 * A collection of metrics produced by a meter.
 */
public data class ScopeMetrics(
    public val scope: InstrumentationScope = InstrumentationScope.EMPTY,
    public val metrics: List<Metric> = emptyList(),
) {
    public companion object {
        public fun default(): ScopeMetrics = ScopeMetrics()
    }
}

/**
 * A collection of one or more aggregated time series from an Instrument.
 */
public data class Metric(
    public val name: String,
    public val descriptionText: String = "",
    public val unit: String = "",
    public val data: AggregatedMetrics,
) {
    public fun description(): String = descriptionText
}

/**
 * Aggregated metrics data from an instrument.
 */
public sealed class AggregatedMetrics {
    /** All metric data with Double value type. */
    public data class F64(
        public val data: MetricData<Any?>,
    ) : AggregatedMetrics()

    /** All metric data with ULong value type. */
    public data class U64(
        public val data: MetricData<Any?>,
    ) : AggregatedMetrics()

    /** All metric data with Long value type. */
    public data class I64(
        public val data: MetricData<Any?>,
    ) : AggregatedMetrics()
}

/**
 * Metric data for all types.
 */
public sealed class MetricData<out T> {
    /** Metric data for Gauge. */
    public data class GaugeData<out T>(
        public val gauge: Gauge<T>,
    ) : MetricData<T>()

    /** Metric data for Sum. */
    public data class SumData<out T>(
        public val sum: Sum<T>,
    ) : MetricData<T>()

    /** Metric data for Histogram. */
    public data class HistogramData<out T>(
        public val histogram: Histogram<T>,
    ) : MetricData<T>()

    /** Metric data for ExponentialHistogram. */
    public data class ExponentialHistogramData<out T>(
        public val exponentialHistogram: ExponentialHistogram<T>,
    ) : MetricData<T>()
}

/**
 * A single data point in a gauge time series.
 */
public data class GaugeDataPoint<out T>(
    public val attributes: List<KeyValue> = emptyList(),
    public val value: T,
    public val exemplars: List<Exemplar<T>> = emptyList(),
)

/**
 * A measurement of the current value of an instrument.
 */
public data class Gauge<out T>(
    public val dataPoints: List<GaugeDataPoint<T>> = emptyList(),
    public val startTime: Instant? = null,
    public val time: Instant,
)

/**
 * A single data point in a sum time series.
 */
public data class SumDataPoint<out T>(
    public val attributes: List<KeyValue> = emptyList(),
    public val value: T,
    public val exemplars: List<Exemplar<T>> = emptyList(),
)

/**
 * Represents the sum of all measurements of values from an instrument.
 */
public data class Sum<out T>(
    public val dataPoints: List<SumDataPoint<T>> = emptyList(),
    public val startTime: Instant,
    public val time: Instant,
    public val temporality: Temporality = Temporality.Cumulative,
    public val isMonotonic: Boolean = true,
)

/**
 * A single histogram data point in a time series.
 */
public data class HistogramDataPoint<out T>(
    public val attributes: List<KeyValue> = emptyList(),
    public val count: ULong = 0u,
    public val bounds: List<Double> = emptyList(),
    public val bucketCounts: List<ULong> = emptyList(),
    public val min: T? = null,
    public val max: T? = null,
    public val sum: T,
    public val exemplars: List<Exemplar<T>> = emptyList(),
)

/**
 * Represents the histogram of all measurements of values from an instrument.
 */
public data class Histogram<out T>(
    public val dataPoints: List<HistogramDataPoint<T>> = emptyList(),
    public val startTime: Instant,
    public val time: Instant,
    public val temporality: Temporality = Temporality.Cumulative,
)

/**
 * A set of bucket counts, encoded in a contiguous array of counts.
 */
public data class ExponentialBucket(
    public val offset: Int = 0,
    public val counts: List<ULong> = emptyList(),
)

/**
 * A single exponential histogram data point in a time series.
 */
public data class ExponentialHistogramDataPoint<out T>(
    public val attributes: List<KeyValue> = emptyList(),
    public val count: ULong = 0u,
    public val min: T? = null,
    public val max: T? = null,
    public val sum: T,
    public val scale: Byte = 0,
    public val zeroCount: ULong = 0u,
    public val positiveBucket: ExponentialBucket = ExponentialBucket(),
    public val negativeBucket: ExponentialBucket = ExponentialBucket(),
    public val zeroThreshold: Double = 0.0,
    public val exemplars: List<Exemplar<T>> = emptyList(),
)

/**
 * The histogram of all measurements of values from an instrument.
 */
public data class ExponentialHistogram<out T>(
    public val dataPoints: List<ExponentialHistogramDataPoint<T>> = emptyList(),
    public val startTime: Instant,
    public val time: Instant,
    public val temporality: Temporality = Temporality.Cumulative,
)

/**
 * A measurement sampled from a time series providing a typical example.
 */
public data class Exemplar<out T>(
    public val filteredAttributes: List<KeyValue> = emptyList(),
    public val time: Instant,
    public val value: T,
    public val spanId: ByteArray = ByteArray(8),
    public val traceId: ByteArray = ByteArray(16),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Exemplar<*>) return false
        return filteredAttributes == other.filteredAttributes &&
            time == other.time &&
            value == other.value &&
            spanId.contentEquals(other.spanId) &&
            traceId.contentEquals(other.traceId)
    }

    override fun hashCode(): Int {
        var result = filteredAttributes.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + (value?.hashCode() ?: 0)
        result = 31 * result + spanId.contentHashCode()
        result = 31 * result + traceId.contentHashCode()
        return result
    }
}


