// port-lint: source trace/error.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.ExportError
import kotlin.time.Duration

/** A specialized Result type for trace operations. */
public typealias TraceResult<T> = Result<T>

/** Errors returned by the trace API. */
public sealed class TraceError(
    message: String,
    cause: Throwable? = null,
) : Throwable(message, cause) {
    /** Export failed with the error returned by the exporter. */
    public data class ExportFailed(
        public val error: ExportError,
    ) : TraceError("Exporter ${error.exporterName} encountered error: $error")

    /** Export failed to finish after certain period and processor stopped the export. */
    public data class ExportTimedOut(
        public val duration: Duration,
    ) : TraceError("Exporting timed out after ${duration.inWholeSeconds} seconds")

    /** TracerProvider already shutdown. */
    public data object TracerProviderAlreadyShutdown : TraceError("TracerProvider already shutdown")

    /** Other errors propagated from trace SDK that weren't covered above. */
    public data class Other(
        public val reason: String,
        public val underlyingCause: Throwable? = null,
    ) : TraceError(reason, underlyingCause)

    public companion object {
        /** Creates a [TraceError] from an [ExportError]. */
        public fun from(error: ExportError): TraceError = ExportFailed(error)

        /** Creates a [TraceError] from a string message. */
        public fun from(message: String): TraceError = Other(message)
    }

    /** Wrap type for string errors. */
    public data class Custom(
        public val value: String,
    ) : TraceError(value)
}

