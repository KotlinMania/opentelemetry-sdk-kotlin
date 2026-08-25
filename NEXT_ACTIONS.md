# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 49/72 (68.1%)
- **Function parity:** 275/992 matched (target 480) — 27.7%
- **Class/type parity:** 93/246 matched (target 190) — 37.8%
- **Combined symbol parity:** 368/1238 matched (target 670) — 29.7%
- **Average inline-code cosine:** 0.32 (function body across 40 matched files)
- **Average documentation cosine:** 0.57 (doc text across 40 matched files)
- **Cheat-zeroed Files:** 11
- **Critical Issues:** 44 files with <0.60 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. logs.log_processor

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

### 2. trace.span

- **Target:** `trace.Span`
- **Similarity:** 0.64
- **Dependents:** 3
- **Priority Score:** 3053503.5
- **Functions:** 29/33 matched (target 40)
- **Missing functions:** `span_context`, `drop`, `build_export_data`, `init`
- **Types:** 1/2 matched
- **Missing types:** `SpanData`
- **Tests:** 17/17 matched

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

### 5. metrics.meter_provider

- **Target:** `metrics.MeterProvider`
- **Similarity:** 0.37
- **Dependents:** 2
- **Priority Score:** 2092406.4
- **Functions:** 13/21 matched (target 13)
- **Missing functions:** `drop`, `meter`, `meter_with_scope`, `fmt`, `test_shutdown_invoked_on_last_drop`, `same_meter_reused_same_scope`, `same_meter_reused_same_scope_attributes`, `different_meter_different_attributes`
- **Types:** 2/3 matched
- **Missing types:** `SdkMeterProviderInner`
- **Tests:** 3/7 matched

### 6. propagation.trace_context

- **Target:** `propagation.TraceContext`
- **Similarity:** 0.33
- **Dependents:** 2
- **Priority Score:** 2051506.8
- **Functions:** 9/14 matched (target 18)
- **Missing functions:** `trace_context_header_fields`, `new`, `extract_data`, `extract_data_invalid`, `inject_data`
- **Types:** 1/1 matched (target 6)
- **Missing types:** _none_
- **Tests:** 5/5 matched

### 7. metrics.periodic_reader

- **Target:** `metrics.PeriodicReader`
- **Similarity:** 0.11
- **Dependents:** 1
- **Priority Score:** 1405009.0
- **Functions:** 8/44 matched (target 9)
- **Missing functions:** `new`, `clone`, `collect_and_export`, `fmt`, `shutdown`, `default`, `get_count`, `export`, `collection_triggered_by_interval_multiple`, `shutdown_repeat`, `flush_after_shutdown`, `flush_repeat`, `periodic_reader_without_pipeline`, `exporter_failures_are_handled`, `shutdown_passed_to_exporter`, `collection`, `collection_from_tokio_multi_with_one_worker`, `collection_from_tokio_with_two_worker`, `collection_from_tokio_current`, `collection_triggered_by_interval_helper`, `collection_triggered_by_flush_helper`, `collection_triggered_by_shutdown_helper`, `collection_triggered_by_drop_helper`, `collection_helper`, `some_async_function`, `async_inside_observable_callback_from_tokio_multi_with_one_worker`, `async_inside_observable_callback_from_tokio_multi_with_two_worker`, `async_inside_observable_callback_from_tokio_current_thread`, `async_inside_observable_callback_from_regular_main`, `async_inside_observable_callback_helper`, `some_tokio_async_function`, `tokio_async_inside_observable_callback_from_tokio_multi_with_one_worker`, `tokio_async_inside_observable_callback_from_tokio_multi_with_two_worker`, `tokio_async_inside_observable_callback_from_tokio_current_thread`, `tokio_async_inside_observable_callback_from_regular_main`, `tokio_async_inside_observable_callback_helper`
- **Types:** 2/6 matched (target 3)
- **Missing types:** `PeriodicReaderInner`, `Message`, `MetricExporterThatFailsOnlyOnFirst`, `MockMetricExporter`
- **Tests:** 0/10 matched

### 8. trace.span_processor

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

### 9. logs.batch_log_processor

- **Target:** `logs.BatchLogProcessor`
- **Similarity:** 0.26
- **Dependents:** 1
- **Priority Score:** 1213707.4
- **Functions:** 12/31 matched (target 15)
- **Missing functions:** `fmt`, `get_logs_and_export`, `export_batch_sync`, `default`, `init_from_env_vars`, `test_default_const_values`, `test_default_batch_config_adheres_to_specification`, `test_code_based_config_overrides_env_vars`, `test_batch_config_configurable_by_env_vars`, `test_batch_config_max_export_batch_size_validation`, `test_batch_config_with_fields`, `test_build_batch_log_processor_builder`, `test_build_batch_log_processor_builder_with_custom_config`, `test_set_resource_batch_processor`, `test_batch_shutdown`, `test_batch_log_processor_shutdown_under_async_runtime_current_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_current_flavor_current_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_current_thread`
- **Types:** 4/6 matched (target 4)
- **Missing types:** `BatchMessage`, `LogsData`
- **Tests:** 0/8 matched

### 10. logs.simple_log_processor

- **Target:** `logs.SimpleLogProcessor`
- **Similarity:** 0.15
- **Dependents:** 1
- **Priority Score:** 1182508.5
- **Functions:** 6/22 matched (target 6)
- **Missing functions:** `shutdown`, `len`, `export`, `test_set_resource_simple_processor`, `test_simple_shutdown`, `test_simple_processor_sync_exporter_without_runtime`, `test_simple_processor_sync_exporter_with_runtime`, `test_simple_processor_sync_exporter_with_multi_thread_runtime`, `test_simple_processor_sync_exporter_with_current_thread_runtime`, `test_simple_processor_async_exporter_without_runtime`, `test_simple_processor_async_exporter_with_all_runtime_worker_threads_blocked`, `test_simple_processor_async_exporter_with_runtime`, `test_simple_processor_async_exporter_with_multi_thread_runtime`, `test_simple_processor_async_exporter_with_current_thread_runtime`, `set_logger`, `exporter_internal_log_does_not_deadlock_with_simple_processor`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `LogExporterThatRequiresTokio`, `ReentrantLogExporter`
- **Tests:** 0/5 matched

### 11. trace.tracer

- **Target:** `trace.Tracer`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1131410.0
- **Functions:** 0/11 matched (target 12)
- **Missing functions:** `fmt`, `new`, `provider`, `instrumentation_scope`, `build_recording_span`, `id_generator`, `should_sample`, `build_with_context`, `allow_sampler_to_change_trace_state`, `drop_parent_based_children`, `uses_current_context_for_builders_if_unset`
- **Types:** 1/3 matched (target 2)
- **Missing types:** `Span`, `TestSampler`
- **Tests:** 0/3 matched

### 12. growable_array

- **Target:** `opentelemetrysdk.GrowableArray`
- **Similarity:** 0.50
- **Dependents:** 1
- **Priority Score:** 1072405.0
- **Functions:** 15/19 matched (target 20)
- **Missing functions:** `default`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Item`, `IntoIter`, `KeyValuePair`
- **Tests:** 9/10 matched

### 13. metrics.aggregation

- **Target:** `metrics.Aggregation`
- **Similarity:** 0.47
- **Dependents:** 1
- **Priority Score:** 1010505.3
- **Functions:** 2/3 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 9)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 14. trace.config

- **Target:** `trace.Config`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1010210.0
- **Functions:** 0/1 matched (target 3)
- **Missing functions:** `default`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 15. metrics.mod

- **Target:** `metrics.Temporality [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 969710.0
- **Functions:** 0/95 matched (target 0)
- **Missing functions:** `invalid_instrument_config_noops`, `valid_instrument_config_with_feature_experimental_metrics_disable_name_validation`, `counter_aggregation_delta`, `counter_aggregation_cumulative`, `counter_aggregation_no_attributes_cumulative`, `counter_aggregation_no_attributes_delta`, `counter_aggregation_overflow_delta`, `counter_aggregation_overflow_cumulative`, `counter_aggregation_attribute_order_sorted_first_delta`, `counter_aggregation_attribute_order_sorted_first_cumulative`, `counter_aggregation_attribute_order_unsorted_first_delta`, `counter_aggregation_attribute_order_unsorted_first_cumulative`, `histogram_aggregation_cumulative`, `histogram_aggregation_delta`, `histogram_aggregation_with_custom_bounds`, `histogram_aggregation_with_empty_bounds`, `updown_counter_aggregation_cumulative`, `updown_counter_aggregation_delta`, `gauge_aggregation`, `observable_gauge_aggregation`, `observable_counter_aggregation_cumulative_non_zero_increment`, `observable_counter_aggregation_cumulative_non_zero_increment_no_attrs`, `observable_counter_aggregation_delta_non_zero_increment`, `observable_counter_aggregation_delta_non_zero_increment_no_attrs`, `observable_counter_aggregation_cumulative_zero_increment`, `observable_counter_aggregation_cumulative_zero_increment_no_attrs`, `observable_counter_aggregation_delta_zero_increment`, `observable_counter_aggregation_delta_zero_increment_no_attrs`, `observable_counter_aggregation_helper`, `empty_meter_name_retained`, `meter_name_retained_helper`, `counter_duplicate_instrument_merge`, `counter_duplicate_instrument_different_meter_no_merge`, `instrumentation_scope_identity_test`, `histogram_aggregation_with_invalid_aggregation_should_proceed_as_if_view_not_exist`, `spatial_aggregation_when_view_drops_attributes_observable_counter`, `spatial_aggregation_when_view_drops_attributes_counter`, `no_attr_cumulative_up_down_counter`, `no_attr_up_down_counter_always_cumulative`, `no_attr_cumulative_counter_value_added_after_export`, `no_attr_delta_counter_value_reset_after_export`, `second_delta_export_does_not_give_no_attr_value_if_add_not_called`, `delta_memory_efficiency_test`, `counter_multithreaded`, `counter_f64_multithreaded`, `histogram_multithreaded`, `histogram_f64_multithreaded`, `synchronous_instruments_cumulative_with_gap_in_measurements`, `synchronous_instruments_cumulative_with_gap_in_measurements_helper`, `assert_correct_export`, `asynchronous_instruments_cumulative_data_points_only_from_last_measurement`, `view_test_rename`, `view_test_change_unit`, `view_test_change_description`, `view_test_change_name_unit`, `view_test_change_name_unit_desc`, `view_test_match_unit`, `view_test_match_none`, `view_test_match_multiple`, `test_view_customization`, `test_view_single_instrument_multiple_stream`, `test_view_multiple_instrument_single_stream`, `asynchronous_instruments_cumulative_data_points_only_from_last_measurement_helper`, `counter_multithreaded_aggregation_helper`, `counter_f64_multithreaded_aggregation_helper`, `histogram_multithreaded_aggregation_helper`, `histogram_f64_multithreaded_aggregation_helper`, `histogram_aggregation_helper`, `histogram_aggregation_with_custom_bounds_helper`, `histogram_aggregation_with_empty_bounds_helper`, `gauge_aggregation_helper`, `observable_gauge_aggregation_helper`, `counter_aggregation_helper`, `counter_aggregation_overflow_helper`, `counter_aggregation_overflow_helper_custom_limit`, `counter_aggregation_attribute_order_helper`, `updown_counter_aggregation_helper`, `find_sum_datapoint_with_key_value`, `find_overflow_sum_datapoint`, `find_gauge_datapoint_with_key_value`, `find_sum_datapoint_with_no_attributes`, `find_gauge_datapoint_with_no_attributes`, `find_histogram_datapoint_with_key_value`, `find_histogram_datapoint_with_no_attributes`, `find_scope_metric`, `new`, `new_with_view`, `u64_counter`, `i64_up_down_counter`, `meter`, `flush_metrics`, `reset_metrics`, `check_no_metrics`, `get_aggregation`, `get_from_multiple_aggregations`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `TestContext`
- **Tests:** 0/10 matched

### 16. internal.mod

- **Target:** `logs.ProcessorAndExporterTest [STUB] [PROVENANCE-FALLBACK]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 363610.0
- **Functions:** 0/29 matched (target 21)
- **Missing functions:** `stream_overflow_attributes`, `new`, `trackers_for_collect`, `is_under_cardinality_limit`, `measure`, `collect_readonly`, `collect_and_reset`, `prepare_data`, `sort_and_dedup`, `min`, `max`, `into_float`, `make_aggregated_metrics`, `extract_metrics_data_ref`, `extract_metrics_data_mut`, `store`, `add`, `get_value`, `get_and_reset_value`, `new_atomic_tracker`, `can_store_u64_atomic_value`, `can_add_and_get_u64_atomic_value`, `can_reset_u64_atomic_value`, `can_store_i64_atomic_value`, `can_add_and_get_i64_atomic_value`, `can_reset_i64_atomic_value`, `can_store_f64_atomic_value`, `can_add_and_get_f64_atomic_value`, `can_reset_f64_atomic_value`
- **Types:** 0/7 matched (target 3)
- **Missing types:** `Aggregator`, `ValueMap`, `AtomicTracker`, `AtomicallyUpdate`, `AggregatedMetricsAccess`, `Number`, `F64AtomicTracker`
- **Tests:** 0/9 matched
- **Provenance warning:** port-lint provenance header matched only by basename: `logs/mod.rs` vs expected `metrics/internal/mod.rs`
- **Proposed provenance header:** `// port-lint: source metrics/internal/mod.rs` (current: `// port-lint: source logs/mod.rs`)
- **Lint issues:** 6

### 17. data.mod

- **Target:** `data.Data [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 335010.0
- **Functions:** 2/35 matched (target 4)
- **Missing functions:** `default`, `resource`, `scope_metrics`, `scope`, `metrics`, `name`, `unit`, `data`, `from`, `attributes`, `exemplars`, `value`, `data_points`, `start_time`, `time`, `temporality`, `is_monotonic`, `bounds`, `bucket_counts`, `count`, `min`, `max`, `sum`, `scale`, `zero_count`, `positive_bucket`, `negative_bucket`, `zero_threshold`, `offset`, `counts`, `filtered_attributes`, `span_id`, `trace_id`
- **Types:** 15/15 matched (target 23)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 18. trace.provider

- **Target:** `trace.Provider`
- **Similarity:** 0.25
- **Dependents:** 0
- **Priority Score:** 254407.5
- **Functions:** 17/36 matched (target 21)
- **Missing functions:** `noop_tracer_provider`, `drop`, `default`, `new`, `with_max_events_per_span`, `with_max_attributes_per_span`, `with_max_links_per_span`, `with_max_attributes_per_event`, `with_max_attributes_per_link`, `started_span_count`, `assert_info`, `on_start`, `on_end`, `test_force_flush`, `test_tracer_provider_default_resource`, `test_shutdown_noops`, `with_resource_multiple_calls_ensure_additive`, `drop_test_with_multiple_providers`, `drop_after_shutdown_test_with_multiple_providers`
- **Types:** 2/8 matched (target 2)
- **Missing types:** `TracerProviderInner`, `Tracer`, `AssertInfo`, `SharedAssertInfo`, `TestSpanProcessor`, `CountingShutdownProcessor`
- **Tests:** 0/10 matched

### 19. logs.logger_provider

- **Target:** `logs.LoggerProvider`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 233808.1
- **Functions:** 12/29 matched (target 13)
- **Missing functions:** `noop_logger_provider`, `drop`, `fmt`, `new`, `emit`, `resource`, `export`, `set_resource`, `test_resource_handling_provider_processor_exporter`, `trace_context_test`, `shutdown_test`, `shutdown_idempotent_test`, `global_shutdown_test`, `drop_test_with_multiple_providers`, `drop_after_shutdown_test_with_multiple_providers`, `test_empty_logger_name`, `with_resource_multiple_calls_ensure_additive`
- **Types:** 3/9 matched (target 3)
- **Missing types:** `Logger`, `ShutdownTestLogProcessor`, `TestExporterForResource`, `TestProcessorForResource`, `LazyLogProcessor`, `CountingShutdownProcessor`
- **Tests:** 0/15 matched

### 20. trace.mod

- **Target:** `trace.TraceModel [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 212110.0
- **Functions:** 0/18 matched
- **Missing functions:** `span_modification_via_context`, `on_start`, `on_end`, `force_flush`, `shutdown_with_timeout`, `span_and_baggage`, `tracer_in_span`, `tracer_start`, `tracer_span_builder`, `exceed_span_links_limit`, `exceed_span_events_limit`, `trace_state_for_dropped_sampler`, `should_sample`, `trace_state_for_record_only_sampler`, `tracer_attributes`, `empty_tracer_name_retained`, `tracer_name_retained_helper`, `trace_suppression`
- **Types:** 0/3 matched (target 11)
- **Missing types:** `ValueA`, `BaggageInspectingSpanProcessor`, `TestRecordOnlySampler`
- **Tests:** 0/11 matched

### 21. jaeger_remote.sampler

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

### 22. metrics.instrument

- **Target:** `metrics.Instrument`
- **Similarity:** 0.44
- **Dependents:** 0
- **Priority Score:** 123005.6
- **Functions:** 14/23 matched (target 16)
- **Missing functions:** `name`, `kind`, `unit`, `scope`, `new`, `validate_bucket_boundaries`, `normalize`, `measure`, `observe`
- **Types:** 4/7 matched (target 5)
- **Missing types:** `InstrumentId`, `ResolvedMeasures`, `Observable`
- **Tests:** 5/5 matched

### 23. logs.mod

- **Target:** `logs.Model [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 121210.0
- **Functions:** 0/10 matched (target 16)
- **Missing functions:** `logging_sdk_test`, `logger_attributes`, `emit`, `force_flush`, `shutdown`, `log_and_baggage`, `log_suppression`, `new`, `set_logger`, `processor_internal_log_does_not_deadlock_with_suppression_enabled`
- **Types:** 0/2 matched (target 13)
- **Missing types:** `EnrichWithBaggageProcessor`, `ReentrantLogProcessor`
- **Tests:** 0/5 matched

### 24. logs.record

- **Target:** `logs.Record`
- **Similarity:** 0.59
- **Dependents:** 0
- **Priority Score:** 103804.1
- **Functions:** 26/35 matched
- **Missing functions:** `event_name`, `target`, `timestamp`, `observed_timestamp`, `trace_context`, `severity_text`, `severity_number`, `body`, `compare_log_record_target_borrowed_eq_owned`
- **Types:** 2/3 matched
- **Missing types:** `LogRecordAttributes`
- **Tests:** 11/12 matched

### 25. trace.sampler

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

### 26. resource.mod

- **Target:** `resource.Resource [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 73510.0
- **Functions:** 25/28 matched (target 40)
- **Missing functions:** `schema_url`, `next`, `into_iter`
- **Types:** 3/7 matched (target 12)
- **Missing types:** `ResourceInner`, `Iter`, `Item`, `IntoIter`
- **Tests:** 7/7 matched

### 27. propagation.baggage

- **Target:** `propagation.Baggage`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 71407.7
- **Functions:** 6/13 matched
- **Missing functions:** `baggage_fields`, `new`, `valid_extract_data`, `valid_extract_data_with_metadata`, `valid_inject_data`, `valid_inject_data_metadata`, `inject_baggage_with_metadata`
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 3/8 matched

### 28. metrics.in_memory_exporter

- **Target:** `metrics.InMemoryExporter`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 61806.8
- **Functions:** 10/16 matched (target 13)
- **Missing functions:** `clone`, `fmt`, `new`, `clone_metrics`, `clone_data`, `clone_inner`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_

### 29. trace.links

- **Target:** `trace.Links`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60710.0
- **Functions:** 0/3 matched (target 4)
- **Missing functions:** `deref`, `into_iter`, `add_link`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 30. trace.events

- **Target:** `trace.Events`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 60710.0
- **Functions:** 0/3 matched (target 4)
- **Missing functions:** `deref`, `into_iter`, `add_event`
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 31. logs.export

- **Target:** `logs.Export`
- **Similarity:** 0.47
- **Dependents:** 0
- **Priority Score:** 51305.3
- **Functions:** 6/8 matched (target 7)
- **Missing functions:** `new_with_owned_data`, `next`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `LogBatchData`, `LogBatchDataIter`, `Item`
- **Lint issues:** 1

### 32. metrics.manual_reader

- **Target:** `metrics.ManualReader`
- **Similarity:** 0.42
- **Dependents:** 0
- **Priority Score:** 41405.8
- **Functions:** 8/11 matched (target 8)
- **Missing functions:** `default`, `fmt`, `new`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `ManualReaderInner`
- **Lint issues:** 1

### 33. trace.in_memory_exporter

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

### 34. logs.in_memory_exporter

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

### 35. logs.logger

- **Target:** `logs.Logger`
- **Similarity:** 0.66
- **Dependents:** 0
- **Priority Score:** 20603.4
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `new`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `LogRecord`

### 36. metrics.view

- **Target:** `metrics.View [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 20210.0
- **Functions:** 0/1 matched (target 0)
- **Missing functions:** `match_inst`
- **Types:** 0/1 matched (target 0)
- **Missing types:** `View`

### 37. trace.error

- **Target:** `trace.Error`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 10407.9
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 2/3 matched (target 6)
- **Missing types:** `Custom`

### 38. metrics.error

- **Target:** `metrics.Error`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10310.0
- **Functions:** 0/1 matched
- **Missing functions:** `from`
- **Types:** 2/2 matched (target 5)
- **Missing types:** _none_

### 39. trace.span_limit

- **Target:** `trace.SpanLimits`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10210.0
- **Functions:** 0/1 matched
- **Missing functions:** `default`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 40. lib

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

### 41. util

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

### 42. trace.export

- **Target:** `trace.Export`
- **Similarity:** 0.65
- **Dependents:** 0
- **Priority Score:** 603.5
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 43. id_generator.mod

- **Target:** `trace.IdGenerator [STUB]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 410.0
- **Functions:** 2/2 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 44. metrics.noop

- **Target:** `metrics.Noop`
- **Similarity:** 0.83
- **Dependents:** 0
- **Priority Score:** 401.7
- **Functions:** 2/2 matched (target 4)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Lint issues:** 2

### 45. metrics.reader

- **Target:** `metrics.Reader`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 304.5
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 46. metrics.exporter

- **Target:** `metrics.Exporter`
- **Similarity:** 0.55
- **Dependents:** 0
- **Priority Score:** 204.5
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 47. resource.telemetry

- **Target:** `resource.Telemetry`
- **Similarity:** 0.58
- **Dependents:** 0
- **Priority Score:** 204.2
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 48. resource.attributes

- **Target:** `resource.Attributes [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 10.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 49. jaeger_remote.mod

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
| `propagation.mod` | `propagation.Mod` | 0 | `propagation/mod.rs` | `propagation/Mod.kt` |
| `testing.metrics.mod` | `testing.metrics.Mod` | 0 | `testing/metrics/mod.rs` | `testing/metrics/Mod.kt` |
| `testing.mod` | `testing.Mod` | 0 | `testing/mod.rs` | `testing/Mod.kt` |
| `testing.trace.mod` | `testing.trace.Mod` | 0 | `testing/trace/mod.rs` | `testing/trace/Mod.kt` |

