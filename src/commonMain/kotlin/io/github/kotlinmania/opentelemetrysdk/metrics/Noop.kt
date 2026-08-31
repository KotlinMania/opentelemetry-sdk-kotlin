// port-lint: source opentelemetry_sdk/src/metrics/noop.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue

/**
 * A no-op instance of a Meter.
 */
public class NoopMeter {
    /**
     * Returns true indicating this is a no-op meter.
     */
    public fun isNoop(): Boolean = true

    public companion object {
        /**
         * Creates a new no-op meter.
         */
        public fun new(): NoopMeter = NoopMeter()
    }
}

/**
 * A no-op sync instrument.
 */
public class NoopSyncInstrument {
    /**
     * Measures a value with attributes (ignored).
     */
    public fun <T> measure(value: T, attributes: List<KeyValue>) {
        // Ignored
    }

    public companion object {
        /**
         * Creates a new no-op sync instrument.
         */
        public fun new(): NoopSyncInstrument = NoopSyncInstrument()
    }
}
