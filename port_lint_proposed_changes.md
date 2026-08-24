# port-lint Proposed Changes

**Generated:** 2026-08-23
**Source:** tmp/opentelemetry_sdk/src
**Target:** src/commonMain/kotlin

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `src/commonMain/kotlin/io/github/kotlinmania/opentelemetrysdk/Error.kt` | `// port-lint: source src/error.rs` | `// port-lint: source error.rs` | `error.rs` | `port-lint provenance header matched only after fallback normalization: 'src/error.rs' vs expected 'error.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/opentelemetrysdk/GrowableArray.kt` | `// port-lint: source src/growable_array.rs` | `// port-lint: source growable_array.rs` | `growable_array.rs` | `port-lint provenance header matched only after fallback normalization: 'src/growable_array.rs' vs expected 'growable_array.rs'` |
| `src/commonTest/kotlin/io/github/kotlinmania/opentelemetrysdk/GrowableArrayTest.kt` | `// port-lint: tests src/growable_array.rs` | `// port-lint: tests growable_array.rs` | `growable_array.rs` | `port-lint provenance header matched only after fallback normalization: 'tests:src/growable_array.rs' vs expected 'growable_array.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/opentelemetrysdk/InMemoryExporterError.kt` | `// port-lint: source src/lib.rs` | `// port-lint: source lib.rs` | `lib.rs` | `port-lint provenance header matched only after fallback normalization: 'src/lib.rs' vs expected 'lib.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/opentelemetrysdk/Util.kt` | `// port-lint: source src/util.rs` | `// port-lint: source util.rs` | `util.rs` | `port-lint provenance header matched only after fallback normalization: 'src/util.rs' vs expected 'util.rs'` |
| `src/commonMain/kotlin/io/github/kotlinmania/opentelemetrysdk/resource/Attributes.kt` | `// port-lint: source src/resource/attributes.rs` | `// port-lint: source resource/attributes.rs` | `resource/attributes.rs` | `port-lint provenance header matched only after fallback normalization: 'src/resource/attributes.rs' vs expected 'resource/attributes.rs'` |
