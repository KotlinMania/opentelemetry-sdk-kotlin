// port-lint: tests metrics/instrument.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InstrumentTest {
    @Test
    fun streamNameValidation() {
        val testCases =
            listOf(
                Pair("validateName", ""),
                Pair("_startWithNoneAlphabet", INSTRUMENT_NAME_FIRST_ALPHABETIC),
                Pair("utf8char锈", INSTRUMENT_NAME_INVALID_CHAR),
                Pair("a".repeat(255), ""),
                Pair("a".repeat(256), INSTRUMENT_NAME_LENGTH),
                Pair("invalid name", INSTRUMENT_NAME_INVALID_CHAR),
                Pair("allow/slash", ""),
                Pair("allow_under_score", ""),
                Pair("allow.dots.ok", ""),
                Pair("", INSTRUMENT_NAME_EMPTY),
                Pair("\\allow\\slash /sec", INSTRUMENT_NAME_FIRST_ALPHABETIC),
                Pair("\\allow\\${'$'}${'$'}slash /sec", INSTRUMENT_NAME_FIRST_ALPHABETIC),
                Pair("Total $ Count", INSTRUMENT_NAME_INVALID_CHAR),
                Pair("\\test\\UsagePercent(Total) > 80%", INSTRUMENT_NAME_FIRST_ALPHABETIC),
                Pair("/not / allowed", INSTRUMENT_NAME_FIRST_ALPHABETIC),
            )

        for ((name, expectedError) in testCases) {
            val builder = StreamBuilder().withName(name)
            val result = builder.build()

            if (expectedError.isEmpty()) {
                assertTrue(result.isSuccess, "Expected successful build for name '$name', but got error: ${result.exceptionOrNull()}")
            } else {
                assertTrue(result.isFailure, "Expected error for name '$name'")
                val errStr = result.exceptionOrNull()?.message ?: ""
                assertEquals(expectedError, errStr, "For name '$name', expected error '$expectedError', but got '$errStr'")
            }
        }
    }

    @Test
    fun streamUnitValidation() {
        val testCases =
            listOf(
                Pair("0123456789012345678901234567890123456789012345678901234567890123", INSTRUMENT_UNIT_LENGTH),
                Pair("utf8char锈", INSTRUMENT_UNIT_INVALID_CHAR),
                Pair("kb", ""),
                Pair("Kb/sec", ""),
                Pair("%", ""),
                Pair("", ""),
            )

        for ((unit, expectedError) in testCases) {
            val builder = StreamBuilder().withName("valid_name").withUnit(unit)
            val result = builder.build()

            if (expectedError.isEmpty()) {
                assertTrue(result.isSuccess, "Expected successful build for unit '$unit', but got error: ${result.exceptionOrNull()}")
            } else {
                assertTrue(result.isFailure, "Expected error for unit '$unit'")
                val errStr = result.exceptionOrNull()?.message ?: ""
                assertEquals(expectedError, errStr, "For unit '$unit', expected error '$expectedError', but got '$errStr'")
            }
        }
    }

    @Test
    fun streamCardinalityLimitValidation() {
        val builder = StreamBuilder().withName("valid_name").withCardinalityLimit(0)
        val result = builder.build()
        assertTrue(result.isFailure)
        assertEquals("Cardinality limit must be greater than 0", result.exceptionOrNull()?.message)

        val validLimits = listOf(1, 10, 100, 1000)
        for (limit in validLimits) {
            val validBuilder = StreamBuilder().withName("valid_name").withCardinalityLimit(limit)
            assertTrue(validBuilder.build().isSuccess)
        }
    }

    @Test
    fun streamValidBuild() {
        val stream =
            StreamBuilder()
                .withName("valid_name")
                .withDescription("Valid description")
                .withUnit("ms")
                .withCardinalityLimit(100)
                .build()
        assertTrue(stream.isSuccess)
    }

    @Test
    fun streamHistogramBucketValidation() {
        val validBoundaries = listOf(1.0, 2.0, 5.0, 10.0, 20.0, 50.0, 100.0)
        val builder =
            StreamBuilder()
                .withName("valid_histogram")
                .withAggregation(
                    Aggregation.ExplicitBucketHistogram(
                        boundaries = validBoundaries,
                        recordMinMax = true,
                    ),
                )
        assertTrue(builder.build().isSuccess)

        // NaN
        val invalidNanBoundaries = listOf(1.0, 2.0, Double.NaN, 10.0)
        val nanBuilder =
            StreamBuilder()
                .withName("invalid_histogram_nan")
                .withAggregation(Aggregation.ExplicitBucketHistogram(invalidNanBoundaries))
        assertTrue(nanBuilder.build().isFailure)
        assertEquals(
            "Bucket boundaries must not contain NaN, Infinity, or -Infinity",
            nanBuilder.build().exceptionOrNull()?.message,
        )

        // Infinity
        val invalidInfBoundaries = listOf(1.0, 5.0, Double.POSITIVE_INFINITY, 100.0)
        val infBuilder =
            StreamBuilder()
                .withName("invalid_histogram_inf")
                .withAggregation(Aggregation.ExplicitBucketHistogram(invalidInfBoundaries))
        assertTrue(infBuilder.build().isFailure)
        assertEquals(
            "Bucket boundaries must not contain NaN, Infinity, or -Infinity",
            infBuilder.build().exceptionOrNull()?.message,
        )

        // Negative Infinity
        val invalidNegInfBoundaries = listOf(Double.NEGATIVE_INFINITY, 5.0, 10.0, 100.0)
        val negInfBuilder =
            StreamBuilder()
                .withName("invalid_histogram_neg_inf")
                .withAggregation(Aggregation.ExplicitBucketHistogram(invalidNegInfBoundaries))
        assertTrue(negInfBuilder.build().isFailure)
        assertEquals(
            "Bucket boundaries must not contain NaN, Infinity, or -Infinity",
            negInfBuilder.build().exceptionOrNull()?.message,
        )

        // Unsorted
        val unsortedBoundaries = listOf(1.0, 5.0, 2.0, 10.0)
        val unsortedBuilder =
            StreamBuilder()
                .withName("unsorted_histogram")
                .withAggregation(Aggregation.ExplicitBucketHistogram(unsortedBoundaries))
        assertTrue(unsortedBuilder.build().isFailure)
        assertEquals(
            "Bucket boundaries must be sorted and not contain any duplicates",
            unsortedBuilder.build().exceptionOrNull()?.message,
        )

        // Duplicates
        val duplicateBoundaries = listOf(1.0, 2.0, 5.0, 5.0, 10.0)
        val duplicateBuilder =
            StreamBuilder()
                .withName("duplicate_histogram")
                .withAggregation(Aggregation.ExplicitBucketHistogram(duplicateBoundaries))
        assertTrue(duplicateBuilder.build().isFailure)
        assertEquals(
            "Bucket boundaries must be sorted and not contain any duplicates",
            duplicateBuilder.build().exceptionOrNull()?.message,
        )
    }
}
