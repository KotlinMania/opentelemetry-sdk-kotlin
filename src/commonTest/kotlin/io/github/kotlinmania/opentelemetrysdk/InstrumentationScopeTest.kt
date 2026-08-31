// port-lint: source opentelemetry_sdk/src/lib.rs
package io.github.kotlinmania.opentelemetrysdk

import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InstrumentationScopeTest {
    @Test
    fun testBuilder() {
        val scope =
            InstrumentationScope
                .builder("test-scope")
                .withVersion("1.0.0")
                .withSchemaUrl("https://opentelemetry.io/schemas/1.0.0")
                .withAttributes(listOf(KeyValue(Key("scope.attr"), Value.of("scope-val"))))
                .build()

        assertEquals("test-scope", scope.name)
        assertEquals("1.0.0", scope.version)
        assertEquals("https://opentelemetry.io/schemas/1.0.0", scope.schemaUrl)
        assertEquals(1, scope.attributes.size)
    }

    @Test
    fun testEmpty() {
        val scope = InstrumentationScope.EMPTY
        assertEquals("", scope.name)
        assertNull(scope.version)
        assertNull(scope.schemaUrl)
        assertEquals(0, scope.attributes.size)
    }
}
