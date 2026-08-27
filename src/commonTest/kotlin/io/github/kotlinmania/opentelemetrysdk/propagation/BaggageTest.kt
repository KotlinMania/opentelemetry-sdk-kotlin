// port-lint: tests propagation/baggage.rs
package io.github.kotlinmania.opentelemetrysdk.propagation

import io.github.kotlinmania.opentelemetrysdk.Context
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaggageTest {
    private class MapCarrier(
        private val map: MutableMap<String, String> = mutableMapOf(),
    ) : Extractor,
        Injector {
        override fun get(key: String): String? = map[key]

        override fun keys(): List<String> = map.keys.toList()

        override fun set(key: String, value: String) {
            map[key] = value
        }
    }

    @Test
    fun extractBaggage() {
        val propagator = BaggagePropagator.new()
        for ((headerValue, kvs) in validExtractData()) {
            val carrier = MapCarrier(mutableMapOf(BaggagePropagator.BAGGAGE_HEADER to headerValue))
            val context = propagator.extract(carrier)
            val baggage = context.baggage
            val baggageMap = baggage.associate { it.key.name to it.value.toString() }
            for ((key, value) in kvs) {
                assertEquals(value, baggageMap[key], "Mismatch for header $headerValue, key $key")
            }
        }
    }

    @Test
    fun extractBaggageWithMetadata() {
        val propagator = BaggagePropagator.new()
        for ((headerValue, kvs) in validExtractDataWithMetadata()) {
            val carrier = MapCarrier(mutableMapOf(BaggagePropagator.BAGGAGE_HEADER to headerValue))
            val context = propagator.extract(carrier)
            val baggage = context.baggage
            val baggageMap = baggage.associate { it.key.name to it.value.toString() }
            for ((key, value) in kvs) {
                assertEquals(value, baggageMap[key], "Mismatch for header $headerValue, key $key")
            }
        }
    }

    @Test
    fun injectBaggage() {
        val propagator = BaggagePropagator.new()
        for ((kvs, headerParts) in validInjectData()) {
            val context = Context(baggage = kvs)
            val carrier = MapCarrier()
            propagator.injectContext(context, carrier)
            val headerValue = carrier.get(BaggagePropagator.BAGGAGE_HEADER)
            assertTrue(headerValue != null, "Header must be present")
            for (headerPart in headerParts) {
                assertTrue(
                    headerValue.contains(headerPart),
                    "Header '$headerValue' should contain '$headerPart'",
                )
            }
        }
    }

    @Test
    fun injectBaggageWithMetadata() {
        val propagator = BaggagePropagator.new()
        for ((kvs, headerParts) in validInjectDataMetadata()) {
            val context = Context(baggage = kvs)
            val carrier = MapCarrier()
            propagator.injectContext(context, carrier)
            val headerValue = carrier.get(BaggagePropagator.BAGGAGE_HEADER)
            assertTrue(headerValue != null, "Header must be present")
            for (headerPart in headerParts) {
                assertTrue(
                    headerValue.contains(headerPart),
                    "Header '$headerValue' should contain '$headerPart'",
                )
            }
        }
    }

    private fun validExtractData(): List<Pair<String, Map<String, String>>> =
        listOf(
            "key1=val1,key2=val2" to mapOf("key1" to "val1", "key2" to "val2"),
            "key1 =   val1,  key2 =val2   " to mapOf("key1" to "val1", "key2" to "val2"),
            "key1=val1,key2=val2%2Cval3" to mapOf("key1" to "val1", "key2" to "val2,val3"),
            "key1=val1,key2=val2,a,val3" to mapOf("key1" to "val1", "key2" to "val2"),
            "key1=,key2=val2" to mapOf("key1" to "", "key2" to "val2"),
        )

    private fun validExtractDataWithMetadata(): List<Pair<String, Map<String, String>>> =
        listOf(
            "key1=val1,key2=val2;prop=1" to mapOf("key1" to "val1", "key2" to "val2"),
            "key1=val1,key2=val2;prop1" to mapOf("key1" to "val1", "key2" to "val2"),
            "key1=value1;property1;property2, key2 = value2, key3=value3; propertyKey=propertyValue" to
                mapOf("key1" to "value1", "key2" to "value2", "key3" to "value3"),
        )

    private fun validInjectData(): List<Pair<List<KeyValue>, List<String>>> =
        listOf(
            listOf(KeyValue("key1", "val1"), KeyValue("key2", "val2")) to listOf("key1=val1", "key2=val2"),
            listOf(KeyValue("key1", "val1,val2"), KeyValue("key2", "val3=4")) to listOf("key1=val1%2Cval2", "key2=val3%3D4"),
            listOf(
                KeyValue("key1", true),
                KeyValue("key2", 123L),
                KeyValue("key3", 123.567),
            ) to listOf("key1=true", "key2=123", "key3=123.567"),
        )

    private fun validInjectDataMetadata(): List<Pair<List<KeyValue>, List<String>>> =
        listOf(
            listOf(
                KeyValue("key1", "val1"),
                KeyValue("key2", "val2"),
                KeyValue("key3", "val3"),
            ) to listOf("key1=val1", "key2=val2", "key3=val3"),
        )
}
