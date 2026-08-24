// port-lint: source resource/telemetry.rs
package io.github.kotlinmania.opentelemetrysdk.resource

import io.github.kotlinmania.opentelemetrysdk.OpenTelemetrySdk

/**
 * Detects the telemetry SDK information used to capture data recorded by instrumentation libraries.
 *
 * It provides:
 * - The name of the telemetry SDK (`telemetry.sdk.name`), which will be `opentelemetry`.
 * - The language of the telemetry SDK (`telemetry.sdk.language`), which will be `kotlin`.
 * - The version of the telemetry SDK (`telemetry.sdk.version`), which will be the current SDK version.
 */
public class TelemetryResourceDetector : ResourceDetector {
    override fun detect(): Resource =
        Resource
            .builderEmpty()
            .withAttributes(
                listOf(
                    KeyValue(Key(TELEMETRY_SDK_NAME), Value.of("opentelemetry")),
                    KeyValue(Key(TELEMETRY_SDK_LANGUAGE), Value.of("kotlin")),
                    KeyValue(Key(TELEMETRY_SDK_VERSION), Value.of(OpenTelemetrySdk.SPECIFICATION_VERSION)),
                ),
            ).build()
}
