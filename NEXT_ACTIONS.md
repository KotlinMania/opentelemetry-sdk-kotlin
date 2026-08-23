# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/86 (4.7%)
- **Function parity:** 15/956 matched (target 22) — 1.6%
- **Class/type parity:** 6/227 matched (target 13) — 2.6%
- **Combined symbol parity:** 21/1183 matched (target 35) — 1.8%
- **Average inline-code cosine:** 0.50 (function body across 3 matched files)
- **Average documentation cosine:** 0.94 (doc text across 3 matched files)
- **Cheat-zeroed Files:** 2
- **Critical Issues:** 3 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. error

- **Target:** `opentelemetrysdk.Error`
- **Similarity:** 1.00
- **Dependents:** 3
- **Priority Score:** 3000300.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_

### 2. growable_array

- **Target:** `opentelemetrysdk.GrowableArray`
- **Similarity:** 0.50
- **Dependents:** 1
- **Priority Score:** 1072405.0
- **Functions:** 15/19 matched (target 20)
- **Missing functions:** `default`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Item`, `IntoIter`, `KeyValuePair`
- **Tests:** 9/10 matched

### 3. lib

- **Target:** `opentelemetrysdk.Lib [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched (target 2)
- **Missing functions:** `from`
- **Types:** 1/1 matched (target 4)
- **Missing types:** _none_

### 4. resource.attributes

- **Target:** `resource.Attributes [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

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
| `logs.mod` | `logs.Mod` | 0 | `src/logs/mod.rs` | `logs/Mod.kt` |
| `data.mod` | `metrics.data.Mod` | 0 | `src/metrics/data/mod.rs` | `metrics/data/Mod.kt` |
| `internal.mod` | `metrics.internal.Mod` | 0 | `src/metrics/internal/mod.rs` | `metrics/internal/Mod.kt` |
| `metrics.mod` | `metrics.Mod` | 0 | `src/metrics/mod.rs` | `metrics/Mod.kt` |
| `propagation.mod` | `propagation.Mod` | 0 | `src/propagation/mod.rs` | `propagation/Mod.kt` |
| `resource.mod` | `resource.Mod` | 0 | `src/resource/mod.rs` | `resource/Mod.kt` |
| `testing.metrics.mod` | `testing.metrics.Mod` | 0 | `src/testing/metrics/mod.rs` | `testing/metrics/Mod.kt` |
| `testing.mod` | `testing.Mod` | 0 | `src/testing/mod.rs` | `testing/Mod.kt` |
| `testing.trace.mod` | `testing.trace.Mod` | 0 | `src/testing/trace/mod.rs` | `testing/trace/Mod.kt` |
| `id_generator.mod` | `trace.idgenerator.Mod` | 0 | `src/trace/id_generator/mod.rs` | `trace/idgenerator/Mod.kt` |
| `trace.mod` | `trace.Mod` | 0 | `src/trace/mod.rs` | `trace/Mod.kt` |
| `jaeger_remote.mod` | `trace.sampler.jaegerremote.Mod` | 0 | `src/trace/sampler/jaeger_remote/mod.rs` | `trace/sampler/jaegerremote/Mod.kt` |

