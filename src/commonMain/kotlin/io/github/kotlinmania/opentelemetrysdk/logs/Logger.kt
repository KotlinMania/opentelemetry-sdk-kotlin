// port-lint: source logs/logger.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.Context
import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import kotlin.time.Clock

/**
 * The object for emitting [SdkLogRecord]s.
 */
public class SdkLogger internal constructor(
    public val scope: InstrumentationScope,
    private val provider: SdkLoggerProvider,
) : Logger {
    override fun createLogRecord(): SdkLogRecord = SdkLogRecord.new()

    override fun emit(record: SdkLogRecord) {
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

    public fun eventEnabled(level: Severity, target: String, name: String? = null): Boolean {
        if (Context.isCurrentTelemetrySuppressed()) {
            return false
        }
        return provider.logProcessors().any { processor ->
            processor.eventEnabled(level, target, name)
        }
    }
}
