// port-lint: tests logs/log_processor.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class MockLogExporter(
    private var resource: Resource? = null,
) : LogExporter {
    override fun export(batch: LogBatch): OTelSdkResult = Result.success(Unit)

    override fun setResource(resource: Resource) {
        this.resource = resource
    }

    public fun getResource(): Resource? = resource
}

internal class FirstProcessor(
    val logs: MutableList<Pair<SdkLogRecord, InstrumentationScope>>,
) : LogProcessor {
    override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
        record.addAttribute(Key("processed_by"), AnyValue.of("FirstProcessor"))
        record.setBody(AnyValue.of("Updated by FirstProcessor"))
        logs.add(Pair(record.clone(), instrumentation))
    }

    override fun forceFlush(): OTelSdkResult = Result.success(Unit)
}

internal class SecondProcessor(
    val logs: MutableList<Pair<SdkLogRecord, InstrumentationScope>>,
) : LogProcessor {
    override fun emit(record: SdkLogRecord, instrumentation: InstrumentationScope) {
        assertTrue(
            record.attributesContains(Key("processed_by"), AnyValue.of("FirstProcessor")),
        )
        assertEquals(
            AnyValue.of("Updated by FirstProcessor"),
            record.body,
        )
        logs.add(Pair(record.clone(), instrumentation))
    }

    override fun forceFlush(): OTelSdkResult = Result.success(Unit)
}

public class LogProcessorTest {
    @Test
    public fun testLogDataModificationByMultipleProcessors() {
        val firstProcessorLogs = mutableListOf<Pair<SdkLogRecord, InstrumentationScope>>()
        val secondProcessorLogs = mutableListOf<Pair<SdkLogRecord, InstrumentationScope>>()

        val firstProcessor = FirstProcessor(firstProcessorLogs)
        val secondProcessor = SecondProcessor(secondProcessorLogs)

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

        assertTrue(
            firstLog.first.attributesContains(Key("processed_by"), AnyValue.of("FirstProcessor")),
        )
        assertTrue(
            secondLog.first.attributesContains(Key("processed_by"), AnyValue.of("FirstProcessor")),
        )

        assertEquals(
            AnyValue.of("Updated by FirstProcessor"),
            firstLog.first.body,
        )
        assertEquals(
            AnyValue.of("Updated by FirstProcessor"),
            secondLog.first.body,
        )
    }
}
