// port-lint: source src/context.rs
package io.github.kotlinmania.opentelemetrysdk

import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * An execution-scoped collection of values.
 */
@OptIn(ExperimentalAtomicApi::class)
public class Context(
    public val entries: Map<String, Any> = emptyMap(),
    public val baggage: List<KeyValue> = emptyList(),
    public val suppressTelemetry: Boolean = false,
) {
    public fun withValue(key: String, value: Any): Context =
        Context(entries + (key to value), baggage, suppressTelemetry)

    public fun get(key: String): Any? = entries[key]

    public fun withBaggage(baggage: Iterable<KeyValue>): Context =
        Context(entries, this.baggage + baggage, suppressTelemetry)

    public fun withTelemetrySuppressed(): Context =
        Context(entries, baggage, suppressTelemetry = true)

    public fun attach(): ContextGuard {
        val prev = currentContextRef.load()
        currentContextRef.store(this)
        return ContextGuard(prev)
    }

    public companion object {
        private val currentContextRef: AtomicReference<Context> = AtomicReference(Context())

        public fun current(): Context = currentContextRef.load()

        public fun currentWithBaggage(baggage: Iterable<KeyValue>): Context =
            current().withBaggage(baggage)

        public fun currentWithValue(key: String, value: Any): Context =
            current().withValue(key, value)

        public fun isCurrentTelemetrySuppressed(): Boolean = current().suppressTelemetry

        public fun enterTelemetrySuppressedScope(): ContextGuard =
            current().withTelemetrySuppressed().attach()

        public fun <T> mapCurrent(transform: (Context) -> T): T =
            transform(current())

        internal fun restore(previous: Context) {
            currentContextRef.store(previous)
        }
    }
}

/**
 * A guard that restores the previous [Context] when closed.
 */
public class ContextGuard internal constructor(
    private val previous: Context,
) : AutoCloseable {
    override fun close() {
        Context.restore(previous)
    }
}
