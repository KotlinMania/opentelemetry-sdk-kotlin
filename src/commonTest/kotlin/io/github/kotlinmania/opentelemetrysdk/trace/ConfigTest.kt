// port-lint: source opentelemetry_sdk/src/trace/config.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConfigTest {
    @Test
    fun testDefaultConfig() {
        val config = Config.defaultConfig()
        assertEquals(128u, config.spanLimits.maxAttributesPerSpan)
        assertEquals(128u, config.spanLimits.maxEventsPerSpan)
        assertEquals(128u, config.spanLimits.maxLinksPerSpan)
        assertTrue(config.sampler is Sampler.ParentBased)
    }

    @Test
    fun testConfigWithEnvOverrides() {
        val fakeEnv =
            mapOf(
                Config.OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT to "64",
                Config.OTEL_SPAN_EVENT_COUNT_LIMIT to "32",
                Config.OTEL_SPAN_LINK_COUNT_LIMIT to "16",
                Config.OTEL_TRACES_SAMPLER to "always_off",
            )
        val config = Config.defaultConfig { fakeEnv[it] }
        assertEquals(64u, config.spanLimits.maxAttributesPerSpan)
        assertEquals(32u, config.spanLimits.maxEventsPerSpan)
        assertEquals(16u, config.spanLimits.maxLinksPerSpan)
        assertTrue(config.sampler is Sampler.AlwaysOff)
    }
}
