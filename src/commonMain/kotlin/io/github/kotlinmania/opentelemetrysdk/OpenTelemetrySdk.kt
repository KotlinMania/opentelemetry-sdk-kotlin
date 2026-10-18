package io.github.kotlinmania.opentelemetrysdk

/**
 * OpenTelemetry SDK module ledger and metadata.
 */
public object OpenTelemetrySdk {
    public const val MODULE_NAME: String = "opentelemetry_sdk"
    public const val SPECIFICATION_VERSION: String = "1.30.0"

    /**
     * Supported signals in the OpenTelemetry SDK.
     */
    public enum class Signal {
        TRACE,
        METRICS,
        LOGS,
    }
}
