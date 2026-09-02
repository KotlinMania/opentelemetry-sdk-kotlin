# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 0/72 (0.0%)
- **Function parity:** 0/861 matched — 0.0%
- **Class/type parity:** 0/207 matched — 0.0%
- **Combined symbol parity:** 0/1068 matched — 0.0%
- **Average inline-code cosine:** 0.00 (function body across 0 matched files)
- **Average documentation cosine:** 0.00 (doc text across 0 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 0 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

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
| `lib` | `Lib` | 0 | `lib.rs` | `Lib.kt` |
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

