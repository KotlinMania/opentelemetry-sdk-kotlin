# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 5/72 (6.9%)
- **Function parity:** 15/861 matched (target 23) — 1.7%
- **Class/type parity:** 6/208 matched (target 14) — 2.9%
- **Combined symbol parity:** 21/1069 matched (target 37) — 2.0%
- **Average inline-code cosine:** 0.38 (function body across 4 matched files)
- **Average documentation cosine:** 0.80 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 4 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `opentelemetrysdk.Error [PROVENANCE-FALLBACK]`
- **Similarity:** 1.00
- **Dependents:** 3
- **Priority Score:** 3000300.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/error.rs` vs expected `error.rs`
- **Proposed provenance header:** `// port-lint: source error.rs` (current: `// port-lint: source src/error.rs`)
- **Lint issues:** 1

### 2. growable_array

- **Target:** `opentelemetrysdk.GrowableArray [PROVENANCE-FALLBACK]`
- **Similarity:** 0.50
- **Dependents:** 1
- **Priority Score:** 1072405.0
- **Functions:** 15/19 matched (target 20)
- **Missing functions:** `default`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Item`, `IntoIter`, `KeyValuePair`
- **Tests:** 9/10 matched
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/growable_array.rs` vs expected `growable_array.rs`
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `tests:src/growable_array.rs` vs expected `growable_array.rs`
- **Proposed provenance header:** `// port-lint: source growable_array.rs` (current: `// port-lint: source src/growable_array.rs`)
- **Proposed provenance header:** `// port-lint: tests growable_array.rs` (current: `// port-lint: tests src/growable_array.rs`)
- **Lint issues:** 2

### 3. lib

- **Target:** `opentelemetrysdk.Lib [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched (target 2)
- **Missing functions:** `from`
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Lint issues:** 1

### 4. util

- **Target:** `opentelemetrysdk.Util [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10110.0
- **Functions:** 0/1 matched
- **Missing functions:** `tokio_interval_stream`
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/util.rs` vs expected `util.rs`
- **Proposed provenance header:** `// port-lint: source util.rs` (current: `// port-lint: source src/util.rs`)
- **Lint issues:** 1

### 5. resource.attributes

- **Target:** `resource.Attributes [ZERO] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/resource/attributes.rs` vs expected `resource/attributes.rs`
- **Proposed provenance header:** `// port-lint: source resource/attributes.rs` (current: `// port-lint: source src/resource/attributes.rs`)
- **Lint issues:** 1

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

## Reexport / Wiring Modules

These files match `reexport_modules` patterns in `.ast_distance_config.json`. They are filtered out of
normal priority and missing-file ladders because they are wiring
modules, not direct logic ports. Consult them for call-site routing;
do not treat them as the next implementation target by default.

### Missing

| Source | Expected target | Deps | Source path | Expected path |
|--------|-----------------|------|-------------|---------------|
| `logs.mod` | `logs.Mod` | 0 | `logs/mod.rs` | `logs/Mod.kt` |
| `data.mod` | `metrics.data.Mod` | 0 | `metrics/data/mod.rs` | `metrics/data/Mod.kt` |
| `internal.mod` | `metrics.internal.Mod` | 0 | `metrics/internal/mod.rs` | `metrics/internal/Mod.kt` |
| `metrics.mod` | `metrics.Mod` | 0 | `metrics/mod.rs` | `metrics/Mod.kt` |
| `propagation.mod` | `propagation.Mod` | 0 | `propagation/mod.rs` | `propagation/Mod.kt` |
| `resource.mod` | `resource.Mod` | 0 | `resource/mod.rs` | `resource/Mod.kt` |
| `testing.metrics.mod` | `testing.metrics.Mod` | 0 | `testing/metrics/mod.rs` | `testing/metrics/Mod.kt` |
| `testing.mod` | `testing.Mod` | 0 | `testing/mod.rs` | `testing/Mod.kt` |
| `testing.trace.mod` | `testing.trace.Mod` | 0 | `testing/trace/mod.rs` | `testing/trace/Mod.kt` |
| `id_generator.mod` | `trace.idgenerator.Mod` | 0 | `trace/id_generator/mod.rs` | `trace/idgenerator/Mod.kt` |
| `trace.mod` | `trace.Mod` | 0 | `trace/mod.rs` | `trace/Mod.kt` |
| `jaeger_remote.mod` | `trace.sampler.jaegerremote.Mod` | 0 | `trace/sampler/jaeger_remote/mod.rs` | `trace/sampler/jaegerremote/Mod.kt` |

