// port-lint: source logs/logger.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetry.Context
import io.github.kotlinmania.opentelemetry.InstrumentationScope
import io.github.kotlinmania.opentelemetry.logs.LogRecord
import io.github.kotlinmania.opentelemetry.logs.Logger
import io.github.kotlinmania.opentelemetry.logs.Severity
import kotlin.time.Clock

/**
 * The object for emitting [SdkLogRecord]s.
 */
public class SdkLogger internal constructor(
    public val scope: InstrumentationScope,
    private val provider: SdkLoggerProvider,
) : Logger {
    override fun createLogRecord(): SdkLogRecord = SdkLogRecord.new()

    override fun emit(record: LogRecord) {
        if (record is SdkLogRecord) {
            emit(record)
        }
    }

    public fun emit(record: SdkLogRecord) {
        if (Context.isCurrentTelemetrySuppressed()) {
            return
        }
        if (record.observedTimestamp == null) {
            record.setObservedTimestamp(Clock.System.now())
        }
        for (processor in provider.logProcessors()) {
            processor.emit(record, scope)
        }
    }

    override fun eventEnabled(level: Severity, target: String, name: String?): Boolean {
        if (Context.isCurrentTelemetrySuppressed()) {
            return false
        }
        return provider.logProcessors().any { processor ->
            processor.eventEnabled(level, target, name)
        }
    }
}
