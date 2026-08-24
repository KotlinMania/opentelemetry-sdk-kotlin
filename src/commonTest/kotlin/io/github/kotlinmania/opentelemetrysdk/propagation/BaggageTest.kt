// port-lint: tests propagation/baggage.rs
package io.github.kotlinmania.opentelemetrysdk.propagation

import io.github.kotlinmania.opentelemetrysdk.Context
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaggageTest {

    private class MapCarrier(
        private val map: MutableMap<String, String> = mutableMapOf(),
    ) : Extractor, Injector {
        override fun get(key: String): String? = map[key]
        override fun keys(): List<String> = map.keys.toList()
        override fun set(key: String, value: String) {
            map[key] = value
        }
    }

    @Test
    fun extractBaggage() {
        val propagator = BaggagePropagator()
        val carrier = MapCarrier(mutableMapOf(
            BaggagePropagator.BAGGAGE_HEADER to "key1=val1,key2=val2",
        ))

        val context = propagator.extract(carrier)
        val baggage = context.baggage
        assertEquals(2, baggage.size)
        assertEquals("key1", baggage[0].key.name)
        assertEquals("val1", baggage[0].value.toString())
        assertEquals("key2", baggage[1].key.name)
        assertEquals("val2", baggage[1].value.toString())
    }

    @Test
    fun extractBaggageWithSpacesAndEscapes() {
        val propagator = BaggagePropagator()
        val carrier = MapCarrier(mutableMapOf(
            BaggagePropagator.BAGGAGE_HEADER to "key1 = val1, key2 = val2%2Cval3",
        ))

        val context = propagator.extract(carrier)
        val baggage = context.baggage
        assertEquals(2, baggage.size)
        assertEquals("key1", baggage[0].key.name)
        assertEquals("val1", baggage[0].value.toString())
        assertEquals("key2", baggage[1].key.name)
        assertEquals("val2,val3", baggage[1].value.toString())
    }

    @Test
    fun extractBaggageWithMetadata() {
        val propagator = BaggagePropagator()
        val carrier = MapCarrier(mutableMapOf(
            BaggagePropagator.BAGGAGE_HEADER to "key1=val1,key2=val2;prop=1",
        ))

        val context = propagator.extract(carrier)
        val baggage = context.baggage
        assertEquals(2, baggage.size)
        assertEquals("key1", baggage[0].key.name)
        assertEquals("val1", baggage[0].value.toString())
        assertEquals("key2", baggage[1].key.name)
        assertEquals("val2", baggage[1].value.toString())
    }

    @Test
    fun injectBaggage() {
        val propagator = BaggagePropagator()
        val context = Context(
            baggage = listOf(
                KeyValue("key1", "val1"),
                KeyValue("key2", "val2"),
            ),
        )

        val carrier = MapCarrier()
        propagator.injectContext(context, carrier)

        val header = carrier.get(BaggagePropagator.BAGGAGE_HEADER)
        assertTrue(header != null)
        assertTrue(header.contains("key1=val1"))
        assertTrue(header.contains("key2=val2"))
    }

    @Test
    fun injectBaggageEscaped() {
        val propagator = BaggagePropagator()
        val context = Context(
            baggage = listOf(
                KeyValue("key1", "val1,val2"),
                KeyValue("key2", "val3=4"),
            ),
        )

        val carrier = MapCarrier()
        propagator.injectContext(context, carrier)

        val header = carrier.get(BaggagePropagator.BAGGAGE_HEADER)
        assertTrue(header != null)
        assertTrue(header.contains("key1=val1%2Cval2"))
        assertTrue(header.contains("key2=val3%3D4"))
    }
}
