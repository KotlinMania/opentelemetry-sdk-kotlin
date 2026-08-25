// port-lint: source logs/mod.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.Context
import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import io.github.kotlinmania.opentelemetrysdk.trace.SpanId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceFlags
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ProcessorAndExporterTest {
    @Test
    fun loggingSdkTest() {
        val resource =
            Resource
                .builderEmpty()
                .withAttributes(
                    listOf(
                        KeyValue("k1", "v1"),
                        KeyValue("k2", "v2"),
                        KeyValue("k3", "v3"),
                        KeyValue("k4", "v4"),
                    ),
                ).build()
        val exporter = InMemoryLogExporter.defaultExporter()
        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withResource(resource)
                .withLogProcessor(SimpleLogProcessor.new(exporter))
                .build()

        val logger = loggerProvider.logger("test-logger")
        val logRecord = logger.createLogRecord()
        logRecord.setSeverityNumber(Severity.Error)
        logRecord.setSeverityText("Error")

        logRecord.addAttributes(
            listOf(
                LogAttribute("key1", "value1"),
                LogAttribute("key2", "value2"),
                LogAttribute("key3", "value3"),
                LogAttribute("key4", "value4"),
                LogAttribute("key5", "value5"),
                LogAttribute("key6", "value6"),
                LogAttribute("key7", "value7"),
                LogAttribute("key8", "value8"),
                LogAttribute("key9", "value9"),
                LogAttribute("key10", "value10"),
            ),
        )

        logger.emit(logRecord)

        val exportedLogs = exporter.getEmittedLogs().getOrThrow()
        assertEquals(1, exportedLogs.size)
        val log = exportedLogs.first()
        assertEquals("test-logger", log.instrumentation.name)
        assertEquals(Severity.Error, log.record.severityNumber)
        assertEquals(10, log.record.attributesLen())
        for (i in 1..10) {
            assertTrue(
                log.record.attributesContains(Key("key$i"), AnyValue.of("value$i")),
                "Expected key$i to equal value$i",
            )
        }
        assertEquals(resource, log.resource)
    }

    @Test
    fun loggerAttributes() {
        val exporter = InMemoryLogExporter.defaultExporter()
        val provider =
            SdkLoggerProvider
                .builder()
                .withLogProcessor(SimpleLogProcessor.new(exporter))
                .build()

        val scope =
            InstrumentationScope
                .builder("test_logger")
                .withSchemaUrl("https://opentelemetry.io/schema/1.0.0")
                .withAttributes(listOf(KeyValue("test_k", "test_v")))
                .build()

        val logger = provider.loggerWithScope(scope)
        val logRecord = logger.createLogRecord()
        logRecord.setSeverityNumber(Severity.Error)

        logger.emit(logRecord)

        val exportedLogs = exporter.getEmittedLogs().getOrThrow()
        assertEquals(1, exportedLogs.size)
        val log = exportedLogs.first()
        assertEquals(Severity.Error, log.record.severityNumber)
        assertEquals("test_logger", log.instrumentation.name)
        assertEquals("https://opentelemetry.io/schema/1.0.0", log.instrumentation.schemaUrl)
        assertEquals(listOf(KeyValue("test_k", "test_v")), log.instrumentation.attributes)
    }

    private class EnrichWithBaggageProcessor : LogProcessor {
        override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
            Context.mapCurrent { cx ->
                for (kv in cx.baggage) {
                    record.addAttribute(kv.key.name, kv.value.toString())
                }
            }
        }

        override fun forceFlush(): OTelSdkResult = Result.success(Unit)
    }

    @Test
    fun logAndBaggage() {
        val exporter = InMemoryLogExporter.defaultExporter()
        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withLogProcessor(EnrichWithBaggageProcessor())
                .withLogProcessor(SimpleLogProcessor.new(exporter))
                .build()

        val logger = loggerProvider.logger("test-logger")
        val contextWithBaggage = Context.currentWithBaggage(listOf(KeyValue("key-from-bag", "value-from-bag")))
        val guard = contextWithBaggage.attach()
        try {
            val logRecord = logger.createLogRecord()
            logRecord.addAttribute("key", "value")
            logger.emit(logRecord)
        } finally {
            guard.close()
        }

        val exportedLogs = exporter.getEmittedLogs().getOrThrow()
        assertEquals(1, exportedLogs.size)
        val log = exportedLogs.first()
        assertEquals("test-logger", log.instrumentation.name)
        assertEquals(2, log.record.attributesLen())
        assertTrue(log.record.attributesContains(Key("key"), AnyValue.of("value")))
        assertTrue(log.record.attributesContains(Key("key-from-bag"), AnyValue.of("value-from-bag")))
    }

    @Test
    fun logSuppression() {
        val exporter = InMemoryLogExporter.defaultExporter()
        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withSimpleExporter(exporter)
                .build()

        val logger = loggerProvider.logger("test-logger")
        val logRecord = logger.createLogRecord()
        val guard = Context.enterTelemetrySuppressedScope()
        try {
            logger.emit(logRecord)
        } finally {
            guard.close()
        }

        val exportedLogs = exporter.getEmittedLogs().getOrThrow()
        assertEquals(0, exportedLogs.size, "Logs emitted inside suppressed context must not be exported")
    }

    private class ReentrantLogProcessor(
        private var logger: SdkLogger? = null,
    ) : LogProcessor {
        fun setLogger(logger: SdkLogger) {
            this.logger = logger
        }

        override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
            val guard = Context.enterTelemetrySuppressedScope()
            try {
                val targetLogger = logger
                if (targetLogger != null) {
                    val recursiveRecord = targetLogger.createLogRecord()
                    recursiveRecord.setSeverityNumber(Severity.Error)
                    targetLogger.emit(recursiveRecord)
                }
            } finally {
                guard.close()
            }
        }

        override fun forceFlush(): OTelSdkResult = Result.success(Unit)
    }

    @Test
    fun processorInternalLogDoesNotDeadlockWithSuppressionEnabled() {
        val processor = ReentrantLogProcessor()
        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withLogProcessor(processor)
                .build()
        processor.setLogger(loggerProvider.logger("processor-logger"))

        val logger = loggerProvider.logger("test-logger")
        val logRecord = logger.createLogRecord()
        logRecord.setSeverityNumber(Severity.Error)
        logger.emit(logRecord)
    }

    @Test
    fun testLogDataModificationByMultipleProcessors() {
        val firstProcessorLogs = mutableListOf<OwnedLogData>()
        val secondProcessorLogs = mutableListOf<OwnedLogData>()

        val firstProcessor =
            object : LogProcessor {
                override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
                    record.addAttribute(Key("processed_by"), AnyValue.of("FirstProcessor"))
                    record.setBody(AnyValue.of("Updated by FirstProcessor"))
                    firstProcessorLogs.add(OwnedLogData(record.clone(), instrumentation))
                }

                override fun forceFlush(): OTelSdkResult = Result.success(Unit)
            }

        val secondProcessor =
            object : LogProcessor {
                override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
                    assertTrue(record.attributesContains(Key("processed_by"), AnyValue.of("FirstProcessor")))
                    assertEquals(AnyValue.of("Updated by FirstProcessor"), record.body)
                    secondProcessorLogs.add(OwnedLogData(record.clone(), instrumentation))
                }

                override fun forceFlush(): OTelSdkResult = Result.success(Unit)
            }

        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withLogProcessor(firstProcessor)
                .withLogProcessor(secondProcessor)
                .build()

        val logger = loggerProvider.logger("test-logger")
        val logRecord = logger.createLogRecord()
        logRecord.setBody(AnyValue.of("Test log"))
        logger.emit(logRecord)

        assertEquals(1, firstProcessorLogs.size)
        assertEquals(1, secondProcessorLogs.size)

        val firstLog = firstProcessorLogs[0]
        val secondLog = secondProcessorLogs[0]

        assertTrue(firstLog.record.attributesContains(Key("processed_by"), AnyValue.of("FirstProcessor")))
        assertTrue(secondLog.record.attributesContains(Key("processed_by"), AnyValue.of("FirstProcessor")))
        assertEquals(AnyValue.of("Updated by FirstProcessor"), firstLog.record.body)
        assertEquals(AnyValue.of("Updated by FirstProcessor"), secondLog.record.body)
    }

    @Test
    fun traceContextTest() {
        val exporter = InMemoryLogExporter.defaultExporter()
        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withSimpleExporter(exporter)
                .build()

        val logger = loggerProvider.logger("test-logger")

        val explicitCtxt =
            TraceContext(
                traceId = TraceId(0u, 13u),
                spanId = SpanId(14u),
                traceFlags = TraceFlags.SAMPLED,
            )

        val logRecord = logger.createLogRecord()
        logRecord.setBody(AnyValue.of("explicit"))
        logRecord.setTraceContext(
            explicitCtxt.traceId,
            explicitCtxt.spanId,
            explicitCtxt.traceFlags,
        )
        logger.emit(logRecord)

        val emitted = exporter.getEmittedLogs().getOrThrow()
        assertEquals(1, emitted.size)
        assertEquals(AnyValue.of("explicit"), emitted[0].record.body)
        val emittedContext = emitted[0].record.traceContext
        assertNotNull(emittedContext)
        assertEquals(explicitCtxt.traceId, emittedContext.traceId)
        assertEquals(explicitCtxt.spanId, emittedContext.spanId)
    }

    @Test
    fun shutdownIdempotentTest() {
        var count = 0
        val processor =
            object : LogProcessor {
                override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
                    count += 1
                }

                override fun forceFlush(): OTelSdkResult = Result.success(Unit)

                override fun shutdown(): OTelSdkResult = Result.success(Unit)
            }

        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withLogProcessor(processor)
                .build()

        val logger = loggerProvider.logger("test-logger")
        logger.emit(logger.createLogRecord())
        assertEquals(1, count)

        val shutdown1 = loggerProvider.shutdown()
        assertTrue(shutdown1.isSuccess)

        // Subsequent shutdowns return failure (AlreadyShutdown)
        val shutdown2 = loggerProvider.shutdown()
        assertFalse(shutdown2.isSuccess)

        // Subsequent emission produces noop logger
        val loggerAfterShutdown = loggerProvider.logger("test-logger-after")
        loggerAfterShutdown.emit(loggerAfterShutdown.createLogRecord())
        assertEquals(1, count)
    }

    @Test
    fun batchLogProcessorTest() {
        val exporter = InMemoryLogExporter.defaultExporter()
        val processor =
            BatchLogProcessor
                .builder(exporter)
                .withBatchConfig(BatchConfig(maxExportBatchSize = 2))
                .build()

        val loggerProvider =
            SdkLoggerProvider
                .builder()
                .withLogProcessor(processor)
                .build()

        val logger = loggerProvider.logger("batch-logger")
        val r1 = logger.createLogRecord()
        r1.setBody(AnyValue.of("r1"))
        val r2 = logger.createLogRecord()
        r2.setBody(AnyValue.of("r2"))

        logger.emit(r1)
        logger.emit(r2)

        val emitted = exporter.getEmittedLogs().getOrThrow()
        assertEquals(2, emitted.size)
        assertEquals(AnyValue.of("r1"), emitted[0].record.body)
        assertEquals(AnyValue.of("r2"), emitted[1].record.body)
    }
}
