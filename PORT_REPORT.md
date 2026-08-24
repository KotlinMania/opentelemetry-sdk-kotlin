=== Deep Analysis: tmp/opentelemetry_sdk/src (rust) -> src/commonMain/kotlin (kotlin) ===

Scanning source codebase (rust)...
Codebase: tmp/opentelemetry_sdk/src (rust)
  Files: 72
  Total imports: 585
  Most depended: error (3 dependents)

Scanning target codebase (kotlin)...
Codebase: src/commonMain/kotlin (kotlin)
  Files: 8
  Total imports: 11

Comparing codebases...
Computing AST similarities...

=== Codebase Comparison Report ===

Source: tmp/opentelemetry_sdk/src (72 files)
Target: src/commonMain/kotlin (8 files)
Scoring invariant: FnSim is required function body/parameter similarity. Class/type and symbol parity are reported beside it; whole-file shape is diagnostic only.

Matched:   5 files
Unmatched: 67 source, 0 target

=== Matched Files (by porting priority) ===

Source                        Target                        FnSim     Dependents FunctionParityTypeParity  Priority
 --------------------------------------------------------------------------------------------------------------------------
error                         opentelemetrysdk.Error [PROVENANCE-FALLBACK]1.00      3          0/0           3/3         3000300.0 
growable_array                opentelemetrysdk.GrowableArr [PROVENANCE-FALLBACK]0.50      1          15/19         2/5         1072405.0 
lib                           opentelemetrysdk.Lib [STUB] [PROVENANCE-FALLBACK]0.00      0          0/1           1/1         10210.0   
util                          opentelemetrysdk.Util [PROVENANCE-FALLBACK]0.00      0          0/1           0/0         10110.0   
resource.attributes           resource.Attributes [ZERO] [PROVENANCE-FALLBACK]0.00      0          0/0           0/0         10.0      

=== Function and Symbol Details ===

error -> opentelemetrysdk.Error [PROVENANCE-FALLBACK]
  similarity: 1.00, priority: 3000300.0, dependents: 3
  provenance warning: port-lint provenance header matched only after fallback normalization: `src/error.rs` vs expected `error.rs`
  functions: 0/0 matched (target total: 0, required body score: 1.00)
  missing functions: none
  types: 3/3 matched (target total: 6)
  missing types: none

growable_array -> opentelemetrysdk.GrowableArray [PROVENANCE-FALLBACK]
  similarity: 0.50, priority: 1072405.0, dependents: 1
  provenance warning: port-lint provenance header matched only after fallback normalization: `src/growable_array.rs` vs expected `growable_array.rs`
  provenance warning: port-lint provenance header matched only after fallback normalization: `tests:src/growable_array.rs` vs expected `growable_array.rs`
  functions: 15/19 matched (target total: 20, required body score: 0.50)
  missing functions: default, new, get_iterator, test_key_value_pair_storage_growable_array
  types: 2/5 matched (target total: 3)
  missing types: Item, IntoIter, KeyValuePair
  tests: 9/10 matched

lib -> opentelemetrysdk.Lib [STUB] [PROVENANCE-FALLBACK]
  similarity: 0.00, priority: 10210.0, dependents: 0
  provenance warning: port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
  functions: 0/1 matched (target total: 2, required body score: 0.00)
  missing functions: from
  types: 1/1 matched (target total: 5)
  missing types: none
  *** CHEAT DETECTION / SCORING FAILURE ***
  function-by-function score forced to 0: target contains TODO/stub/placeholder markers in function bodies; Lib.kt: snake_case identifier `opentelemetry_stdout` in Kotlin comments; InMemoryExporterError.kt: Rust attribute syntax in Kotlin comments

util -> opentelemetrysdk.Util [PROVENANCE-FALLBACK]
  similarity: 0.00, priority: 10110.0, dependents: 0
  provenance warning: port-lint provenance header matched only after fallback normalization: `src/util.rs` vs expected `util.rs`
  functions: 0/1 matched (target total: 1, required body score: 0.00)
  missing functions: tokio_interval_stream
  types: 0/0 matched (target total: 0)
  missing types: none

resource.attributes -> resource.Attributes [ZERO] [PROVENANCE-FALLBACK]
  similarity: 0.00, priority: 10.0, dependents: 0
  provenance warning: port-lint provenance header matched only after fallback normalization: `src/resource/attributes.rs` vs expected `resource/attributes.rs`
  functions: 0/0 matched (target total: 0, required body score: 0.00)
  missing functions: none
  types: 0/0 matched (target total: 0)
  missing types: none
  *** CHEAT DETECTION / SCORING FAILURE ***
  function-by-function score forced to 0: Attributes.kt: snake_case identifier `unknown_service` in Kotlin comments


=== Scores Forced To 0 ===

  - lib -> opentelemetrysdk.Lib: target contains TODO/stub/placeholder markers in function bodies; Lib.kt: snake_case identifier `opentelemetry_stdout` in Kotlin comments; InMemoryExporterError.kt: Rust attribute syntax in Kotlin comments
  - resource.attributes -> resource.Attributes: Attributes.kt: snake_case identifier `unknown_service` in Kotlin comments

=== Provenance Header Fallbacks ===

These files were paired only after normalization; fix the port-lint source header.
  - error -> opentelemetrysdk.Error: port-lint provenance header matched only after fallback normalization: `src/error.rs` vs expected `error.rs`
    proposed: // port-lint: source error.rs
  - growable_array -> opentelemetrysdk.GrowableArray: port-lint provenance header matched only after fallback normalization: `src/growable_array.rs` vs expected `growable_array.rs`
    proposed: // port-lint: source growable_array.rs
  - growable_array -> opentelemetrysdk.GrowableArray: port-lint provenance header matched only after fallback normalization: `tests:src/growable_array.rs` vs expected `growable_array.rs`
    proposed: // port-lint: tests growable_array.rs
  - lib -> opentelemetrysdk.Lib: port-lint provenance header matched only after fallback normalization: `src/lib.rs` vs expected `lib.rs`
    proposed: // port-lint: source lib.rs
  - util -> opentelemetrysdk.Util: port-lint provenance header matched only after fallback normalization: `src/util.rs` vs expected `util.rs`
    proposed: // port-lint: source util.rs
  - resource.attributes -> resource.Attributes: port-lint provenance header matched only after fallback normalization: `src/resource/attributes.rs` vs expected `resource/attributes.rs`
    proposed: // port-lint: source resource/attributes.rs

=== Missing from Target (need to port) ===

File                          Deps    Path
------------------------------------------------------------------------------
trace.span                    3       trace/span.rs
resource.env                  3       resource/env.rs
logs.log_processor            3       logs/log_processor.rs
metrics.meter_provider        2       metrics/meter_provider.rs
runtime                       2       runtime.rs
propagation.trace_context     2       propagation/trace_context.rs
trace.tracer                  1       trace/tracer.rs
metrics.periodic_reader       1       metrics/periodic_reader.rs
trace.span_processor          1       trace/span_processor.rs
trace.config                  1       trace/config.rs
logs.simple_log_processor     1       logs/simple_log_processor.rs
metrics.aggregation           1       metrics/aggregation.rs
metrics.metric_reader         1       testing/metrics/metric_reader.rs
logs.batch_log_processor      1       logs/batch_log_processor.rs
metrics.pipeline              1       metrics/pipeline.rs
logs.logger_provider          0       logs/logger_provider.rs
metrics.view                  0       metrics/view.rs
internal.aggregate            0       metrics/internal/aggregate.rs
internal.exponential_histogr  0       metrics/internal/exponential_histogram.rs
internal.histogram            0       metrics/internal/histogram.rs
internal.last_value           0       metrics/internal/last_value.rs
internal.mod                  0       metrics/internal/mod.rs
internal.precomputed_sum      0       metrics/internal/precomputed_sum.rs
internal.sum                  0       metrics/internal/sum.rs
metrics.manual_reader         0       metrics/manual_reader.rs
metrics.meter                 0       metrics/meter.rs
logs.logger                   0       logs/logger.rs
metrics.mod                   0       metrics/mod.rs
metrics.noop                  0       metrics/noop.rs
metrics.in_memory_exporter    0       metrics/in_memory_exporter.rs
metrics.periodic_reader_with  0       metrics/periodic_reader_with_async_runtime.rs
metrics.exporter              0       metrics/exporter.rs
metrics.reader                0       metrics/reader.rs
metrics.error                 0       metrics/error.rs
propagation.baggage           0       propagation/baggage.rs
propagation.mod               0       propagation/mod.rs
logs.log_processor_with_asyn  0       logs/log_processor_with_async_runtime.rs
logs.in_memory_exporter       0       logs/in_memory_exporter.rs
resource.mod                  0       resource/mod.rs
resource.telemetry            0       resource/telemetry.rs
logs.export                   0       logs/export.rs
data.mod                      0       metrics/data/mod.rs
testing.metrics.mod           0       testing/metrics/mod.rs
testing.mod                   0       testing/mod.rs
testing.trace.mod             0       testing/trace/mod.rs
trace.span_exporters          0       testing/trace/span_exporters.rs
logs.record                   0       logs/record.rs
trace.error                   0       trace/error.rs
trace.events                  0       trace/events.rs
trace.export                  0       trace/export.rs
id_generator.mod              0       trace/id_generator/mod.rs
trace.in_memory_exporter      0       trace/in_memory_exporter.rs
trace.links                   0       trace/links.rs
trace.mod                     0       trace/mod.rs
trace.provider                0       trace/provider.rs
trace.runtime_tests           0       trace/runtime_tests.rs
trace.sampler                 0       trace/sampler.rs
jaeger_remote.mod             0       trace/sampler/jaeger_remote/mod.rs
jaeger_remote.rate_limit      0       trace/sampler/jaeger_remote/rate_limit.rs
jaeger_remote.remote          0       trace/sampler/jaeger_remote/remote.rs
jaeger_remote.sampler         0       trace/sampler/jaeger_remote/sampler.rs
jaeger_remote.sampling_strat  0       trace/sampler/jaeger_remote/sampling_strategy.rs
logs.concurrent_log_processo  0       logs/concurrent_log_processor.rs
trace.span_limit              0       trace/span_limit.rs
logs.mod                      0       logs/mod.rs
trace.span_processor_with_as  0       trace/span_processor_with_async_runtime.rs
metrics.instrument            0       metrics/instrument.rs

=== Porting Quality Summary ===

Matched by exact header:          0 / 5
Matched by provenance fallback:   5 / 5
Matched by name:                  0 / 5
Total TODOs in target: 0
Total lint errors:    6
Stub files:           1

=== Big Picture ===

- Missing files: 67
- Incomplete ports (similarity < 60%): 4
- Stub files: 1
- Files missing functions: 3 (total deficit: 6 functions)
- Type definitions missing: 3
- Files missing tests: 1 (total deficit: 1 unported `#[test]` functions)
- Documentation coverage: 110 / 362 lines (30%)

Primary focus: create missing files (highest deps first)

=== Files with Issues ===

File                          Similarity LineRatio  FunctionParityTests     TODOs Lint  Status
----------------------------------------------------------------------------------------------------
opentelemetrysdk.Error [PROV  1.00       0.00       -             -         0     1     LINT
opentelemetrysdk.GrowableArr  0.50       0.00       15/19         9/10      0     2     MISSING_FUNCS
  missing functions: `default`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
  missing types: `Item`, `IntoIter`, `KeyValuePair`
opentelemetrysdk.Lib [STUB]   0.00       0.00       0/1           -         0     1     STUB
  missing functions: `from`
opentelemetrysdk.Util [PROVE  0.00       0.00       0/1           -         0     1     LOW_SIM
  missing functions: `tokio_interval_stream`
resource.Attributes [ZERO] [  0.00       0.00       -             -         0     1     LOW_SIM

=== Porting Recommendations ===

Incomplete ports (similarity < 60%): 4
Missing files: 67

Incomplete ports to complete:
  growable_array                 similarity=0.50 function_parity=15/19 dependents=1
    missing functions: `default`, `new`, `get_iterator`, `test_key_value_pair_storage_growable_array`
    missing types: `Item`, `IntoIter`, `KeyValuePair`
  lib                            similarity=0.00 function_parity=0/1 dependents=0 [STUB]
    missing functions: `from`
  util                           similarity=0.00 function_parity=0/1 dependents=0
    missing functions: `tokio_interval_stream`
  resource.attributes            similarity=0.00 function_parity=- dependents=0

=== Missing Files (by Dependents) ===

Source File                   Expected Target                       Dependents Path
-----------------------------------------------------------------------------------------------------------------------
trace.span                    trace.Span                            3          trace/span.rs
resource.env                  resource.Env                          3          resource/env.rs
logs.log_processor            logs.LogProcessor                     3          logs/log_processor.rs
metrics.meter_provider        metrics.MeterProvider                 2          metrics/meter_provider.rs
runtime                       Runtime                               2          runtime.rs
propagation.trace_context     propagation.TraceContext              2          propagation/trace_context.rs
logs.batch_log_processor      logs.BatchLogProcessor                1          logs/batch_log_processor.rs
metrics.periodic_reader       metrics.PeriodicReader                1          metrics/periodic_reader.rs
trace.span_processor          trace.SpanProcessor                   1          trace/span_processor.rs
logs.simple_log_processor     logs.SimpleLogProcessor               1          logs/simple_log_processor.rs
metrics.aggregation           metrics.Aggregation                   1          metrics/aggregation.rs
trace.config                  trace.Config                          1          trace/config.rs
metrics.metric_reader         testing.metrics.MetricReader          1          testing/metrics/metric_reader.rs
trace.tracer                  trace.Tracer                          1          trace/tracer.rs
metrics.pipeline              metrics.Pipeline                      1          metrics/pipeline.rs
logs.log_processor_with_asyn  logs.LogProcessorWithAsyncRuntime     0          logs/log_processor_with_async_runtime.rs
logs.logger_provider          logs.LoggerProvider                   0          logs/logger_provider.rs
internal.histogram            metrics.internal.Histogram            0          metrics/internal/histogram.rs
internal.last_value           metrics.internal.LastValue            0          metrics/internal/last_value.rs
internal.precomputed_sum      metrics.internal.PrecomputedSum       0          metrics/internal/precomputed_sum.rs
internal.sum                  metrics.internal.Sum                  0          metrics/internal/sum.rs
metrics.manual_reader         metrics.ManualReader                  0          metrics/manual_reader.rs
metrics.meter                 metrics.Meter                         0          metrics/meter.rs
logs.logger                   logs.Logger                           0          logs/logger.rs
metrics.noop                  metrics.Noop                          0          metrics/noop.rs
internal.aggregate            metrics.internal.Aggregate            0          metrics/internal/aggregate.rs
metrics.periodic_reader_with  metrics.PeriodicReaderWithAsyncRunti  0          metrics/periodic_reader_with_async_runtime.rs
metrics.instrument            metrics.Instrument                    0          metrics/instrument.rs
metrics.reader                metrics.Reader                        0          metrics/reader.rs
metrics.view                  metrics.View                          0          metrics/view.rs
propagation.baggage           propagation.Baggage                   0          propagation/baggage.rs
metrics.in_memory_exporter    metrics.InMemoryExporter              0          metrics/in_memory_exporter.rs
logs.in_memory_exporter       logs.InMemoryExporter                 0          logs/in_memory_exporter.rs
resource.telemetry            resource.Telemetry                    0          resource/telemetry.rs
logs.export                   logs.Export                           0          logs/export.rs
metrics.exporter              metrics.Exporter                      0          metrics/exporter.rs
trace.span_exporters          testing.trace.SpanExporters           0          testing/trace/span_exporters.rs
metrics.error                 metrics.Error                         0          metrics/error.rs
trace.error                   trace.Error                           0          trace/error.rs
trace.events                  trace.Events                          0          trace/events.rs
trace.export                  trace.Export                          0          trace/export.rs
trace.in_memory_exporter      trace.InMemoryExporter                0          trace/in_memory_exporter.rs
trace.links                   trace.Links                           0          trace/links.rs
trace.provider                trace.Provider                        0          trace/provider.rs
trace.runtime_tests           trace.RuntimeTests                    0          trace/runtime_tests.rs
trace.sampler                 trace.Sampler                         0          trace/sampler.rs
jaeger_remote.rate_limit      trace.sampler.jaegerremote.RateLimit  0          trace/sampler/jaeger_remote/rate_limit.rs
jaeger_remote.remote          trace.sampler.jaegerremote.Remote     0          trace/sampler/jaeger_remote/remote.rs
jaeger_remote.sampler         trace.sampler.jaegerremote.Sampler    0          trace/sampler/jaeger_remote/sampler.rs
jaeger_remote.sampling_strat  trace.sampler.jaegerremote.SamplingS  0          trace/sampler/jaeger_remote/sampling_strategy.rs
logs.concurrent_log_processo  logs.ConcurrentLogProcessor           0          logs/concurrent_log_processor.rs
trace.span_limit              trace.SpanLimit                       0          trace/span_limit.rs
logs.record                   logs.Record                           0          logs/record.rs
trace.span_processor_with_as  trace.SpanProcessorWithAsyncRuntime   0          trace/span_processor_with_async_runtime.rs
internal.exponential_histogr  metrics.internal.ExponentialHistogra  0          metrics/internal/exponential_histogram.rs

=== Reexport / Wiring Modules (consult, don't transliterate) ===

logs.mod                      logs.Mod                              0          logs/mod.rs
data.mod                      metrics.data.Mod                      0          metrics/data/mod.rs
internal.mod                  metrics.internal.Mod                  0          metrics/internal/mod.rs
metrics.mod                   metrics.Mod                           0          metrics/mod.rs
propagation.mod               propagation.Mod                       0          propagation/mod.rs
resource.mod                  resource.Mod                          0          resource/mod.rs
testing.metrics.mod           testing.metrics.Mod                   0          testing/metrics/mod.rs
testing.mod                   testing.Mod                           0          testing/mod.rs
testing.trace.mod             testing.trace.Mod                     0          testing/trace/mod.rs
id_generator.mod              trace.idgenerator.Mod                 0          trace/id_generator/mod.rs
trace.mod                     trace.Mod                             0          trace/mod.rs
jaeger_remote.mod             trace.sampler.jaegerremote.Mod        0          trace/sampler/jaeger_remote/mod.rs

=== Documentation Gaps ===

There is missing documentation that is hurting overall scoring.
Documentation coverage: 110 / 362 lines (30%)
Files with >20% doc gap: 4

File                          Src Docs    Tgt Docs    Gap %     DocSim    DocAmt    DocEq     
----------------------------------------------------------------------------------------------
lib                           210         6           97%       0.39      0.03      0.21      
error                         46          28          39%       0.97      0.61      0.79      
resource.attributes           50          34          32%       0.99      0.68      0.84      
growable_array                52          36          30%       0.84      0.69      0.77      

=== Generating Reports ===

✅ Generated: port_status_report.md
✅ Generated: high_priority_ports.md
✅ Generated: NEXT_ACTIONS.md
✅ Generated: port_lint_proposed_changes.md

📁 All reports generated successfully!
