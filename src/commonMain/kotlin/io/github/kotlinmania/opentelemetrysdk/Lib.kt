// port-lint: source src/lib.rs
package io.github.kotlinmania.opentelemetrysdk

// Implements the [`SDK`] component of OpenTelemetry.
//
// [`SDK`]: https://opentelemetry.io/docs/specs/otel/overview/#sdk
// [OpenTelemetry]: https://opentelemetry.io/docs/what-is-opentelemetry/
//
// # Getting Started
//
// ```
// import io.github.kotlinmania.opentelemetrysdk.trace.SdkTracerProvider
//
// fun example(newExporter: () -> SpanExporter) {
//     // Choose an exporter like `opentelemetry_stdout::SpanExporter`
//     val exporter = newExporter()
//
//     // Create a new trace pipeline that prints to stdout
//     val provider = SdkTracerProvider.builder()
//         .withSimpleExporter(exporter)
//         .build()
//     val tracer = provider.tracer("readme_example")
//
//     tracer.inSpan("doing_work") { cx ->
//         // Traced app logic here...
//     }
//
//     // Shutdown trace pipeline
//     check(provider.shutdown()) { "TracerProvider should shutdown successfully" }
// }
// ```
//
// See the examples directory in the upstream repository for different
// integration patterns.
//
// See the API `trace` module docs for more information on creating and managing
// spans.
//
// [examples]: https://github.com/open-telemetry/opentelemetry-rust/tree/main/examples
//
// # Metrics
//
// ### Creating instruments and recording measurements
//
// ```
// // get a meter from a provider
// val meter = global.meter("my_service")
//
// // create an instrument
// val counter = meter.u64Counter("my_counter").build()
//
// // record a measurement
// counter.add(1, listOf(KeyValue("http.client_ip", "83.164.160.102")))
// ```
//
// See the API `metrics` module docs for more information on creating and
// managing instruments.
//
// ## Crate Feature Flags
//
// The upstream Rust crate uses the following Cargo feature flags to control the
// telemetry signals to use:
//
// * `trace`: Includes the trace SDK (enabled by default).
// * `metrics`: Includes the metrics SDK.
// * `logs`: Includes the logs SDK.
//
// For `trace` the following feature flags are available:
//
// * `jaeger_remote_sampler`: Enables the Jaeger remote sampler.
//
// For `logs` the following feature flags are available:
//
// * `spec_unstable_logs_enabled`: control the log level
//
// Support for recording and exporting telemetry asynchronously and performing
// metrics aggregation can be added via the following flags:
//
// * `experimental_async_runtime`: Enables the experimental `Runtime` interface
//   and related functionality.
// * `rt-tokio`: Spawn telemetry tasks using tokio's multi-thread runtime.
// * `rt-tokio-current-thread`: Spawn telemetry tasks on a separate runtime so
//   that the main runtime won't be blocked.
//
// In this Kotlin port, the trace/metrics/logs modules are unconditionally
// available because Kotlin does not have a stable equivalent of Cargo features
// gated on the module graph; the runtime async-runtime hooks become explicit
// coroutine-dispatcher parameters.

// Module declarations (`pub mod ...`) carried by this ledger:
//
// pub(crate) mod growable_array;
// #[cfg(feature = "logs")]      pub mod logs;
// #[cfg(feature = "metrics")]   pub mod metrics;
// #[cfg(feature = "trace")]     pub mod propagation;
// pub mod resource;
// #[cfg(feature = "experimental_async_runtime")] pub mod runtime;
// #[cfg(any(feature = "testing", test))] pub mod testing;
// #[cfg(feature = "trace")]     pub mod trace;
// #[doc(hidden)]                pub mod util;
// pub mod error;

// Crate-root re-exports carried by this ledger (do not mint typealiases):
//
// pub use resource::Resource;
// pub use error::ExportError;
//
// `ExportError` already lives at `io.github.kotlinmania.opentelemetrysdk.ExportError`
// in `Error.kt`, so the upstream root re-export is satisfied by package
// placement. `Resource` is parceled out of the `resource` module; the
// resource module is not yet ported.
