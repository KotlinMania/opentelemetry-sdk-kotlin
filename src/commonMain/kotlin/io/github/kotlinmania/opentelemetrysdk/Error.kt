// port-lint: source src/error.rs
package io.github.kotlinmania.opentelemetrysdk

import kotlin.time.Duration

// Wrapper for error from trace, logs and metrics part of open telemetry.

/** Trait for errors returned by exporters */
interface ExportError {
    /** The name of exporter that returned this error */
    val exporterName: String
}

/** Errors that can occur during SDK operations export(), forceFlush() and shutdown(). */
sealed class OTelSdkError(message: String) : Throwable(message) {
    /**
     * Shutdown has already been invoked.
     *
     * While shutdown is idempotent and calling it multiple times has no
     * impact, this error suggests that another part of the application is
     * invoking `shutdown` earlier than intended. Users should review their
     * code to identify unintended or duplicate shutdown calls and ensure it is
     * only triggered once at the correct place.
     */
    data object AlreadyShutdown : OTelSdkError("Shutdown already invoked")

    /**
     * Operation timed out before completing.
     *
     * This does not necessarily indicate a failure—operation may still be
     * complete. If this occurs frequently, consider increasing the timeout
     * duration to allow more time for completion.
     */
    data class Timeout(val duration: Duration) : OTelSdkError("Operation timed out after $duration")

    /**
     * Operation failed due to an internal error.
     *
     * The error message is intended for logging purposes only and should not
     * be used to make programmatic decisions. It is implementation-specific
     * and subject to change without notice. Consumers of this error should not
     * rely on its content beyond logging.
     */
    data class InternalFailure(val reason: String) : OTelSdkError("Operation failed: $reason")
}

/** A specialized `Result` type for Shutdown operations. */
typealias OTelSdkResult = Result<Unit>
