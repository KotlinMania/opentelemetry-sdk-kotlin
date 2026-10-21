// port-lint: source trace/provider.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetry.Context
import io.github.kotlinmania.opentelemetry.InstrumentationScope
import io.github.kotlinmania.opentelemetry.SpanId
import io.github.kotlinmania.opentelemetry.TraceFlags
import io.github.kotlinmania.opentelemetry.trace.SamplingDecision
import io.github.kotlinmania.opentelemetry.trace.SpanBuilder
import io.github.kotlinmania.opentelemetry.trace.SpanContext
import io.github.kotlinmania.opentelemetry.trace.SpanKind
import io.github.kotlinmania.opentelemetry.trace.TracerProvider
import io.github.kotlinmania.opentelemetry.trace.hasActiveSpan
import io.github.kotlinmania.opentelemetry.trace.span
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.propagation.spanContext
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
) : TracerProvider {
    private val isShutdownRef = AtomicBoolean(false)

    public fun isShutdown(): Boolean = isShutdownRef.load()

    public fun spanProcessors(): List<SpanProcessor> = processors

    public fun config(): Config = config

    public fun resource(): Resource = config.resource

    override fun tracer(name: String): SdkTracer =
        tracerWithScope(InstrumentationScope(scopeName = name))

    public fun tracer(name: String, version: String?): SdkTracer =
        tracerWithScope(InstrumentationScope(scopeName = name, scopeVersion = version))

    override fun tracerWithScope(scope: InstrumentationScope): SdkTracer =
        SdkTracer(instrumentationScope = scope, provider = this)

    internal fun startSpanFromBuilder(
        builder: SpanBuilder,
        tracer: SdkTracer,
        parentCx: Context = Context.current(),
    ): Span {
        if (isShutdown()) {
            return Span(
                spanContext = SpanContext.NONE,
                initialData = null,
                tracer = tracer,
                spanLimits = config.spanLimits,
            )
        }

        val parentContext =
            if (parentCx.hasActiveSpan()) {
                parentCx.span().spanContext()
            } else {
                parentCx.spanContext()
            }
        val traceId =
            if (parentContext != null && parentContext.isValid()) {
                parentContext.traceId
            } else {
                builder.traceId ?: config.idGenerator.newTraceId()
            }
        val spanId = builder.spanId ?: config.idGenerator.newSpanId()
        val spanKind = builder.spanKind ?: SpanKind.Internal
        val builderAttributes = builder.attributes.orEmpty()
        val builderLinks = builder.links.orEmpty()
        val builderEvents = builder.events.orEmpty()

        val samplingResult =
            builder.samplingResult
                ?: config.sampler.shouldSample(
                    parentContext = parentContext,
                    traceId = traceId,
                    name = builder.name,
                    spanKind = spanKind,
                    attributes = builderAttributes,
                    links = builderLinks,
                )

        if (samplingResult.decision == SamplingDecision.Drop) {
            val sc =
                SpanContext(
                    traceId = traceId,
                    spanId = spanId,
                    traceFlags = TraceFlags.NOT_SAMPLED,
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

        val traceFlags =
            if (samplingResult.decision == SamplingDecision.RecordAndSample) {
                TraceFlags.SAMPLED
            } else {
                TraceFlags.NOT_SAMPLED
            }

        val spanContext =
            SpanContext(
                traceId = traceId,
                spanId = spanId,
                traceFlags = traceFlags,
                isRemote = false,
                traceState = samplingResult.traceState,
            )

        val allAttributes = builderAttributes + samplingResult.attributes
        val maxAttrs = config.spanLimits.maxAttributesPerSpan.toInt()
        val droppedAttrs = if (allAttributes.size > maxAttrs) (allAttributes.size - maxAttrs).toUInt() else 0u
        val cappedAttrs = allAttributes.take(maxAttrs)

        val maxAttrsPerLink = config.spanLimits.maxAttributesPerLink.toInt()
        val maxLinks = config.spanLimits.maxLinksPerSpan.toInt()
        val droppedLinks = if (builderLinks.size > maxLinks) (builderLinks.size - maxLinks).toUInt() else 0u
        val cappedLinks =
            builderLinks.take(maxLinks).map { link ->
                val dropped =
                    if (link.attributes.size > maxAttrsPerLink) {
                        (link.attributes.size - maxAttrsPerLink).toUInt() + link.droppedAttributesCount
                    } else {
                        link.droppedAttributesCount
                    }
                link.copy(
                    attributes = link.attributes.take(maxAttrsPerLink),
                    droppedAttributesCount = dropped,
                )
            }

        val maxAttrsPerEvent = config.spanLimits.maxAttributesPerEvent.toInt()
        val maxEvents = config.spanLimits.maxEventsPerSpan.toInt()
        val droppedEvents = if (builderEvents.size > maxEvents) (builderEvents.size - maxEvents).toUInt() else 0u
        val cappedEvents =
            builderEvents.take(maxEvents).map { event ->
                val dropped =
                    if (event.attributes.size > maxAttrsPerEvent) {
                        (event.attributes.size - maxAttrsPerEvent).toUInt() + event.droppedAttributesCount
                    } else {
                        event.droppedAttributesCount
                    }
                event.copy(
                    attributes = event.attributes.take(maxAttrsPerEvent),
                    droppedAttributesCount = dropped,
                )
            }

        val startTime = builder.startTime ?: Clock.System.now()
        val endTime = builder.endTime ?: startTime

        val spanData =
            SpanData(
                spanContext = spanContext,
                parentSpanId = if (parentContext != null && parentContext.isValid()) parentContext.spanId else SpanId.INVALID,
                parentSpanIsRemote = parentContext != null && parentContext.isValid() && parentContext.isRemote,
                spanKind = spanKind,
                name = builder.name,
                startTime = startTime,
                endTime = endTime,
                attributes = cappedAttrs,
                droppedAttributesCount = droppedAttrs,
                events = SpanEvents(cappedEvents, droppedEvents),
                links = SpanLinks(cappedLinks, droppedLinks),
                status = builder.status,
                instrumentationScope = tracer.instrumentationScope,
            )

        val span =
            Span(
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
    private var resource: Resource? = null

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
        this.resource = this.resource?.merge(resource) ?: resource
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
        val finalConfig = this.resource?.let { config.copy(resource = it) } ?: config
        val finalResource = finalConfig.resource
        for (processor in processors) {
            processor.setResource(finalResource)
        }
        return SdkTracerProvider(processors.toList(), finalConfig)
    }
}
