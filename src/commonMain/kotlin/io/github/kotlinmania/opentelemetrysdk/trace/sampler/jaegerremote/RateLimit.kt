// port-lint: source opentelemetry_sdk/src/trace/sampler/jaeger_remote/rate_limit.rs
package io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote

import kotlin.math.min
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Leaky bucket based rate limiter for Jaeger remote sampler.
 */
internal class LeakyBucket(
    val bucketSize: Double,
    var spanPerSec: Double,
) {
    var available: Double = bucketSize
    var lastTime: Instant = Clock.System.now()

    fun update(spanPerSec: Double) {
        this.spanPerSec = spanPerSec
    }

    fun shouldSample(): Boolean =
        checkAvailability { Clock.System.now() }

    fun checkAvailability(now: () -> Instant): Boolean {
        if (available >= 1.0) {
            available -= 1.0
            return true
        }
        val curTime = now()
        val elapsed = curTime - lastTime
        if (elapsed < Duration.ZERO) {
            // Clock rewind detected
            return true
        }
        lastTime = curTime
        available =
            min(
                elapsed.inWholeMilliseconds.toDouble() / 1000.0 * spanPerSec + available,
                bucketSize,
            )
        return if (available >= 1.0) {
            available -= 1.0
            true
        } else {
            false
        }
    }
}
