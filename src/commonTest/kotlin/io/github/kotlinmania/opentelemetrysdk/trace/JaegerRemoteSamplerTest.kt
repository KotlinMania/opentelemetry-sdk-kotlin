package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.Tokio
import io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote.Inner
import io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote.JaegerRemoteSampler
import io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote.LeakyBucket
import io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote.ProbabilisticSamplingStrategy
import io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote.SamplingStrategyResponse
import io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote.SamplingStrategyType
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class JaegerRemoteSamplerTest {
    @Test
    fun testLeakyBucket() {
        val leakyBucket = LeakyBucket(bucketSize = 2.0, spanPerSec = 0.1)
        val initialTime = Instant.fromEpochSeconds(1000)
        leakyBucket.lastTime = initialTime

        val testCases =
            listOf(
                0 to listOf(true, true, false),
                1 to listOf(false),
                5 to listOf(false),
                10 to listOf(true, false),
                60 to listOf(true, true, false),
            )

        for ((elapsedSec, cases) in testCases) {
            for (shouldPass in cases) {
                val pass = leakyBucket.checkAvailability { initialTime.plus(elapsedSec.seconds) }
                assertEquals(shouldPass, pass)
            }
        }
    }

    @Test
    fun testLeakyBucketClockAdjustment() {
        val leakyBucket = LeakyBucket(bucketSize = 2.0, spanPerSec = 0.1)
        val initialTime = Instant.fromEpochSeconds(1000)
        leakyBucket.lastTime = initialTime

        // Rewind clock: should pass
        val pass = leakyBucket.checkAvailability { initialTime.minus(10.seconds) }
        assertTrue(pass)
    }

    @Test
    fun testSamplingStrategyResponseSerialization() {
        val json = Json { ignoreUnknownKeys = true }
        val jsonString =
            """
            {
                "strategyType": "PROBABILISTIC",
                "probabilisticSampling": {
                    "samplingRate": 0.5
                }
            }
            """.trimIndent()
        val resp = json.decodeFromString<SamplingStrategyResponse>(jsonString)
        assertEquals(SamplingStrategyType.PROBABILISTIC, resp.strategyType)
        val prob = resp.probabilisticSampling
        assertNotNull(prob)
        assertEquals(0.5, prob.samplingRate)
    }

    @Test
    fun testInnerSamplingDecisions() {
        val inner = Inner(leakyBucketSize = 10.0)
        val traceId = TraceId.from(1uL, 2uL)

        // No strategy yet -> null
        assertEquals(null, inner.shouldSample(null, traceId, "test"))

        // Update to probabilistic
        inner.update(
            SamplingStrategyResponse(
                probabilisticSampling = ProbabilisticSamplingStrategy(1.0),
            ),
        )
        val res = inner.shouldSample(null, traceId, "test")
        assertNotNull(res)
        assertEquals(SamplingDecision.RECORD_AND_SAMPLE, res.decision)
    }

    @Test
    fun testJaegerRemoteSamplerBuilder() {
        val runtime = Tokio()
        val sampler =
            JaegerRemoteSampler
                .builder(runtime, "test-service")
                .withLeakyBucketSize(50.0)
                .build()

        assertNotNull(sampler)
    }
}
