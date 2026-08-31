// port-lint: source opentelemetry_sdk/src/logs/record.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.trace.SpanContext
import io.github.kotlinmania.opentelemetrysdk.trace.SpanId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceFlags
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import kotlin.time.Instant

/**
 * Trace context for logs that have an associated span.
 */
public data class TraceContext(
    public val traceId: TraceId,
    public val spanId: SpanId,
    public val traceFlags: TraceFlags? = null,
) {
    public companion object {
        public fun from(spanContext: SpanContext): TraceContext =
            TraceContext(
                traceId = spanContext.traceId,
                spanId = spanContext.spanId,
                traceFlags = spanContext.traceFlags,
            )
    }
}

/**
 * Represents all data carried by a log record.
 */
public class SdkLogRecord(
    eventName: String? = null,
    target: String? = null,
    timestamp: Instant? = null,
    observedTimestamp: Instant? = null,
    traceContext: TraceContext? = null,
    severityText: String? = null,
    severityNumber: Severity? = null,
    body: AnyValue? = null,
    attributes: List<LogAttribute> = emptyList(),
) : LogRecord {
    private var _eventName: String? = eventName
    private var _target: String? = target
    private var _timestamp: Instant? = timestamp
    private var _observedTimestamp: Instant? = observedTimestamp
    private var _traceContext: TraceContext? = traceContext
    private var _severityText: String? = severityText
    private var _severityNumber: Severity? = severityNumber
    private var _body: AnyValue? = body
    private val _attributes: MutableList<LogAttribute> = attributes.toMutableList()

    public val eventName: String? get() = _eventName
    public val target: String? get() = _target
    public val timestamp: Instant? get() = _timestamp
    public val observedTimestamp: Instant? get() = _observedTimestamp
    public val traceContext: TraceContext? get() = _traceContext
    public val severityText: String? get() = _severityText
    public val severityNumber: Severity? get() = _severityNumber
    public val body: AnyValue? get() = _body
    public val attributes: List<LogAttribute> get() = _attributes

    override fun setEventName(name: String) {
        this._eventName = name
    }

    override fun setTarget(target: String) {
        this._target = target
    }

    override fun setTimestamp(timestamp: Instant) {
        this._timestamp = timestamp
    }

    override fun setObservedTimestamp(timestamp: Instant) {
        this._observedTimestamp = timestamp
    }

    override fun setSeverityText(text: String) {
        this._severityText = text
    }

    override fun setSeverityNumber(number: Severity) {
        this._severityNumber = number
    }

    override fun setBody(body: AnyValue) {
        this._body = body
    }

    override fun addAttributes(attributes: List<LogAttribute>) {
        _attributes.addAll(attributes)
    }

    override fun addAttribute(key: Key, value: AnyValue) {
        _attributes.add(LogAttribute(key, value))
    }

    override fun addAttribute(key: String, value: String) {
        _attributes.add(LogAttribute(Key(key), AnyValue.of(value)))
    }

    override fun addAttribute(key: String, value: AnyValue) {
        _attributes.add(LogAttribute(Key(key), value))
    }

    override fun addAttribute(attribute: LogAttribute) {
        _attributes.add(attribute)
    }

    override fun setTraceContext(traceId: TraceId, spanId: SpanId, traceFlags: TraceFlags?) {
        this._traceContext = TraceContext(traceId, spanId, traceFlags)
    }

    public fun attributesIter(): Iterator<LogAttribute> = _attributes.iterator()

    public fun attributesLen(): Int = _attributes.size

    public fun attributesContains(key: Key, value: AnyValue): Boolean =
        _attributes.any { it.key == key && it.value == value }

    public fun attributesContains(key: String, value: String): Boolean =
        attributesContains(Key(key), AnyValue.of(value))

    public fun attributesContains(key: String, value: AnyValue): Boolean =
        attributesContains(Key(key), value)

    public fun clone(): SdkLogRecord =
        SdkLogRecord(
            eventName = _eventName,
            target = _target,
            timestamp = _timestamp,
            observedTimestamp = _observedTimestamp,
            traceContext = _traceContext?.copy(),
            severityText = _severityText,
            severityNumber = _severityNumber,
            body = _body,
            attributes = _attributes.toList(),
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SdkLogRecord) return false
        return _eventName == other._eventName &&
            _target == other._target &&
            _timestamp == other._timestamp &&
            _observedTimestamp == other._observedTimestamp &&
            _traceContext == other._traceContext &&
            _severityText == other._severityText &&
            _severityNumber == other._severityNumber &&
            _body == other._body &&
            _attributes == other._attributes
    }

    override fun hashCode(): Int {
        var result = _eventName?.hashCode() ?: 0
        result = 31 * result + (_target?.hashCode() ?: 0)
        result = 31 * result + (_timestamp?.hashCode() ?: 0)
        result = 31 * result + (_observedTimestamp?.hashCode() ?: 0)
        result = 31 * result + (_traceContext?.hashCode() ?: 0)
        result = 31 * result + (_severityText?.hashCode() ?: 0)
        result = 31 * result + (_severityNumber?.hashCode() ?: 0)
        result = 31 * result + (_body?.hashCode() ?: 0)
        result = 31 * result + _attributes.hashCode()
        return result
    }

    override fun toString(): String =
        "SdkLogRecord(eventName=$_eventName, target=$_target, timestamp=$_timestamp, observedTimestamp=$_observedTimestamp, traceContext=$_traceContext, severityText=$_severityText, severityNumber=$_severityNumber, body=$_body, attributes=$_attributes)"

    public companion object {
        public fun new(): SdkLogRecord = SdkLogRecord()
    }
}
