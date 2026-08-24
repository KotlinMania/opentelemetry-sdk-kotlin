# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 24/72 (33.3%)
- **Function parity:** 102/885 matched (target 205) — 11.5%
- **Class/type parity:** 36/222 matched (target 84) — 16.2%
- **Combined symbol parity:** 138/1107 matched (target 289) — 12.5%
- **Average inline-code cosine:** 0.25 (function body across 19 matched files)
- **Average documentation cosine:** 0.62 (doc text across 19 matched files)
- **Cheat-zeroed Files:** 6
- **Critical Issues:** 22 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. trace.span

- **Target:** `trace.Span`
- **Similarity:** 0.16
- **Dependents:** 3
- **Priority Score:** 3253508.5
- **Functions:** 9/33 matched (target 12)
- **Missing functions:** `new`, `with_data`, `span_context`, `drop`, `build_export_data`, `init`, `create_span`, `create_span_without_data`, `create_span_with_data_mut`, `add_event`, `record_error`, `set_attributes`, `end`, `allows_to_get_span_context_after_end`, `end_only_once`, `noop_after_end`, `is_recording_true_when_not_ended`, `is_recording_false_after_end`, `exceed_span_attributes_limit`, `exceed_event_attributes_limit`, `exceed_link_attributes_limit`, `exceed_span_links_limit`, `exceed_span_events_limit`, `test_span_exported_data`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `SpanData`
- **Tests:** 0/17 matched

### 2. resource.env

- **Target:** `resource.Env`
- **Similarity:** 0.56
- **Dependents:** 3
- **Priority Score:** 3010804.2
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `default`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 3. error

- **Target:** `opentelemetrysdk.Error`
- **Similarity:** 1.00
- **Dependents:** 3
- **Priority Score:** 3000300.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_

### 4. trace.span_processor

- **Target:** `trace.SpanProcessor`
- **Similarity:** 0.28
- **Dependents:** 1
- **Priority Score:** 1274707.1
- **Functions:** 14/39 matched (target 23)
- **Missing functions:** `new`, `get_spans_and_export`, `export_batch_sync`, `default`, `init_from_env_vars`, `simple_span_processor_on_end_calls_export`, `simple_span_processor_on_end_skips_export_if_not_sampled`, `simple_span_processor_shutdown_calls_shutdown`, `test_default_const_values`, `test_default_batch_config_adheres_to_specification`, `test_code_based_config_overrides_env_vars`, `test_batch_config_configurable_by_env_vars`, `test_batch_config_max_export_batch_size_validation`, `test_batch_config_with_fields`, `create_test_span`, `export`, `batchspanprocessor_handles_on_end`, `batchspanprocessor_force_flush`, `batchspanprocessor_shutdown`, `batchspanprocessor_handles_dropped_spans`, `validate_span_attributes_exported_correctly`, `batchspanprocessor_sets_and_exports_with_resource`, `test_batch_processor_current_thread_runtime`, `test_batch_processor_multi_thread_count_1_runtime`, `test_batch_processor_multi_thread`
- **Types:** 6/8 matched (target 6)
- **Missing types:** `BatchMessage`, `MockSpanExporter`
- **Tests:** 0/15 matched
- **Lint issues:** 5

### 5. trace.tracer

- **Target:** `trace.Tracer`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1131410.0
- **Functions:** 0/11 matched (target 12)
- **Missing functions:** `fmt`, `new`, `provider`, `instrumentation_scope`, `build_recording_span`, `id_generator`, `should_sample`, `build_with_context`, `allow_sampler_to_change_trace_state`, `drop_parent_based_children`, `uses_current_context_for_builders_if_unset`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Span`, `TestSampler`
- **Tests:** 0/3 matched

### 6. growable_array

- **Target:** `opentelemetrysdk.GrowableArray`
- **Similarity:** 0.50
- **Dependents:** 1
- **Priority Score:** 1072405.0
- **Functions:** 15/19 matched (target 20)
- **Missing functions:** `default`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Item`, `IntoIter`, `KeyValuePair`
- **Tests:** 9/10 matched

### 7. trace.config

- **Target:** `trace.Config`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010210.0
- **Functions:** 0/1 matched (target 3)
- **Missing functions:** `default`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 8. trace.provider

- **Target:** `trace.Provider`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 254407.5
- **Functions:** 17/36 matched (target 21)
- **Missing functions:** `noop_tracer_provider`, `drop`, `default`, `new`, `with_max_events_per_span`, `with_max_attributes_per_span`, `with_max_links_per_span`, `with_max_attributes_per_event`, `with_max_attributes_per_link`, `started_span_count`, `assert_info`, `on_start`, `on_end`, `test_force_flush`, `test_tracer_provider_default_resource`, `test_shutdown_noops`, `with_resource_multiple_calls_ensure_additive`, `drop_test_with_multiple_providers`, `drop_after_shutdown_test_with_multiple_providers`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `TracerProviderInner`, `Tracer`, `AssertInfo`, `SharedAssertInfo`, `TestSpanProcessor`, `CountingShutdownProcessor`
- **Tests:** 0/10 matched

### 9. trace.mod

- **Target:** `trace.TraceModel [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 212110.0
- **Functions:** 0/18 matched
- **Missing functions:** `span_modification_via_context`, `on_start`, `on_end`, `force_flush`, `shutdown_with_timeout`, `span_and_baggage`, `tracer_in_span`, `tracer_start`, `tracer_span_builder`, `exceed_span_links_limit`, `exceed_span_events_limit`, `trace_state_for_dropped_sampler`, `should_sample`, `trace_state_for_record_only_sampler`, `tracer_attributes`, `empty_tracer_name_retained`, `tracer_name_retained_helper`, `trace_suppression`
- **Types:** 0/3 matched (target 11)
- **Missing types:** `ValueA`, `BaggageInspectingSpanProcessor`, `TestRecordOnlySampler`
- **Tests:** 0/11 matched

### 10. jaeger_remote.sampler

- **Target:** `trace.SamplerTest [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 131310.0
- **Functions:** 0/11 matched (target 3)
- **Missing functions:** `new`, `with_update_interval`, `with_endpoint`, `with_leaky_bucket_size`, `build`, `get_endpoint`, `run_update_task`, `request_new_strategy`, `should_sample`, `fmt`, `deserialize_sampling_strategy_response`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `JaegerRemoteSamplerBuilder`, `JaegerRemoteSampler`
- **Tests:** 0/2 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `trace/sampler.rs` vs expected `trace/sampler/jaeger_remote/sampler.rs`
- **Proposed provenance header:** `// port-lint: source trace/sampler/jaeger_remote/sampler.rs` (current: `// port-lint: source trace/sampler.rs`)
- **Lint issues:** 1

### 11. logs.mod

- **Target:** `trace.TracePipelineTest [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 121210.0
- **Functions:** 0/10 matched (target 5)
- **Missing functions:** `logging_sdk_test`, `logger_attributes`, `emit`, `force_flush`, `shutdown`, `log_and_baggage`, `log_suppression`, `new`, `set_logger`, `processor_internal_log_does_not_deadlock_with_suppression_enabled`
- **Types:** 0/2 matched (target 1)
- **Missing types:** `EnrichWithBaggageProcessor`, `ReentrantLogProcessor`
- **Tests:** 0/5 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `trace/mod.rs` vs expected `logs/mod.rs`
- **Proposed provenance header:** `// port-lint: source logs/mod.rs` (current: `// port-lint: source trace/mod.rs`)
- **Lint issues:** 1

### 12. trace.sampler

- **Target:** `trace.Sampler`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 81208.2
- **Functions:** 2/9 matched (target 5)
- **Missing functions:** `box_clone`, `clone`, `jaeger_remote`, `sampler_data`, `sampling`, `clone_a_parent_sampler`, `parent_sampler`
- **Types:** 2/3 matched (target 8)
- **Missing types:** `CloneShouldSample`
- **Tests:** 0/3 matched
- **Lint issues:** 4

### 13. resource.mod

- **Target:** `resource.Resource [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 73510.0
- **Functions:** 25/28 matched (target 39)
- **Missing functions:** `schema_url`, `next`, `into_iter`
- **Types:** 3/7 matched (target 12)
- **Missing types:** `ResourceInner`, `Iter`, `Item`, `IntoIter`
- **Tests:** 7/7 matched

### 14. trace.events

- **Target:** `trace.Events`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60710.0
- **Functions:** 0/3 matched
- **Missing functions:** `deref`, `into_iter`, `add_event`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 15. trace.links

- **Target:** `trace.Links`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60710.0
- **Functions:** 0/3 matched (target 4)
- **Missing functions:** `deref`, `into_iter`, `add_link`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 16. trace.in_memory_exporter

- **Target:** `trace.InMemoryExporter`
- **Similarity:** 0.28
- **Dependents:** 0
- **Priority Score:** 31207.2
- **Functions:** 7/10 matched
- **Missing functions:** `default`, `new`, `keep_records_on_shutdown`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Lint issues:** 1

### 17. trace.error

- **Target:** `trace.Error`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 10407.9
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 6)
- **Missing types:** `Custom`

### 18. trace.span_limit

- **Target:** `trace.SpanLimits`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched
- **Missing functions:** `default`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 19. lib

- **Target:** `opentelemetrysdk.Lib [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched (target 9)
- **Missing functions:** `from`
- **Types:** 1/1 matched (target 8)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
- **Proposed provenance header:** `// port-lint: source lib.rs` (current: `// port-lint: source src/lib.rs`)
- **Lint issues:** 1

### 20. util

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

### 21. trace.export

- **Target:** `trace.Export`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 603.5
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 22. id_generator.mod

- **Target:** `trace.IdGenerator [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 23. resource.telemetry

- **Target:** `resource.Telemetry`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 204.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 24. resource.attributes

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
| `data.mod` | `metrics.data.Mod` | 0 | `metrics/data/mod.rs` | `metrics/data/Mod.kt` |
| `internal.mod` | `metrics.internal.Mod` | 0 | `metrics/internal/mod.rs` | `metrics/internal/Mod.kt` |
| `metrics.mod` | `metrics.Mod` | 0 | `metrics/mod.rs` | `metrics/Mod.kt` |
| `propagation.mod` | `propagation.Mod` | 0 | `propagation/mod.rs` | `propagation/Mod.kt` |
| `testing.metrics.mod` | `testing.metrics.Mod` | 0 | `testing/metrics/mod.rs` | `testing/metrics/Mod.kt` |
| `testing.mod` | `testing.Mod` | 0 | `testing/mod.rs` | `testing/Mod.kt` |
| `testing.trace.mod` | `testing.trace.Mod` | 0 | `testing/trace/mod.rs` | `testing/trace/Mod.kt` |
| `jaeger_remote.mod` | `trace.sampler.jaegerremote.Mod` | 0 | `trace/sampler/jaeger_remote/mod.rs` | `trace/sampler/jaegerremote/Mod.kt` |

