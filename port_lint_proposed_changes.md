# port-lint Proposed Changes

**Generated:** 2026-08-26
**Source:** tmp/opentelemetry_sdk/src
**Target:** src/commonMain/kotlin/io/github/kotlinmania/opentelemetrysdk

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonTest/kotlin/io/github/kotlinmania/opentelemetrysdk/logs/ProcessorAndExporterTest.kt` | `// port-lint: source logs/mod.rs` | `// port-lint: source metrics/internal/mod.rs` | `metrics/internal/mod.rs` | `port-lint provenance header matched only by basename: 'logs/mod.rs' vs expected 'metrics/internal/mod.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/opentelemetrysdk/trace/SamplerTest.kt` | `// port-lint: source trace/sampler.rs` | `// port-lint: source trace/sampler/jaeger_remote/sampler.rs` | `trace/sampler/jaeger_remote/sampler.rs` | `port-lint provenance header matched only by basename: 'trace/sampler.rs' vs expected 'trace/sampler/jaeger_remote/sampler.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/opentelemetrysdk/trace/TracePipelineTest.kt` | `// port-lint: source trace/mod.rs` | `// port-lint: source trace/sampler/jaeger_remote/mod.rs` | `trace/sampler/jaeger_remote/mod.rs` | `port-lint provenance header matched only by basename: 'trace/mod.rs' vs expected 'trace/sampler/jaeger_remote/mod.rs'` |
