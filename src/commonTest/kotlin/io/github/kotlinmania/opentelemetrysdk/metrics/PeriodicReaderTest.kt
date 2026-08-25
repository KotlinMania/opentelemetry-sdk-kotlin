// port-lint: tests metrics/periodic_reader.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class PeriodicReaderTest {
    @Test
    fun periodicReaderCreationAndConfiguration() {
        val exporter = InMemoryMetricExporter.default()
        val reader =
            PeriodicReader
                .builder(exporter)
                .withInterval(30.seconds)
                .build()

        assertEquals(30.seconds, reader.intervalDuration)
        assertEquals(Temporality.Cumulative, reader.temporality(InstrumentKind.Counter))
    }
}
