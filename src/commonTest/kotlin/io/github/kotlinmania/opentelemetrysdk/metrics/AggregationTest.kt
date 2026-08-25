// port-lint: tests metrics/aggregation.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import kotlin.test.Test
import kotlin.test.assertTrue

class AggregationTest {
    private data class TestCase(
        val name: String,
        val input: Aggregation,
        val check: (Result<Unit>) -> Boolean,
    )

    @Test
    fun validateAggregation() {
        val ok: (Result<Unit>) -> Boolean = { it.isSuccess }
        val configError: (Result<Unit>) -> Boolean = { result ->
            result.isFailure && result.exceptionOrNull() is MetricError.Config
        }

        val testCases =
            listOf(
                TestCase(
                    name = "base2 histogram with maximum max_scale",
                    input =
                        Aggregation.Base2ExponentialHistogram(
                            maxSize = 160u,
                            maxScale = EXPO_MAX_SCALE,
                            recordMinMax = true,
                        ),
                    check = ok,
                ),
                TestCase(
                    name = "base2 histogram with minimum max_scale",
                    input =
                        Aggregation.Base2ExponentialHistogram(
                            maxSize = 160u,
                            maxScale = EXPO_MIN_SCALE,
                            recordMinMax = true,
                        ),
                    check = ok,
                ),
                TestCase(
                    name = "base2 histogram with max_scale too small",
                    input =
                        Aggregation.Base2ExponentialHistogram(
                            maxSize = 160u,
                            maxScale = (EXPO_MIN_SCALE - 1).toByte(),
                            recordMinMax = true,
                        ),
                    check = configError,
                ),
                TestCase(
                    name = "base2 histogram with max_scale too big",
                    input =
                        Aggregation.Base2ExponentialHistogram(
                            maxSize = 160u,
                            maxScale = (EXPO_MAX_SCALE + 1).toByte(),
                            recordMinMax = true,
                        ),
                    check = configError,
                ),
                TestCase(
                    name = "explicit histogram with one boundary",
                    input =
                        Aggregation.ExplicitBucketHistogram(
                            boundaries = listOf(0.0),
                            recordMinMax = true,
                        ),
                    check = ok,
                ),
                TestCase(
                    name = "explicit histogram with monotonic boundaries",
                    input =
                        Aggregation.ExplicitBucketHistogram(
                            boundaries = listOf(0.0, 2.0, 4.0, 8.0),
                            recordMinMax = true,
                        ),
                    check = ok,
                ),
                TestCase(
                    name = "explicit histogram with non-monotonic boundaries",
                    input =
                        Aggregation.ExplicitBucketHistogram(
                            boundaries = listOf(2.0, 0.0, 4.0, 8.0),
                            recordMinMax = true,
                        ),
                    check = configError,
                ),
            )

        for (test in testCases) {
            assertTrue(test.check(test.input.validate()), test.name)
        }
    }
}
