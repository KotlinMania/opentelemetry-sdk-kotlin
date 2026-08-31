// port-lint: source opentelemetry_sdk/src/trace/runtime_tests.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Test helper span exporter that counts exported spans.
 */
@OptIn(ExperimentalAtomicApi::class)
public class SpanCountExporter : SpanExporter {
    public val spanCount: AtomicInt = AtomicInt(0)

    override fun export(batch: List<SpanData>): OTelSdkResult {
        spanCount.addAndFetch(batch.size)
        return Result.success(Unit)
    }

    public companion object {
        public fun new(): SpanCountExporter = SpanCountExporter()
    }
}
