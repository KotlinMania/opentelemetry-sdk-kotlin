package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.Tokio
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ConcurrentLogProcessorTest {
    @Test
    fun testSimpleConcurrentLogProcessor() {
        val exporter = InMemoryLogExporter.defaultExporter()
        val processor = SimpleConcurrentLogProcessor.new(exporter)

        val record =
            SdkLogRecord(
                body = AnyValue.of("concurrent-test"),
                severityNumber = Severity.Info,
            )
        val scope = InstrumentationScope("test-scope")

        processor.emit(record, scope)
        val exported = exporter.getEmittedLogs().getOrThrow()
        assertEquals(1, exported.size)
        assertEquals(AnyValue.of("concurrent-test"), exported[0].record.body)

        assertTrue(processor.forceFlush().isSuccess)
        assertTrue(processor.shutdownWithTimeout(1.seconds).isSuccess)
    }

    @Test
    fun testAsyncBatchLogProcessor() {
        val exporter = InMemoryLogExporter.defaultExporter()
        val runtime = Tokio()
        val processor =
            AsyncBatchLogProcessor
                .builder(exporter, runtime)
                .withBatchConfig(BatchConfig(maxExportBatchSize = 1))
                .build()

        val record =
            SdkLogRecord(
                body = AnyValue.of("async-batch-test"),
                severityNumber = Severity.Info,
            )
        val scope = InstrumentationScope("async-scope")

        processor.emit(record, scope)
        assertTrue(processor.forceFlush().isSuccess)
        assertTrue(processor.shutdownWithTimeout(1.seconds).isSuccess)
    }
}
