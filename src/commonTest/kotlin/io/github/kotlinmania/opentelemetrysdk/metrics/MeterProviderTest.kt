// port-lint: tests metrics/meter_provider.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import io.github.kotlinmania.opentelemetrysdk.resource.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MeterProviderTest {
    @Test
    fun testMeterProviderResource() {
        val reader = ManualReader.builder().build()
        val customMeterProvider =
            SdkMeterProvider
                .builder()
                .withReader(reader)
                .withResource(
                    Resource
                        .builderEmpty()
                        .withServiceName("test_service")
                        .build(),
                ).build()

        assertEquals("test_service", customMeterProvider.resource.serviceName())
        assertEquals(1, customMeterProvider.resource.size)
    }

    @Test
    fun testMeterProviderShutdown() {
        val reader = ManualReader.builder().build()
        val provider =
            SdkMeterProvider
                .builder()
                .withReader(reader)
                .build()

        val shutdownRes = provider.shutdown()
        assertTrue(shutdownRes.isSuccess)

        // shutdown once more should return an error
        val shutdownRes2 = provider.shutdown()
        assertTrue(shutdownRes2.isFailure)
        assertEquals(OTelSdkError.AlreadyShutdown, shutdownRes2.exceptionOrNull())
    }

    @Test
    fun withResourceMultipleCallsEnsureAdditive() {
        val resource =
            Resource
                .builderEmpty()
                .withAttributes(listOf(KeyValue("key1", "value1")))
                .build()
                .merge(
                    Resource
                        .builderEmpty()
                        .withAttributes(listOf(KeyValue("key2", "value2")))
                        .build(),
                ).merge(
                    Resource
                        .builderEmpty()
                        .withSchemaUrl(emptyList(), "http://example.com")
                        .build(),
                ).merge(
                    Resource
                        .builderEmpty()
                        .withAttributes(listOf(KeyValue("key3", "value3")))
                        .build(),
                )

        val provider =
            SdkMeterProvider
                .builder()
                .withResource(resource)
                .build()

        assertEquals(
            Value.StringValue("value1"),
            provider.resource.get(Key.fromStaticStr("key1")),
        )
        assertEquals(
            Value.StringValue("value2"),
            provider.resource.get(Key.fromStaticStr("key2")),
        )
        assertEquals(
            Value.StringValue("value3"),
            provider.resource.get(Key.fromStaticStr("key3")),
        )
        assertEquals("http://example.com", provider.resource.schemaUrl)
    }
}
