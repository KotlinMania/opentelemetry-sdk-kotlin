// port-lint: source src/resource/attributes.rs
package io.github.kotlinmania.opentelemetrysdk.resource

/**
 * Logical name of the service.
 *
 * MUST be the same for all instances of horizontally scaled services. If the value was not
 * specified, SDKs MUST fallback to `unknown_service:` concatenated with
 * `process.executable.name`, e.g. `unknown_service:bash`. If `process.executable.name` is not
 * available, the value MUST be set to `unknown_service`.
 *
 * Examples:
 *
 * - `shoppingcart`
 */
internal const val SERVICE_NAME: String = "service.name"

/** The language of the telemetry SDK. */
internal const val TELEMETRY_SDK_LANGUAGE: String = "telemetry.sdk.language"

/**
 * The name of the telemetry SDK as defined above.
 *
 * The OpenTelemetry SDK MUST set the `telemetry.sdk.name` attribute to `opentelemetry`.
 * If another SDK, like a fork or a vendor-provided implementation, is used, this SDK MUST set
 * the `telemetry.sdk.name` attribute to the fully-qualified class or module name of this SDK's
 * main entry point or another suitable identifier depending on the language.
 * The identifier `opentelemetry` is reserved and MUST NOT be used in this case.
 * All custom identifiers SHOULD be stable across different versions of an implementation.
 *
 * Examples:
 *
 * - `opentelemetry`
 */
internal const val TELEMETRY_SDK_NAME: String = "telemetry.sdk.name"

/**
 * The version string of the telemetry SDK.
 *
 * Examples:
 *
 * - `1.2.3`
 */
internal const val TELEMETRY_SDK_VERSION: String = "telemetry.sdk.version"
