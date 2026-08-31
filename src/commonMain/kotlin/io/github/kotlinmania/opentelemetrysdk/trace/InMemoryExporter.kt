// port-lint: source opentelemetry_sdk/src/trace/in_memory_exporter.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration

/**
 * An in-memory span exporter that stores span data in memory.
 */
@OptIn(ExperimentalAtomicApi::class)
public class InMemorySpanExporter internal constructor(
    private val shouldResetOnShutdown: Boolean = true,
) : SpanExporter {
    private val spansRef = AtomicReference<List<SpanData>>(emptyList())
    private val resourceRef = AtomicReference(Resource.builder().build())
    private val shutdownCalledRef = AtomicBoolean(false)

    public fun isShutdownCalled(): Boolean = shutdownCalledRef.load()

    public fun getFinishedSpans(): Result<List<SpanData>> = Result.success(spansRef.load())

    public fun reset() {
        spansRef.store(emptyList())
    }

    override fun export(batch: List<SpanData>): OTelSdkResult {
        while (true) {
            val current = spansRef.load()
            val next = current + batch
            if (spansRef.compareAndSet(current, next)) {
                break
            }
        }
        return Result.success(Unit)
    }

    override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        shutdownCalledRef.store(true)
        if (shouldResetOnShutdown) {
            reset()
        }
        return Result.success(Unit)
    }

    override fun setResource(resource: Resource) {
        resourceRef.store(resource)
    }

    public companion object {
        public fun builder(): InMemorySpanExporterBuilder = InMemorySpanExporterBuilder()

        public fun defaultExporter(): InMemorySpanExporter = builder().build()
    }
}

/**
 * Builder for [InMemorySpanExporter].
 */
public class InMemorySpanExporterBuilder {
    private var resetOnShutdown: Boolean = true

    public fun resetOnShutdown(reset: Boolean): InMemorySpanExporterBuilder {
        this.resetOnShutdown = reset
        return this
    }

    public fun build(): InMemorySpanExporter = InMemorySpanExporter(resetOnShutdown)
}
