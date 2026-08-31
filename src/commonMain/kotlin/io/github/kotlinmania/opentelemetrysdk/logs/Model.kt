// port-lint: source opentelemetry_sdk/src/logs/mod.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.trace.SpanId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceFlags
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import kotlin.time.Instant

/**
 * Severity levels for a log record.
 */
public enum class Severity(
    public val severityNumber: Int,
    public val severityName: String,
) {
    Trace(1, "TRACE"),
    Trace2(2, "TRACE2"),
    Trace3(3, "TRACE3"),
    Trace4(4, "TRACE4"),
    Debug(5, "DEBUG"),
    Debug2(6, "DEBUG2"),
    Debug3(7, "DEBUG3"),
    Debug4(8, "DEBUG4"),
    Info(9, "INFO"),
    Info2(10, "INFO2"),
    Info3(11, "INFO3"),
    Info4(12, "INFO4"),
    Warn(13, "WARN"),
    Warn2(14, "WARN2"),
    Warn3(15, "WARN3"),
    Warn4(16, "WARN4"),
    Error(17, "ERROR"),
    Error2(18, "ERROR2"),
    Error3(19, "ERROR3"),
    Error4(20, "ERROR4"),
    Fatal(21, "FATAL"),
    Fatal2(22, "FATAL2"),
    Fatal3(23, "FATAL3"),
    Fatal4(24, "FATAL4"),
    ;

    public fun shortName(): String = severityName
}

/**
 * Value types for representing arbitrary values in a log record.
 */
public sealed interface AnyValue {
    /** An integer value. */
    public data class IntValue(
        public val value: Long,
    ) : AnyValue {
        override fun toString(): String = value.toString()
    }

    /** A double value. */
    public data class DoubleValue(
        public val value: Double,
    ) : AnyValue {
        override fun toString(): String = value.toString()
    }

    /** A string value. */
    public data class StringValue(
        public val value: String,
    ) : AnyValue {
        override fun toString(): String = value
    }

    /** A boolean value. */
    public data class BooleanValue(
        public val value: Boolean,
    ) : AnyValue {
        override fun toString(): String = value.toString()
    }

    /** A byte array value. */
    public class BytesValue(
        public val value: ByteArray,
    ) : AnyValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is BytesValue) return false
            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): String = "BytesValue(size=${value.size})"
    }

    /** An array of [AnyValue] values. */
    public data class ListAny(
        public val value: List<AnyValue>,
    ) : AnyValue

    /** A map of string keys to [AnyValue] values, arbitrarily nested. */
    public data class MapValue(
        public val value: Map<Key, AnyValue>,
    ) : AnyValue

    public companion object {
        public fun of(value: String): AnyValue = StringValue(value)

        public fun of(value: Long): AnyValue = IntValue(value)

        public fun of(value: Int): AnyValue = IntValue(value.toLong())

        public fun of(value: Double): AnyValue = DoubleValue(value)

        public fun of(value: Boolean): AnyValue = BooleanValue(value)

        public fun of(value: ByteArray): AnyValue = BytesValue(value)

        public fun of(value: List<AnyValue>): AnyValue = ListAny(value)

        public fun of(value: Map<Key, AnyValue>): AnyValue = MapValue(value)
    }
}

/**
 * An attribute key-value pair attached to a log record.
 */
public data class LogAttribute(
    public val key: Key,
    public val value: AnyValue,
) {
    public constructor(key: String, value: String) : this(Key(key), AnyValue.of(value))
    public constructor(key: String, value: Long) : this(Key(key), AnyValue.of(value))
    public constructor(key: String, value: Int) : this(Key(key), AnyValue.of(value.toLong()))
    public constructor(key: String, value: Double) : this(Key(key), AnyValue.of(value))
    public constructor(key: String, value: Boolean) : this(Key(key), AnyValue.of(value))
    public constructor(key: String, value: AnyValue) : this(Key(key), value)
}

/**
 * Interface for managing log records.
 */
public interface LogRecord {
    public fun setEventName(name: String)

    public fun setTarget(target: String)

    public fun setTimestamp(timestamp: Instant)

    public fun setObservedTimestamp(timestamp: Instant)

    public fun setSeverityText(text: String)

    public fun setSeverityNumber(number: Severity)

    public fun setBody(body: AnyValue)

    public fun addAttributes(attributes: List<LogAttribute>)

    public fun addAttribute(key: Key, value: AnyValue)

    public fun addAttribute(key: String, value: String)

    public fun addAttribute(key: String, value: AnyValue)

    public fun addAttribute(attribute: LogAttribute)

    public fun setTraceContext(traceId: TraceId, spanId: SpanId, traceFlags: TraceFlags? = null)
}

/**
 * The object for emitting log records.
 */
public interface Logger {
    public fun createLogRecord(): SdkLogRecord

    public fun emit(record: SdkLogRecord)
}

/**
 * Handles the creation of loggers.
 */
public interface LoggerProvider {
    public fun logger(name: String): Logger

    public fun loggerWithScope(scope: InstrumentationScope): Logger
}
