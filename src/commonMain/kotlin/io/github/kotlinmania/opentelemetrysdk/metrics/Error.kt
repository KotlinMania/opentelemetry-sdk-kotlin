// port-lint: source metrics/error.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

/**
 * A specialized Result type for metric operations.
 */
public typealias MetricResult<T> = Result<T>

/**
 * Errors returned by the metrics API.
 */
public sealed class MetricError(
    message: String,
) : Exception(message) {
    /**
     * Other errors not covered by specific cases.
     */
    public class Other(
        message: String,
    ) : MetricError("Metrics error: $message")

    /**
     * Invalid configuration.
     */
    public class Config(
        message: String,
    ) : MetricError("Config error $message")

    /**
     * Invalid instrument configuration such as invalid instrument name, invalid instrument description, etc.
     */
    public class InvalidInstrumentConfiguration(
        public val reason: String,
    ) : MetricError("Invalid instrument configuration: $reason") {
        public fun description(): String = reason
    }

    public companion object {
        public fun from(err: Throwable): MetricError = Other(err.message ?: err.toString())

        public fun from(message: String): MetricError = Other(message)
    }
}
