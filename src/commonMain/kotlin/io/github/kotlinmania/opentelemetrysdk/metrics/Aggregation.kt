// port-lint: source opentelemetry_sdk/src/metrics/aggregation.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

public const val EXPO_MAX_SCALE: Byte = 20
public const val EXPO_MIN_SCALE: Byte = -10

public val DEFAULT_HISTOGRAM_BOUNDARIES: List<Double> =
    listOf(
        0.0,
        5.0,
        10.0,
        25.0,
        50.0,
        75.0,
        100.0,
        250.0,
        500.0,
        750.0,
        1000.0,
        2500.0,
        5000.0,
        7500.0,
        10000.0,
    )

/**
 * The way recorded measurements are summarized.
 */
public sealed class Aggregation {
    /**
     * An aggregation that drops all recorded data.
     */
    public data object Drop : Aggregation() {
        override fun toString(): String = "Drop"
    }

    /**
     * An aggregation that uses the default instrument kind selection mapping to
     * select another aggregation.
     */
    public data object Default : Aggregation() {
        override fun toString(): String = "Default"
    }

    /**
     * An aggregation that summarizes a set of measurements as their arithmetic sum.
     */
    public data object Sum : Aggregation() {
        override fun toString(): String = "Sum"
    }

    /**
     * An aggregation that summarizes a set of measurements as the last one made.
     */
    public data object LastValue : Aggregation() {
        override fun toString(): String = "LastValue"
    }

    /**
     * An aggregation that summarizes a set of measurements as a histogram with
     * explicitly defined buckets.
     */
    public data class ExplicitBucketHistogram(
        public val boundaries: List<Double> = DEFAULT_HISTOGRAM_BOUNDARIES,
        public val recordMinMax: Boolean = true,
    ) : Aggregation() {
        override fun toString(): String = "ExplicitBucketHistogram"
    }

    /**
     * An aggregation that summarizes a set of measurements as a histogram with
     * bucket widths that grow exponentially.
     */
    public data class Base2ExponentialHistogram(
        public val maxSize: UInt = 160u,
        public val maxScale: Byte = 20,
        public val recordMinMax: Boolean = true,
    ) : Aggregation() {
        override fun toString(): String = "Base2ExponentialHistogram"
    }

    /**
     * Validates that this aggregation has correct configuration.
     */
    public fun validate(): Result<Unit> =
        when (this) {
            is Drop -> Result.success(Unit)
            is Default -> Result.success(Unit)
            is Sum -> Result.success(Unit)
            is LastValue -> Result.success(Unit)
            is ExplicitBucketHistogram -> {
                var isValid = true
                for (i in 1 until boundaries.size) {
                    if (boundaries[i - 1] >= boundaries[i]) {
                        isValid = false
                        break
                    }
                }
                if (isValid) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        MetricError.Config(
                            "aggregation: explicit bucket histogram: non-monotonic boundaries: $boundaries",
                        ),
                    )
                }
            }
            is Base2ExponentialHistogram -> {
                if (maxScale > EXPO_MAX_SCALE) {
                    Result.failure(
                        MetricError.Config(
                            "aggregation: exponential histogram: max scale ($maxScale) is greater than 20",
                        ),
                    )
                } else if (maxScale < EXPO_MIN_SCALE) {
                    Result.failure(
                        MetricError.Config(
                            "aggregation: exponential histogram: max scale ($maxScale) is less than -10",
                        ),
                    )
                } else {
                    Result.success(Unit)
                }
            }
        }
}
