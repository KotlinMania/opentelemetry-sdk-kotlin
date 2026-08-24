// port-lint: tests resource/env.rs
package io.github.kotlinmania.opentelemetrysdk.resource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EnvTest {
    @Test
    fun testReadFromEnv() {
        val env =
            mapOf(
                "OTEL_RESOURCE_ATTRIBUTES" to "key=value, k = v , a= x, a=z,base64=SGVsbG8sIFdvcmxkIQ==",
                "IRRELEVANT" to "20200810",
            )
        val detector = EnvResourceDetector(getEnv = { env[it] })
        val resource = detector.detect()
        assertEquals(
            Resource
                .builderEmpty()
                .withAttributes(
                    listOf(
                        KeyValue("key", "value"),
                        KeyValue("k", "v"),
                        KeyValue("a", "x"),
                        KeyValue("a", "z"),
                        KeyValue("base64", "SGVsbG8sIFdvcmxkIQ=="),
                    ),
                ).build(),
            resource,
        )

        val emptyDetector = EnvResourceDetector(getEnv = { null })
        val emptyResource = emptyDetector.detect()
        assertTrue(emptyResource.isEmpty())
    }

    @Test
    fun testSdkProvidedResourceDetector() {
        val noEnv = SdkProvidedResourceDetector(getEnv = { null }).detect()
        assertEquals(
            Value.of("unknown_service"),
            noEnv.get(Key(SERVICE_NAME)),
        )

        val withServiceEnv =
            mapOf(
                OTEL_SERVICE_NAME to "test service",
            )
        val withService = SdkProvidedResourceDetector(getEnv = { withServiceEnv[it] }).detect()
        assertEquals(
            Value.of("test service"),
            withService.get(Key(SERVICE_NAME)),
        )

        val withAttrsEnv =
            mapOf(
                OTEL_RESOURCE_ATTRIBUTES to "service.name=test service1",
            )
        val withAttrs = SdkProvidedResourceDetector(getEnv = { withAttrsEnv[it] }).detect()
        assertEquals(
            Value.of("test service1"),
            withAttrs.get(Key(SERVICE_NAME)),
        )

        // OTEL_SERVICE_NAME takes priority
        val priorityEnv =
            mapOf(
                OTEL_SERVICE_NAME to "test service",
                OTEL_RESOURCE_ATTRIBUTES to "service.name=test service3",
            )
        val withPriority = SdkProvidedResourceDetector(getEnv = { priorityEnv[it] }).detect()
        assertEquals(
            Value.of("test service"),
            withPriority.get(Key(SERVICE_NAME)),
        )
    }
}
