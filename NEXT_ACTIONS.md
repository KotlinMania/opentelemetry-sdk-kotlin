# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 36/72 (50.0%)
- **Function parity:** 194/857 matched (target 368) — 22.6%
- **Class/type parity:** 56/222 matched (target 130) — 25.2%
- **Combined symbol parity:** 250/1079 matched (target 498) — 23.2%
- **Average inline-code cosine:** 0.28 (function body across 29 matched files)
- **Average documentation cosine:** 0.55 (doc text across 29 matched files)
- **Cheat-zeroed Files:** 8
- **Critical Issues:** 33 files with <0.60 function similarity

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

### 2. logs.log_processor

- **Target:** `logs.LogProcessor`
- **Similarity:** 0.27
- **Dependents:** 3
- **Priority Score:** 3081307.2
- **Functions:** 4/9 matched (target 4)
- **Missing functions:** `export`, `get_resource`, `emit`, `force_flush`, `test_log_data_modification_by_multiple_processors`
- **Types:** 1/4 matched (target 1)
- **Missing types:** `MockLogExporter`, `FirstProcessor`, `SecondProcessor`
- **Tests:** 0/1 matched
- **Lint issues:** 1

### 3. resource.env

- **Target:** `resource.Env`
- **Similarity:** 0.56
- **Dependents:** 3
- **Priority Score:** 3010804.2
- **Functions:** 5/6 matched (target 7)
- **Missing functions:** `default`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_
- **Tests:** 2/2 matched

### 4. error

- **Target:** `opentelemetrysdk.Error`
- **Similarity:** 1.00
- **Dependents:** 3
- **Priority Score:** 3000300.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_

### 5. propagation.trace_context

- **Target:** `propagation.TraceContext`
- **Similarity:** 0.33
- **Dependents:** 2
- **Priority Score:** 2051506.8
- **Functions:** 9/14 matched (target 18)
- **Missing functions:** `trace_context_header_fields`, `new`, `extract_data`, `extract_data_invalid`, `inject_data`
- **Types:** 1/1 matched (target 6)
- **Missing types:** _none_
- **Tests:** 5/5 matched

### 6. trace.span_processor

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

### 7. logs.batch_log_processor

- **Target:** `logs.BatchLogProcessor`
- **Similarity:** 0.26
- **Dependents:** 1
- **Priority Score:** 1213707.4
- **Functions:** 12/31 matched (target 15)
- **Missing functions:** `fmt`, `get_logs_and_export`, `export_batch_sync`, `default`, `init_from_env_vars`, `test_default_const_values`, `test_default_batch_config_adheres_to_specification`, `test_code_based_config_overrides_env_vars`, `test_batch_config_configurable_by_env_vars`, `test_batch_config_max_export_batch_size_validation`, `test_batch_config_with_fields`, `test_build_batch_log_processor_builder`, `test_build_batch_log_processor_builder_with_custom_config`, `test_set_resource_batch_processor`, `test_batch_shutdown`, `test_batch_log_processor_shutdown_under_async_runtime_current_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_current_flavor_current_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_current_thread`
- **Types:** 4/6 matched (target 4)
- **Missing types:** `BatchMessage`, `LogsData`
- **Tests:** 0/8 matched

### 8. logs.simple_log_processor

- **Target:** `logs.SimpleLogProcessor`
- **Similarity:** 0.15
- **Dependents:** 1
- **Priority Score:** 1182508.5
- **Functions:** 6/22 matched (target 6)
- **Missing functions:** `shutdown`, `len`, `export`, `test_set_resource_simple_processor`, `test_simple_shutdown`, `test_simple_processor_sync_exporter_without_runtime`, `test_simple_processor_sync_exporter_with_runtime`, `test_simple_processor_sync_exporter_with_multi_thread_runtime`, `test_simple_processor_sync_exporter_with_current_thread_runtime`, `test_simple_processor_async_exporter_without_runtime`, `test_simple_processor_async_exporter_with_all_runtime_worker_threads_blocked`, `test_simple_processor_async_exporter_with_runtime`, `test_simple_processor_async_exporter_with_multi_thread_runtime`, `test_simple_processor_async_exporter_with_current_thread_runtime`, `set_logger`, `exporter_internal_log_does_not_deadlock_with_simple_processor`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `LogExporterThatRequiresTokio`, `ReentrantLogExporter`
- **Tests:** 0/5 matched

### 9. trace.tracer

- **Target:** `trace.Tracer`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1131410.0
- **Functions:** 0/11 matched (target 12)
- **Missing functions:** `fmt`, `new`, `provider`, `instrumentation_scope`, `build_recording_span`, `id_generator`, `should_sample`, `build_with_context`, `allow_sampler_to_change_trace_state`, `drop_parent_based_children`, `uses_current_context_for_builders_if_unset`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Span`, `TestSampler`
- **Tests:** 0/3 matched

### 10. growable_array

- **Target:** `opentelemetrysdk.GrowableArray`
- **Similarity:** 0.50
- **Dependents:** 1
- **Priority Score:** 1072405.0
- **Functions:** 15/19 matched (target 20)
- **Missing functions:** `default`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Item`, `IntoIter`, `KeyValuePair`
- **Tests:** 9/10 matched

### 11. trace.config

- **Target:** `trace.Config`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010210.0
- **Functions:** 0/1 matched (target 3)
- **Missing functions:** `default`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 12. trace.provider

- **Target:** `trace.Provider`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 254407.5
- **Functions:** 17/36 matched (target 21)
- **Missing functions:** `noop_tracer_provider`, `drop`, `default`, `new`, `with_max_events_per_span`, `with_max_attributes_per_span`, `with_max_links_per_span`, `with_max_attributes_per_event`, `with_max_attributes_per_link`, `started_span_count`, `assert_info`, `on_start`, `on_end`, `test_force_flush`, `test_tracer_provider_default_resource`, `test_shutdown_noops`, `with_resource_multiple_calls_ensure_additive`, `drop_test_with_multiple_providers`, `drop_after_shutdown_test_with_multiple_providers`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `TracerProviderInner`, `Tracer`, `AssertInfo`, `SharedAssertInfo`, `TestSpanProcessor`, `CountingShutdownProcessor`
- **Tests:** 0/10 matched

### 13. logs.logger_provider

- **Target:** `logs.LoggerProvider`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 233808.1
- **Functions:** 12/29 matched (target 13)
- **Missing functions:** `noop_logger_provider`, `drop`, `fmt`, `new`, `emit`, `resource`, `export`, `set_resource`, `test_resource_handling_provider_processor_exporter`, `trace_context_test`, `shutdown_test`, `shutdown_idempotent_test`, `global_shutdown_test`, `drop_test_with_multiple_providers`, `drop_after_shutdown_test_with_multiple_providers`, `test_empty_logger_name`, `with_resource_multiple_calls_ensure_additive`
- **Types:** 3/9 matched (target 3)
- **Missing types:** `Logger`, `ShutdownTestLogProcessor`, `TestExporterForResource`, `TestProcessorForResource`, `LazyLogProcessor`, `CountingShutdownProcessor`
- **Tests:** 0/15 matched

### 14. trace.mod

- **Target:** `trace.TraceModel [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 212110.0
- **Functions:** 0/18 matched
- **Missing functions:** `span_modification_via_context`, `on_start`, `on_end`, `force_flush`, `shutdown_with_timeout`, `span_and_baggage`, `tracer_in_span`, `tracer_start`, `tracer_span_builder`, `exceed_span_links_limit`, `exceed_span_events_limit`, `trace_state_for_dropped_sampler`, `should_sample`, `trace_state_for_record_only_sampler`, `tracer_attributes`, `empty_tracer_name_retained`, `tracer_name_retained_helper`, `trace_suppression`
- **Types:** 0/3 matched (target 11)
- **Missing types:** `ValueA`, `BaggageInspectingSpanProcessor`, `TestRecordOnlySampler`
- **Tests:** 0/11 matched

### 15. jaeger_remote.sampler

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

### 16. logs.mod

- **Target:** `logs.Model [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 121210.0
- **Functions:** 0/10 matched (target 16)
- **Missing functions:** `logging_sdk_test`, `logger_attributes`, `emit`, `force_flush`, `shutdown`, `log_and_baggage`, `log_suppression`, `new`, `set_logger`, `processor_internal_log_does_not_deadlock_with_suppression_enabled`
- **Types:** 0/2 matched (target 13)
- **Missing types:** `EnrichWithBaggageProcessor`, `ReentrantLogProcessor`
- **Tests:** 0/5 matched

### 17. logs.record

- **Target:** `logs.Record`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 103804.1
- **Functions:** 26/35 matched
- **Missing functions:** `event_name`, `target`, `timestamp`, `observed_timestamp`, `trace_context`, `severity_text`, `severity_number`, `body`, `compare_log_record_target_borrowed_eq_owned`
- **Types:** 2/3 matched
- **Missing types:** `LogRecordAttributes`
- **Tests:** 11/12 matched

### 18. trace.sampler

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

### 19. resource.mod

- **Target:** `resource.Resource [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 73510.0
- **Functions:** 25/28 matched (target 39)
- **Missing functions:** `schema_url`, `next`, `into_iter`
- **Types:** 3/7 matched (target 12)
- **Missing types:** `ResourceInner`, `Iter`, `Item`, `IntoIter`
- **Tests:** 7/7 matched

### 20. propagation.baggage

- **Target:** `propagation.Baggage`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 71407.7
- **Functions:** 6/13 matched
- **Missing functions:** `baggage_fields`, `new`, `valid_extract_data`, `valid_extract_data_with_metadata`, `valid_inject_data`, `valid_inject_data_metadata`, `inject_baggage_with_metadata`
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 3/8 matched

### 21. trace.links

- **Target:** `trace.Links`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60710.0
- **Functions:** 0/3 matched (target 4)
- **Missing functions:** `deref`, `into_iter`, `add_link`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 22. trace.events

- **Target:** `trace.Events`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60710.0
- **Functions:** 0/3 matched
- **Missing functions:** `deref`, `into_iter`, `add_event`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 23. logs.export

- **Target:** `logs.Export`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 51305.3
- **Functions:** 6/8 matched (target 7)
- **Missing functions:** `new_with_owned_data`, `next`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `LogBatchData`, `LogBatchDataIter`, `Item`
- **Lint issues:** 1

### 24. trace.in_memory_exporter

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

### 25. logs.in_memory_exporter

- **Target:** `logs.InMemoryExporter`
- **Similarity:** 0.41
- **Dependents:** 0
- **Priority Score:** 21405.9
- **Functions:** 8/10 matched (target 12)
- **Missing functions:** `default`, `keep_records_on_shutdown`
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Lint issues:** 1

### 26. logs.logger

- **Target:** `logs.Logger`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 20603.4
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `new`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `LogRecord`

### 27. trace.error

- **Target:** `trace.Error`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 10407.9
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 6)
- **Missing types:** `Custom`

### 28. lib

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

### 29. trace.span_limit

- **Target:** `trace.SpanLimits`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched
- **Missing functions:** `default`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 30. util

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

### 31. trace.export

- **Target:** `trace.Export`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 603.5
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 32. id_generator.mod

- **Target:** `trace.IdGenerator [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 33. resource.telemetry

- **Target:** `resource.Telemetry`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 204.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 34. resource.attributes

- **Target:** `resource.Attributes [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 35. jaeger_remote.mod

- **Target:** `trace.TracePipelineTest [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 5)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 1)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `trace/mod.rs` vs expected `trace/sampler/jaeger_remote/mod.rs`
- **Proposed provenance header:** `// port-lint: source trace/sampler/jaeger_remote/mod.rs` (current: `// port-lint: source trace/mod.rs`)
- **Lint issues:** 1

### 36. testing.mod

- **Target:** `logs.ProcessorAndExporterTest [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched (target 21)
- **Missing functions:** _none_
- **Types:** 0/0 matched (target 3)
- **Missing types:** _none_
- **Provenance warning:** port-lint provenance header matched only by basename: `logs/mod.rs` vs expected `testing/mod.rs`
- **Proposed provenance header:** `// port-lint: source testing/mod.rs` (current: `// port-lint: source logs/mod.rs`)
- **Lint issues:** 6

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
| `testing.trace.mod` | `testing.trace.Mod` | 0 | `testing/trace/mod.rs` | `testing/trace/Mod.kt` |

