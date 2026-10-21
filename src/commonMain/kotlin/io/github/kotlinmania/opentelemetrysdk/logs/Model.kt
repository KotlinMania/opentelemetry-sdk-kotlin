package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetry.logs.Severity
import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.trace.SpanId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceFlags
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import kotlin.time.Instant

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
