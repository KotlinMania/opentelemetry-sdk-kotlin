// port-lint: source src/lib.rs
package io.github.kotlinmania.opentelemetrysdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class InMemoryExporterErrorTest {
    @Test
    fun internalFailureCarriesUpstreamMessageShape() {
        val err: InMemoryExporterError = InMemoryExporterError.InternalFailure("mutex poisoned")
        assertIs<InMemoryExporterError.InternalFailure>(err)
        assertEquals("mutex poisoned", err.reason)
        assertEquals("Unable to obtain telemetry. Reason: mutex poisoned", err.message)
    }

    @Test
    fun internalFailureEquatableByReason() {
        val a = InMemoryExporterError.InternalFailure("x")
        val b = InMemoryExporterError.InternalFailure("x")
        val c = InMemoryExporterError.InternalFailure("y")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        kotlin.test.assertNotEquals(a, c)
    }
}
