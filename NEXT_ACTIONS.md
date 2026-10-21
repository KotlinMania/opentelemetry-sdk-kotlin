# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 59/72 (81.9%)
- **Function parity:** 453/861 matched (target 629) — 52.6%
- **Class/type parity:** 133/207 matched (target 224) — 64.3%
- **Combined symbol parity:** 586/1068 matched (target 853) — 54.9%
- **Average inline-code cosine:** 0.17 (function body across 59 matched files)
- **Average documentation cosine:** 0.56 (doc text across 59 matched files)
- **Cheat-zeroed Files:** 1
- **Critical Issues:** 56 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 3 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. trace.span

- **Target:** `trace.Span`
- **Similarity:** 0.27
- **Dependents:** 3
- **Priority Score:** 3044007.2
- **Functions:** 35/38 matched (target 41)
- **Missing functions:** `span_context`, `drop`, `init`
- **Types:** 1/2 matched
- **Missing types:** `SpanData`
- **Tests:** 22/22 matched

### 2. logs.log_processor

- **Target:** `logs.LogProcessor`
- **Similarity:** 0.16
- **Dependents:** 3
- **Priority Score:** 3001608.5
- **Functions:** 12/12 matched
- **Missing functions:** _none_
- **Types:** 4/4 matched (target 5)
- **Missing types:** _none_
- **Tests:** 1/1 matched
- **Lint issues:** 1

### 3. resource.env

- **Target:** `resource.Env`
- **Similarity:** 0.14
- **Dependents:** 3
- **Priority Score:** 3000908.5
- **Functions:** 7/7 matched (target 9)
- **Missing functions:** _none_
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

- **Target:** `metrics.MeterProvider [ZERO]`
- **Similarity:** 0.00
- **Dependents:** 2
- **Priority Score:** 2072710.0
- **Functions:** 18/24 matched (target 21)
- **Missing functions:** `force_flush`, `shutdown_with_timeout`, `shutdown`, `drop`, `fmt`, `test_shutdown_invoked_on_last_drop`
- **Types:** 2/3 matched
- **Missing types:** `SdkMeterProviderInner`
- **Tests:** 6/7 matched
- **Lint issues:** 1

### 6. runtime

- **Target:** `opentelemetrysdk.Runtime`
- **Similarity:** 0.16
- **Dependents:** 2
- **Priority Score:** 2011708.4
- **Functions:** 8/8 matched (target 11)
- **Missing functions:** _none_
- **Types:** 8/9 matched (target 13)
- **Missing types:** `Message`

### 7. propagation.trace_context

- **Target:** `propagation.TraceContext`
- **Similarity:** 0.14
- **Dependents:** 2
- **Priority Score:** 2001508.6
- **Functions:** 14/14 matched (target 24)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 6)
- **Missing types:** _none_
- **Tests:** 5/5 matched

### 8. metrics.periodic_reader

- **Target:** `metrics.PeriodicReader`
- **Similarity:** 0.09
- **Dependents:** 1
- **Priority Score:** 1366509.1
- **Functions:** 25/59 matched (target 26)
- **Missing functions:** `new`, `clone`, `new`, `collect_and_export`, `fmt`, `collect_and_export`, `register_pipeline`, `collect`, `default`, `force_flush`, `shutdown`, `temporality`, `collection_triggered_by_interval_multiple`, `collection`, `collection_from_tokio_multi_with_one_worker`, `collection_from_tokio_with_two_worker`, `collection_from_tokio_current`, `collection_triggered_by_interval_helper`, `collection_triggered_by_flush_helper`, `collection_triggered_by_shutdown_helper`, `collection_triggered_by_drop_helper`, `collection_helper`, `some_async_function`, `async_inside_observable_callback_from_tokio_multi_with_one_worker`, `async_inside_observable_callback_from_tokio_multi_with_two_worker`, `async_inside_observable_callback_from_tokio_current_thread`, `async_inside_observable_callback_from_regular_main`, `async_inside_observable_callback_helper`, `some_tokio_async_function`, `tokio_async_inside_observable_callback_from_tokio_multi_with_one_worker`, `tokio_async_inside_observable_callback_from_tokio_multi_with_two_worker`, `tokio_async_inside_observable_callback_from_tokio_current_thread`, `tokio_async_inside_observable_callback_from_regular_main`, `tokio_async_inside_observable_callback_helper`
- **Types:** 4/6 matched (target 5)
- **Missing types:** `PeriodicReaderInner`, `Message`
- **Tests:** 6/10 matched

### 9. trace.span_processor

- **Target:** `trace.SpanProcessor`
- **Similarity:** 0.07
- **Dependents:** 1
- **Priority Score:** 1325909.2
- **Functions:** 21/51 matched (target 23)
- **Missing functions:** `new`, `new`, `get_spans_and_export`, `export_batch_sync`, `default`, `default`, `init_from_env_vars`, `simple_span_processor_on_end_calls_export`, `simple_span_processor_on_end_skips_export_if_not_sampled`, `simple_span_processor_shutdown_calls_shutdown`, `test_default_const_values`, `test_default_batch_config_adheres_to_specification`, `test_code_based_config_overrides_env_vars`, `test_batch_config_configurable_by_env_vars`, `test_batch_config_max_export_batch_size_validation`, `test_batch_config_with_fields`, `create_test_span`, `new`, `export`, `shutdown`, `set_resource`, `batchspanprocessor_handles_on_end`, `batchspanprocessor_force_flush`, `batchspanprocessor_shutdown`, `batchspanprocessor_handles_dropped_spans`, `validate_span_attributes_exported_correctly`, `batchspanprocessor_sets_and_exports_with_resource`, `test_batch_processor_current_thread_runtime`, `test_batch_processor_multi_thread_count_1_runtime`, `test_batch_processor_multi_thread`
- **Types:** 6/8 matched (target 6)
- **Missing types:** `BatchMessage`, `MockSpanExporter`
- **Tests:** 0/15 matched
- **Lint issues:** 5

### 10. logs.batch_log_processor

- **Target:** `logs.BatchLogProcessor`
- **Similarity:** 0.06
- **Dependents:** 1
- **Priority Score:** 1223909.4
- **Functions:** 13/33 matched (target 15)
- **Missing functions:** `fmt`, `get_logs_and_export`, `export_batch_sync`, `default`, `default`, `init_from_env_vars`, `test_default_const_values`, `test_default_batch_config_adheres_to_specification`, `test_code_based_config_overrides_env_vars`, `test_batch_config_configurable_by_env_vars`, `test_batch_config_max_export_batch_size_validation`, `test_batch_config_with_fields`, `test_build_batch_log_processor_builder`, `test_build_batch_log_processor_builder_with_custom_config`, `test_set_resource_batch_processor`, `test_batch_shutdown`, `test_batch_log_processor_shutdown_under_async_runtime_current_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_current_flavor_current_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_current_thread`
- **Types:** 4/6 matched (target 4)
- **Missing types:** `BatchMessage`, `LogsData`
- **Tests:** 0/8 matched

### 11. logs.simple_log_processor

- **Target:** `logs.SimpleLogProcessor`
- **Similarity:** 0.03
- **Dependents:** 1
- **Priority Score:** 1212809.6
- **Functions:** 6/25 matched (target 6)
- **Missing functions:** `shutdown`, `new`, `len`, `export`, `test_set_resource_simple_processor`, `test_simple_shutdown`, `test_simple_processor_sync_exporter_without_runtime`, `test_simple_processor_sync_exporter_with_runtime`, `test_simple_processor_sync_exporter_with_multi_thread_runtime`, `test_simple_processor_sync_exporter_with_current_thread_runtime`, `test_simple_processor_async_exporter_without_runtime`, `test_simple_processor_async_exporter_with_all_runtime_worker_threads_blocked`, `test_simple_processor_async_exporter_with_runtime`, `test_simple_processor_async_exporter_with_multi_thread_runtime`, `test_simple_processor_async_exporter_with_current_thread_runtime`, `new`, `set_logger`, `export`, `exporter_internal_log_does_not_deadlock_with_simple_processor`
- **Types:** 1/3 matched (target 1)
- **Missing types:** `LogExporterThatRequiresTokio`, `ReentrantLogExporter`
- **Tests:** 0/5 matched

### 12. metrics.pipeline

- **Target:** `metrics.Pipeline`
- **Similarity:** 0.11
- **Dependents:** 1
- **Priority Score:** 1103008.9
- **Functions:** 15/22 matched (target 19)
- **Missing functions:** `fmt`, `fmt`, `fmt`, `new`, `log_conflict`, `new`, `new`
- **Types:** 5/8 matched (target 7)
- **Missing types:** `GenericCallback`, `PipelineInner`, `Cache`

### 13. growable_array

- **Target:** `opentelemetrysdk.GrowableArray`
- **Similarity:** 0.19
- **Dependents:** 1
- **Priority Score:** 1082508.1
- **Functions:** 15/20 matched
- **Missing functions:** `default`, `new`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
- **Types:** 2/5 matched (target 3)
- **Missing types:** `Item`, `IntoIter`, `KeyValuePair`
- **Tests:** 9/10 matched

### 14. trace.tracer

- **Target:** `trace.Tracer`
- **Similarity:** 0.21
- **Dependents:** 1
- **Priority Score:** 1041507.9
- **Functions:** 9/12 matched (target 22)
- **Missing functions:** `fmt`, `instrumentation_scope`, `build_recording_span`
- **Types:** 2/3 matched (target 4)
- **Missing types:** `Span`
- **Tests:** 3/3 matched

### 15. metrics.aggregation

- **Target:** `metrics.Aggregation`
- **Similarity:** 0.10
- **Dependents:** 1
- **Priority Score:** 1010509.0
- **Functions:** 2/3 matched (target 8)
- **Missing functions:** `fmt`
- **Types:** 2/2 matched (target 9)
- **Missing types:** _none_
- **Tests:** 1/1 matched

### 16. metrics.metric_reader

- **Target:** `metrics.MetricReader`
- **Similarity:** 0.09
- **Dependents:** 1
- **Priority Score:** 1000909.1
- **Functions:** 8/8 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Lint issues:** 2

### 17. trace.config

- **Target:** `trace.Config`
- **Similarity:** 0.00
- **Dependents:** 1
- **Priority Score:** 1000210.0
- **Functions:** 1/1 matched (target 5)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 18. logs.logger_provider

- **Target:** `logs.LoggerProvider`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 425709.5
- **Functions:** 12/48 matched (target 13)
- **Missing functions:** `noop_logger_provider`, `shutdown_with_timeout`, `shutdown`, `drop`, `fmt`, `new`, `emit`, `force_flush`, `shutdown_with_timeout`, `new`, `resource`, `export`, `set_resource`, `shutdown_with_timeout`, `emit`, `force_flush`, `set_resource`, `new`, `resource`, `test_resource_handling_provider_processor_exporter`, `trace_context_test`, `shutdown_test`, `shutdown_idempotent_test`, `global_shutdown_test`, `drop_test_with_multiple_providers`, `drop_after_shutdown_test_with_multiple_providers`, `test_empty_logger_name`, `with_resource_multiple_calls_ensure_additive`, `new`, `emit`, `force_flush`, `shutdown_with_timeout`, `new`, `emit`, `force_flush`, `shutdown_with_timeout`
- **Types:** 3/9 matched (target 3)
- **Missing types:** `Logger`, `ShutdownTestLogProcessor`, `TestExporterForResource`, `TestProcessorForResource`, `LazyLogProcessor`, `CountingShutdownProcessor`
- **Tests:** 0/32 matched

### 19. logs.log_processor_with_async_runtime

- **Target:** `logs.LogProcessorWithAsyncRuntime`
- **Similarity:** 0.03
- **Dependents:** 0
- **Priority Score:** 414709.7
- **Functions:** 6/41 matched (target 7)
- **Missing functions:** `fmt`, `shutdown`, `new`, `export_with_timeout`, `export`, `set_resource`, `get_resource`, `test_default_const_values`, `test_default_batch_config_adheres_to_specification`, `test_batch_config_configurable_by_env_vars`, `test_batch_config_max_export_batch_size_validation`, `test_batch_config_with_fields`, `test_build_batch_log_processor_builder`, `test_build_batch_log_processor_builder_with_custom_config`, `test_set_resource_simple_processor`, `test_set_resource_batch_processor`, `test_batch_shutdown`, `test_batch_log_processor_shutdown_under_async_runtime_current_flavor_multi_thread`, `test_batch_log_processor_with_async_runtime_shutdown_under_async_runtime_current_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_current_flavor_current_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_multi_thread`, `test_batch_log_processor_shutdown_with_async_runtime_multi_flavor_current_thread`, `emit`, `force_flush`, `emit`, `force_flush`, `test_log_data_modification_by_multiple_processors`, `test_build_batch_log_processor_builder_rt`, `test_build_batch_log_processor_builder_rt_with_custom_config`, `test_set_resource_batch_processor_rt`, `test_batch_shutdown_rt`, `test_batch_log_processor_rt_shutdown_with_async_runtime_current_flavor_multi_thread`, `test_batch_log_processor_rt_shutdown_with_async_runtime_current_flavor_current_thread`, `test_batch_log_processor_rt_shutdown_with_async_runtime_multi_flavor_multi_thread`, `test_batch_log_processor_rt_shutdown_with_async_runtime_multi_flavor_current_thread`
- **Types:** 0/6 matched (target 7)
- **Missing types:** `BatchMessage`, `BatchLogProcessor`, `BatchLogProcessorBuilder`, `MockLogExporter`, `FirstProcessor`, `SecondProcessor`
- **Tests:** 0/11 matched
- **Lint issues:** 1

### 20. metrics.meter

- **Target:** `metrics.Meter`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 363810.0
- **Functions:** 1/36 matched (target 15)
- **Missing functions:** `new`, `create_counter`, `create_observable_counter`, `create_observable_updown_counter`, `create_observable_gauge`, `create_updown_counter`, `create_gauge`, `create_histogram`, `u64_counter`, `f64_counter`, `u64_observable_counter`, `f64_observable_counter`, `i64_up_down_counter`, `f64_up_down_counter`, `i64_observable_up_down_counter`, `f64_observable_up_down_counter`, `u64_gauge`, `f64_gauge`, `i64_gauge`, `u64_observable_gauge`, `i64_observable_gauge`, `f64_observable_gauge`, `f64_histogram`, `u64_histogram`, `validate_bucket_boundaries`, `validate_instrument_name`, `validate_instrument_name`, `validate_instrument_unit`, `fmt`, `new`, `lookup`, `measures`, `instrument_name_validation`, `instrument_name_validation_disabled`, `instrument_unit_validation`
- **Types:** 1/2 matched (target 7)
- **Missing types:** `InstrumentResolver`
- **Tests:** 0/3 matched
- **Lint issues:** 1

### 21. internal.exponential_histogram

- **Target:** `internal.ExponentialHistogram`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 284408.9
- **Functions:** 12/35 matched (target 18)
- **Missing functions:** `new`, `scale_factors`, `create`, `new`, `test_expo_histogram_data_point_record`, `run_data_point_record`, `run_min_max_sum_f64`, `new`, `run_min_max_sum`, `new`, `run_data_point_record_f64`, `data_point_record_limits`, `expo_bucket_downscale`, `expo_bucket_record`, `scale_change_rescaling`, `sub_normal`, `hist_aggregations`, `hist_aggregation`, `assert_aggregation_eq`, `assert_sum_data_points_eq`, `assert_gauge_data_points_eq`, `assert_hist_data_points_eq`, `assert_exponential_hist_data_points_eq`
- **Types:** 4/9 matched (target 6)
- **Missing types:** `InitConfig`, `PreComputedValue`, `TestCase`, `Expected`, `Args`
- **Tests:** 0/19 matched

### 22. trace.provider

- **Target:** `trace.Provider`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 245408.8
- **Functions:** 24/46 matched (target 37)
- **Missing functions:** `noop_tracer_provider`, `drop`, `default`, `new`, `shutdown`, `with_max_events_per_span`, `with_max_attributes_per_span`, `with_max_links_per_span`, `with_max_attributes_per_event`, `with_max_attributes_per_link`, `started_span_count`, `new`, `assert_info`, `on_start`, `on_end`, `force_flush`, `shutdown_with_timeout`, `new`, `on_start`, `on_end`, `force_flush`, `shutdown_with_timeout`
- **Types:** 6/8 matched (target 7)
- **Missing types:** `TracerProviderInner`, `Tracer`
- **Tests:** 6/18 matched

### 23. trace.span_processor_with_async_runtime

- **Target:** `trace.SpanProcessorWithAsyncRuntime`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 233109.4
- **Functions:** 8/25 matched (target 8)
- **Missing functions:** `fmt`, `flush`, `process_message`, `export`, `run`, `new`, `fmt`, `export`, `fmt`, `export`, `test_build_batch_span_processor_builder`, `test_batch_span_processor`, `timeout_test_tokio`, `test_timeout_tokio_timeout`, `test_timeout_tokio_not_timeout`, `test_concurrent_exports_expected`, `test_exports_serial_when_max_concurrent_exports_1`
- **Types:** 0/6 matched (target 7)
- **Missing types:** `BatchSpanProcessor`, `BatchMessage`, `BatchSpanProcessorInternal`, `BatchSpanProcessorBuilder`, `BlockingExporter`, `TrackingExporter`
- **Tests:** 0/1 matched
- **Lint issues:** 3

### 24. metrics.periodic_reader_with_async_runtime

- **Target:** `metrics.PeriodicReaderWithAsyncRuntime`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 203009.4
- **Functions:** 10/24 matched (target 11)
- **Missing functions:** `new`, `clone`, `fmt`, `process_message`, `run`, `collection_triggered_by_interval_tokio_current`, `collection_triggered_by_interval_from_tokio_multi_one_thread_on_runtime_tokio`, `collection_triggered_by_interval_from_tokio_multi_two_thread_on_runtime_tokio`, `collection_triggered_by_interval_from_tokio_multi_one_thread_on_runtime_tokio_current`, `collection_triggered_by_interval_from_tokio_multi_two_thread_on_runtime_tokio_current`, `collection_triggered_by_interval_from_tokio_current_on_runtime_tokio`, `collection_triggered_by_interval_from_tokio_current_on_runtime_tokio_current`, `unregistered_collect`, `collection_triggered_by_interval_helper`
- **Types:** 0/6 matched
- **Missing types:** `PeriodicReaderBuilder`, `PeriodicReader`, `PeriodicReaderInner`, `Message`, `ProducerOrWorker`, `PeriodicReaderWorker`
- **Tests:** 0/2 matched
- **Lint issues:** 1

### 25. metrics.instrument

- **Target:** `metrics.Instrument`
- **Similarity:** 0.16
- **Dependents:** 0
- **Priority Score:** 133108.4
- **Functions:** 14/24 matched (target 16)
- **Missing functions:** `name`, `kind`, `unit`, `scope`, `new`, `validate_bucket_boundaries`, `normalize`, `measure`, `new`, `observe`
- **Types:** 4/7 matched (target 5)
- **Missing types:** `InstrumentId`, `ResolvedMeasures`, `Observable`
- **Tests:** 5/5 matched

### 26. logs.record

- **Target:** `logs.Record`
- **Similarity:** 0.26
- **Dependents:** 0
- **Priority Score:** 103807.4
- **Functions:** 26/35 matched
- **Missing functions:** `event_name`, `target`, `timestamp`, `observed_timestamp`, `trace_context`, `severity_text`, `severity_number`, `body`, `compare_log_record_target_borrowed_eq_owned`
- **Types:** 2/3 matched
- **Missing types:** `LogRecordAttributes`
- **Tests:** 11/12 matched

### 27. internal.aggregate

- **Target:** `internal.Aggregate`
- **Similarity:** 0.07
- **Dependents:** 0
- **Priority Score:** 102509.3
- **Functions:** 8/17 matched (target 8)
- **Missing functions:** `from`, `default`, `new`, `new`, `last_value_aggregation`, `precomputed_sum_aggregation`, `sum_aggregation`, `explicit_bucket_histogram_aggregation`, `exponential_histogram_aggregation`
- **Types:** 7/8 matched (target 7)
- **Missing types:** `Filter`
- **Tests:** 0/5 matched

### 28. metrics.in_memory_exporter

- **Target:** `metrics.InMemoryExporter`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 82009.5
- **Functions:** 10/18 matched (target 13)
- **Missing functions:** `clone`, `fmt`, `fmt`, `default`, `new`, `clone_metrics`, `clone_data`, `clone_inner`
- **Types:** 2/2 matched (target 3)
- **Missing types:** _none_

### 29. trace.sampler

- **Target:** `trace.Sampler`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 81209.6
- **Functions:** 2/9 matched (target 12)
- **Missing functions:** `box_clone`, `clone`, `jaeger_remote`, `sampler_data`, `sampling`, `clone_a_parent_sampler`, `parent_sampler`
- **Types:** 2/3 matched (target 9)
- **Missing types:** `CloneShouldSample`
- **Tests:** 0/3 matched
- **Lint issues:** 4

### 30. trace.runtime_tests

- **Target:** `trace.RuntimeTests`
- **Similarity:** 0.02
- **Dependents:** 0
- **Priority Score:** 81109.8
- **Functions:** 2/10 matched (target 2)
- **Missing functions:** `build_batch_tracer_provider`, `build_simple_tracer_provider`, `test_set_provider_in_tokio`, `test_set_provider_multiple_thread_tokio`, `test_set_provider_multiple_thread_tokio_shutdown`, `test_set_provider_single_thread_tokio_with_simple_processor`, `test_set_provider_single_thread_tokio`, `test_set_provider_single_thread_tokio_shutdown`
- **Types:** 1/1 matched
- **Missing types:** _none_

### 31. jaeger_remote.sampler

- **Target:** `jaegerremote.Sampler`
- **Similarity:** 0.10
- **Dependents:** 0
- **Priority Score:** 71409.0
- **Functions:** 5/12 matched (target 7)
- **Missing functions:** `new`, `get_endpoint`, `new`, `run_update_task`, `request_new_strategy`, `fmt`, `deserialize_sampling_strategy_response`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 32. metrics.manual_reader

- **Target:** `metrics.ManualReader`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 61608.8
- **Functions:** 8/13 matched (target 8)
- **Missing functions:** `default`, `fmt`, `new`, `fmt`, `new`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `ManualReaderInner`
- **Lint issues:** 1

### 33. internal.histogram

- **Target:** `internal.Histogram`
- **Similarity:** 0.08
- **Dependents:** 0
- **Priority Score:** 61409.2
- **Functions:** 6/10 matched
- **Missing functions:** `create`, `new`, `new`, `check_buckets_are_selected_correctly`
- **Types:** 2/4 matched
- **Missing types:** `InitConfig`, `PreComputedValue`
- **Tests:** 0/1 matched

### 34. logs.export

- **Target:** `logs.Export`
- **Similarity:** 0.11
- **Dependents:** 0
- **Priority Score:** 51308.9
- **Functions:** 6/8 matched (target 7)
- **Missing functions:** `new_with_owned_data`, `next`
- **Types:** 2/5 matched (target 2)
- **Missing types:** `LogBatchData`, `LogBatchDataIter`, `Item`
- **Lint issues:** 1

### 35. trace.span_exporters

- **Target:** `trace.SpanExporters`
- **Similarity:** 0.06
- **Dependents:** 0
- **Priority Score:** 51209.4
- **Functions:** 5/9 matched (target 6)
- **Missing functions:** `shutdown`, `exporter_name`, `fmt`, `from`
- **Types:** 2/3 matched (target 2)
- **Missing types:** `TestExportError`
- **Lint issues:** 1

### 36. propagation.baggage

- **Target:** `propagation.Baggage`
- **Similarity:** 0.12
- **Dependents:** 0
- **Priority Score:** 41408.8
- **Functions:** 9/13 matched (target 19)
- **Missing functions:** `valid_extract_data`, `valid_extract_data_with_metadata`, `valid_inject_data`, `valid_inject_data_metadata`
- **Types:** 1/1 matched (target 5)
- **Missing types:** _none_
- **Tests:** 4/8 matched

### 37. trace.in_memory_exporter

- **Target:** `trace.InMemoryExporter`
- **Similarity:** 0.04
- **Dependents:** 0
- **Priority Score:** 41309.6
- **Functions:** 7/11 matched (target 10)
- **Missing functions:** `default`, `default`, `new`, `keep_records_on_shutdown`
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Lint issues:** 1

### 38. internal.last_value

- **Target:** `internal.LastValue`
- **Similarity:** 0.19
- **Dependents:** 0
- **Priority Score:** 41208.1
- **Functions:** 6/8 matched
- **Missing functions:** `create`, `new`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `InitConfig`, `PreComputedValue`

### 39. internal.sum

- **Target:** `internal.Sum`
- **Similarity:** 0.21
- **Dependents:** 0
- **Priority Score:** 41207.9
- **Functions:** 6/8 matched (target 9)
- **Missing functions:** `create`, `new`
- **Types:** 2/4 matched (target 3)
- **Missing types:** `InitConfig`, `PreComputedValue`

### 40. logs.in_memory_exporter

- **Target:** `logs.InMemoryExporter`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 31509.5
- **Functions:** 8/11 matched (target 12)
- **Missing functions:** `default`, `default`, `keep_records_on_shutdown`
- **Types:** 4/4 matched
- **Missing types:** _none_
- **Tests:** 0/1 matched
- **Lint issues:** 1

### 41. jaeger_remote.sampling_strategy

- **Target:** `jaegerremote.SamplingStrategy`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 31108.7
- **Functions:** 5/8 matched (target 5)
- **Missing functions:** `fmt`, `drop`, `new`
- **Types:** 3/3 matched (target 6)
- **Missing types:** _none_

### 42. trace.events

- **Target:** `trace.Events`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 30708.7
- **Functions:** 3/3 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 43. trace.links

- **Target:** `trace.Links`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 30708.7
- **Functions:** 3/3 matched (target 9)
- **Missing functions:** _none_
- **Types:** 1/4 matched (target 2)
- **Missing types:** `Target`, `Item`, `IntoIter`

### 44. jaeger_remote.rate_limit

- **Target:** `jaegerremote.RateLimit`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 30708.7
- **Functions:** 3/6 matched (target 3)
- **Missing functions:** `new`, `test_leaky_bucket`, `test_rewind_clock_should_pass`
- **Types:** 1/1 matched
- **Missing types:** _none_
- **Tests:** 0/2 matched

### 45. trace.error

- **Target:** `trace.Error`
- **Similarity:** 0.05
- **Dependents:** 0
- **Priority Score:** 20709.5
- **Functions:** 2/4 matched (target 2)
- **Missing functions:** `from`, `from`
- **Types:** 3/3 matched (target 7)
- **Missing types:** _none_

### 46. logs.logger

- **Target:** `logs.Logger`
- **Similarity:** 0.27
- **Dependents:** 0
- **Priority Score:** 20607.3
- **Functions:** 3/4 matched (target 3)
- **Missing functions:** `new`
- **Types:** 1/2 matched (target 1)
- **Missing types:** `LogRecord`

### 47. internal.precomputed_sum

- **Target:** `internal.PrecomputedSum`
- **Similarity:** 0.18
- **Dependents:** 0
- **Priority Score:** 10608.2
- **Functions:** 4/5 matched (target 6)
- **Missing functions:** `new`
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 48. logs.concurrent_log_processor

- **Target:** `logs.ConcurrentLogProcessor`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 706.8
- **Functions:** 6/6 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 49. jaeger_remote.remote

- **Target:** `jaegerremote.Remote`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 700.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 7/7 matched
- **Missing types:** _none_

### 50. trace.export

- **Target:** `trace.Export`
- **Similarity:** 0.13
- **Dependents:** 0
- **Priority Score:** 608.7
- **Functions:** 4/4 matched
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Lint issues:** 1

### 51. metrics.noop

- **Target:** `metrics.Noop`
- **Similarity:** 0.14
- **Dependents:** 0
- **Priority Score:** 508.6
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_
- **Lint issues:** 2

### 52. metrics.reader

- **Target:** `metrics.Reader`
- **Similarity:** 0.08
- **Dependents:** 0
- **Priority Score:** 309.2
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched
- **Missing types:** _none_

### 53. metrics.error

- **Target:** `metrics.Error`
- **Similarity:** 0.37
- **Dependents:** 0
- **Priority Score:** 306.3
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 2/2 matched (target 5)
- **Missing types:** _none_

### 54. trace.span_limit

- **Target:** `trace.SpanLimits`
- **Similarity:** 0.00
- **Dependents:** 0
- **Priority Score:** 210.0
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 55. metrics.exporter

- **Target:** `metrics.Exporter`
- **Similarity:** 0.08
- **Dependents:** 0
- **Priority Score:** 209.2
- **Functions:** 1/1 matched (target 3)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 56. resource.telemetry

- **Target:** `resource.Telemetry`
- **Similarity:** 0.22
- **Dependents:** 0
- **Priority Score:** 207.8
- **Functions:** 1/1 matched
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 57. metrics.view

- **Target:** `metrics.View`
- **Similarity:** 0.32
- **Dependents:** 0
- **Priority Score:** 206.8
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 1/1 matched
- **Missing types:** _none_

### 58. util

- **Target:** `opentelemetrysdk.Util`
- **Similarity:** 0.16
- **Dependents:** 0
- **Priority Score:** 108.4
- **Functions:** 1/1 matched (target 2)
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

### 59. resource.attributes

- **Target:** `resource.Attributes`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
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

