// port-lint: source lib.rs
package io.github.kotlinmania.opentelemetrysdk

/**
 * Errors that can occur during when returning telemetry from InMemoryLogExporter.
 *
 * Upstream this sits behind testing features. In the Kotlin port the
 * testing surface is not behind a Cargo feature gate, so the type is unconditionally
 * available in commonMain.
 *
 * The upstream poison error conversion implementation has no Kotlin Multiplatform
 * analog: KMP does not expose a poison-aware mutex.
 */
sealed class InMemoryExporterError(
    message: String,
) : Throwable(message) {
    /**
     * Operation failed due to an internal error.
     *
     * The error message is intended for logging purposes only and should not
     * be used to make programmatic decisions. It is implementation-specific
     * and subject to change without notice. Consumers of this error should not
     * rely on its content beyond logging.
     */
    data class InternalFailure(
        val reason: String,
    ) : InMemoryExporterError("Unable to obtain telemetry. Reason: $reason")
}
