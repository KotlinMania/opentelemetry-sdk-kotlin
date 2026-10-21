// port-lint: tests metrics/meter_provider.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetry.InstrumentationScope
import io.github.kotlinmania.opentelemetry.Key
import io.github.kotlinmania.opentelemetry.KeyValue
import io.github.kotlinmania.opentelemetry.Value
import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
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
            Value.of("value1"),
            provider.resource.get(Key.fromStaticStr("key1")),
        )
        assertEquals(
            Value.of("value2"),
            provider.resource.get(Key.fromStaticStr("key2")),
        )
        assertEquals(
            Value.of("value3"),
            provider.resource.get(Key.fromStaticStr("key3")),
        )
        assertEquals("http://example.com", provider.resource.schemaUrl)
    }

    @Test
    fun sameMeterReusedSameScope() {
        val provider = SdkMeterProvider.builder().build()
        val meter1 = provider.meter("test")
        val meter2 = provider.meter("test")
        assertEquals(meter1, meter2)
        assertEquals(1, provider.meters.load().size)

        val scope =
            InstrumentationScope
                .builder("test")
                .withVersion("1.0.0")
                .withSchemaUrl("http://example.com")
                .build()

        val meter3 = provider.meterWithScope(scope)
        val meter4 = provider.meterWithScope(scope)
        assertEquals(meter3, meter4)
        assertEquals(2, provider.meters.load().size)

        fun makeScope(name: String) =
            InstrumentationScope
                .builder(name)
                .withVersion("1.0.0")
                .withSchemaUrl("http://example.com")
                .build()

        provider.meterWithScope(makeScope("ABC"))
        provider.meterWithScope(makeScope("Abc"))
        provider.meterWithScope(makeScope("abc"))

        assertEquals(5, provider.meters.load().size)
    }

    @Test
    fun sameMeterReusedSameScopeAttributes() {
        val provider = SdkMeterProvider.builder().build()

        fun makeScope(attributes: List<KeyValue>) =
            InstrumentationScope
                .builder("test.meter")
                .withVersion("v0.1.0")
                .withSchemaUrl("http://example.com")
                .withAttributes(attributes)
                .build()

        val meter1 = provider.meterWithScope(makeScope(listOf(KeyValue("key", "value1"))))
        val meter2 = provider.meterWithScope(makeScope(listOf(KeyValue("key", "value1"))))
        assertEquals(meter1, meter2)
        assertEquals(1, provider.meters.load().size)

        val meter3 =
            provider.meterWithScope(
                makeScope(
                    listOf(
                        KeyValue("key1", "value1"),
                        KeyValue("key2", "value2"),
                    ),
                ),
            )
        val meter4 =
            provider.meterWithScope(
                makeScope(
                    listOf(
                        KeyValue("key2", "value2"),
                        KeyValue("key1", "value1"),
                    ),
                ),
            )
        assertEquals(meter3, meter4)
        assertEquals(2, provider.meters.load().size)
    }

    @Test
    fun differentMeterDifferentAttributes() {
        val provider = SdkMeterProvider.builder().build()

        fun makeScope(attributes: List<KeyValue>) =
            InstrumentationScope
                .builder("test.meter")
                .withVersion("v0.1.0")
                .withSchemaUrl("http://example.com")
                .withAttributes(attributes)
                .build()

        provider.meterWithScope(makeScope(emptyList()))
        provider.meterWithScope(makeScope(listOf(KeyValue("key1", "value1"))))
        provider.meterWithScope(makeScope(listOf(KeyValue("Key1", "value1"))))
        provider.meterWithScope(makeScope(listOf(KeyValue("key1", "Value1"))))
        provider.meterWithScope(
            makeScope(
                listOf(
                    KeyValue("key1", "value1"),
                    KeyValue("key2", "value2"),
                ),
            ),
        )

        assertEquals(5, provider.meters.load().size)
    }

    // Note: upstream testShutdownInvokedOnLastDrop tests Rust RAII Arc Drop semantics which does not apply to Kotlin GC runtimes.
}
