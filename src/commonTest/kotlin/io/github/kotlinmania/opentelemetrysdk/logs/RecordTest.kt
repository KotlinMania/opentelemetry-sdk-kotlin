// port-lint: source logs/record.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetry.Key
import io.github.kotlinmania.opentelemetry.SpanId
import io.github.kotlinmania.opentelemetry.TraceFlags
import io.github.kotlinmania.opentelemetry.TraceId
import io.github.kotlinmania.opentelemetry.logs.AnyValue
import io.github.kotlinmania.opentelemetry.logs.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

class RecordTest {
    @Test
    fun testSetEventName() {
        val logRecord = SdkLogRecord.new()
        logRecord.setEventName("test_event")
        assertEquals("test_event", logRecord.eventName)
    }

    @Test
    fun testSetTarget() {
        val logRecord = SdkLogRecord.new()
        logRecord.setTarget("foo::bar")
        assertEquals("foo::bar", logRecord.target)
    }

    @Test
    fun testSetTimestamp() {
        val logRecord = SdkLogRecord.new()
        val now = Clock.System.now()
        logRecord.setTimestamp(now)
        assertEquals(now, logRecord.timestamp)
    }

    @Test
    fun testSetObservedTimestamp() {
        val logRecord = SdkLogRecord.new()
        val now = Clock.System.now()
        logRecord.setObservedTimestamp(now)
        assertEquals(now, logRecord.observedTimestamp)
    }

    @Test
    fun testSetSeverityText() {
        val logRecord = SdkLogRecord.new()
        logRecord.setSeverityText("ERROR")
        assertEquals("ERROR", logRecord.severityText)
    }

    @Test
    fun testSetSeverityNumber() {
        val logRecord = SdkLogRecord.new()
        logRecord.setSeverityNumber(Severity.Error)
        assertEquals(Severity.Error, logRecord.severityNumber)
    }

    @Test
    fun testSetBody() {
        val logRecord = SdkLogRecord.new()
        val body = AnyValue.of("Test body")
        logRecord.setBody(body)
        assertEquals(body, logRecord.body)
    }

    @Test
    fun testSetAttributes() {
        val logRecord = SdkLogRecord.new()
        val attributes = listOf(LogAttribute(Key("key"), AnyValue.of("value")))
        logRecord.addAttributes(attributes)
        for (attr in attributes) {
            assertTrue(logRecord.attributesContains(attr.key, attr.value))
        }
    }

    @Test
    fun testSetAttribute() {
        val logRecord = SdkLogRecord.new()
        logRecord.addAttribute("key", "value")
        val key = Key("key")
        val value = AnyValue.of("value")
        assertTrue(logRecord.attributesContains(key, value))
    }

    @Test
    fun compareTraceContext() {
        val traceContext =
            TraceContext(
                traceId = TraceId.fromHex("00000000000000010000000000000001"),
                spanId = SpanId.from(1L),
                traceFlags = TraceFlags.NOT_SAMPLED,
            )
        val traceContextCloned = traceContext.copy()
        assertEquals(traceContext, traceContextCloned)

        val traceContextDifferent =
            TraceContext(
                traceId = TraceId.fromHex("00000000000000020000000000000002"),
                spanId = SpanId.from(2L),
                traceFlags = TraceFlags.NOT_SAMPLED,
            )
        assertNotEquals(traceContext, traceContextDifferent)
    }

    @Test
    fun compareLogRecord() {
        val now = Clock.System.now()
        val logRecord =
            SdkLogRecord(
                eventName = "test_event",
                target = "foo::bar",
                timestamp = now,
                observedTimestamp = now,
                severityText = "ERROR",
                severityNumber = Severity.Error,
                body = AnyValue.of("Test body"),
                traceContext =
                    TraceContext(
                        traceId = TraceId.fromHex("00000000000000010000000000000001"),
                        spanId = SpanId.from(1L),
                        traceFlags = TraceFlags.NOT_SAMPLED,
                    ),
            )
        logRecord.addAttribute(Key("key"), AnyValue.of("value"))

        val logRecordCloned = logRecord.clone()
        assertEquals(logRecord, logRecordCloned)

        val logRecordDifferent = logRecord.clone()
        logRecordDifferent.setEventName("different_event")
        assertNotEquals(logRecord, logRecordDifferent)
    }
}
