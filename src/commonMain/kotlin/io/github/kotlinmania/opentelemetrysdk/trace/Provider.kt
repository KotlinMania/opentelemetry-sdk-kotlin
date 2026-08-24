// port-lint: source trace/provider.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Creator and registry of named [SdkTracer] instances.
 */
@OptIn(ExperimentalAtomicApi::class)
public class SdkTracerProvider internal constructor(
    private val processors: List<SpanProcessor>,
    private val config: Config,
) {
    private val isShutdownRef = AtomicBoolean(false)

    public fun isShutdown(): Boolean = isShutdownRef.load()

    public fun spanProcessors(): List<SpanProcessor> = processors

    public fun config(): Config = config

    public fun resource(): Resource = config.resource

    public fun tracer(name: String): SdkTracer =
        tracerWithScope(InstrumentationScope(name = name))

    public fun tracer(name: String, version: String?): SdkTracer =
        tracerWithScope(InstrumentationScope(name = name, version = version))

    public fun tracerWithScope(scope: InstrumentationScope): SdkTracer =
        SdkTracer(instrumentationScope = scope, provider = this)

    internal fun startSpanFromBuilder(builder: SpanBuilder, tracer: SdkTracer): Span {
        if (isShutdown()) {
            return Span(
                spanContext = SpanContext.EMPTY,
                initialData = null,
                tracer = tracer,
                spanLimits = config.spanLimits,
            )
        }

        val parentContext = builder.parentContext
        val traceId = if (parentContext != null && parentContext.isValid) {
            parentContext.traceId
        } else {
            config.idGenerator.newTraceId()
        }
        val spanId = config.idGenerator.newSpanId()

        val samplingResult = config.sampler.shouldSample(
            parentContext = parentContext,
            traceId = traceId,
            name = builder.name,
            spanKind = builder.spanKind,
            attributes = builder.attributes,
            links = builder.links,
        )

        if (samplingResult.decision == SamplingDecision.DROP) {
            val sc = SpanContext(
                traceId = traceId,
                spanId = spanId,
                traceFlags = TraceFlags.DEFAULT,
                isRemote = false,
                traceState = samplingResult.traceState,
            )
            return Span(
                spanContext = sc,
                initialData = null,
                tracer = tracer,
                spanLimits = config.spanLimits,
            )
        }

        val traceFlags = if (samplingResult.decision == SamplingDecision.RECORD_AND_SAMPLE) {
            TraceFlags.SAMPLED
        } else {
            TraceFlags.DEFAULT
        }

        val spanContext = SpanContext(
            traceId = traceId,
            spanId = spanId,
            traceFlags = traceFlags,
            isRemote = false,
            traceState = samplingResult.traceState,
        )

        val allAttributes = builder.attributes + samplingResult.attributes
        val maxAttrs = config.spanLimits.maxAttributesPerSpan.toInt()
        val droppedAttrs = if (allAttributes.size > maxAttrs) (allAttributes.size - maxAttrs).toUInt() else 0u
        val cappedAttrs = allAttributes.take(maxAttrs)

        val maxLinks = config.spanLimits.maxLinksPerSpan.toInt()
        val droppedLinks = if (builder.links.size > maxLinks) (builder.links.size - maxLinks).toUInt() else 0u
        val cappedLinks = builder.links.take(maxLinks)

        val maxEvents = config.spanLimits.maxEventsPerSpan.toInt()
        val droppedEvents = if (builder.events.size > maxEvents) (builder.events.size - maxEvents).toUInt() else 0u
        val cappedEvents = builder.events.take(maxEvents)

        val startTime = builder.startTime ?: Clock.System.now()

        val spanData = SpanData(
            spanContext = spanContext,
            parentSpanId = parentContext?.spanId ?: SpanId.INVALID,
            parentSpanIsRemote = parentContext?.isRemote ?: false,
            spanKind = builder.spanKind,
            name = builder.name,
            startTime = startTime,
            endTime = startTime,
            attributes = cappedAttrs,
            droppedAttributesCount = droppedAttrs,
            events = SpanEvents(cappedEvents, droppedEvents),
            links = SpanLinks(cappedLinks, droppedLinks),
            status = builder.status,
            instrumentationScope = tracer.instrumentationScope,
        )

        val span = Span(
            spanContext = spanContext,
            initialData = spanData,
            tracer = tracer,
            spanLimits = config.spanLimits,
        )

        for (processor in processors) {
            processor.onStart(span, parentContext)
        }

        return span
    }

    public fun forceFlush(): OTelSdkResult {
        for (processor in processors) {
            val res = processor.forceFlush()
            if (res.isFailure) return res
        }
        return Result.success(Unit)
    }

    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        isShutdownRef.store(true)
        for (processor in processors) {
            val res = processor.shutdownWithTimeout(timeout)
            if (res.isFailure) return res
        }
        return Result.success(Unit)
    }

    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    public companion object {
        public val EMPTY: SdkTracerProvider = SdkTracerProvider(emptyList(), Config())

        public fun builder(): TracerProviderBuilder = TracerProviderBuilder()
    }
}

/**
 * Builder for [SdkTracerProvider].
 */
public class TracerProviderBuilder {
    private val processors = mutableListOf<SpanProcessor>()
    private var config: Config = Config.defaultConfig()

    public fun withSpanProcessor(processor: SpanProcessor): TracerProviderBuilder {
        processors.add(processor)
        return this
    }

    public fun withSimpleExporter(exporter: SpanExporter): TracerProviderBuilder {
        processors.add(SimpleSpanProcessor(exporter))
        return this
    }

    public fun withBatchExporter(
        exporter: SpanExporter,
        batchConfig: BatchConfig = BatchConfig(),
    ): TracerProviderBuilder {
        processors.add(BatchSpanProcessor(exporter, batchConfig))
        return this
    }

    public fun withSampler(sampler: ShouldSample): TracerProviderBuilder {
        config = config.copy(sampler = sampler)
        return this
    }

    public fun withConfig(config: Config): TracerProviderBuilder {
        this.config = config
        return this
    }

    public fun withResource(resource: Resource): TracerProviderBuilder {
        config = config.copy(resource = resource)
        for (processor in processors) {
            processor.setResource(resource)
        }
        return this
    }

    public fun withIdGenerator(idGenerator: IdGenerator): TracerProviderBuilder {
        config = config.copy(idGenerator = idGenerator)
        return this
    }

    public fun withSpanLimits(spanLimits: SpanLimits): TracerProviderBuilder {
        config = config.copy(spanLimits = spanLimits)
        return this
    }

    public fun build(): SdkTracerProvider {
        val resource = config.resource
        for (processor in processors) {
            processor.setResource(resource)
        }
        return SdkTracerProvider(processors.toList(), config)
    }
}
