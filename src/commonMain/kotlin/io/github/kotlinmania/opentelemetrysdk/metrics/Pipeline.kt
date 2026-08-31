// port-lint: source opentelemetry_sdk/src/metrics/pipeline.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.data.Metric
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ScopeMetrics
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.AggregateBuilder
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.AggregateFns
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.ComputeAggregation
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.Measure
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.NumberOps
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal const val DEFAULT_CARDINALITY_LIMIT: Int = 2000

internal data class InstrumentSync(
    val name: String,
    val description: String,
    val unit: String,
    val compAgg: ComputeAggregation,
)

internal data class InstrumentId(
    var name: String,
    var description: String,
    var kind: InstrumentKind,
    var unit: String,
    var number: String,
) {
    fun normalize() {
        if (name.any { it.isUpperCase() }) {
            name = name.lowercase()
        }
    }
}

/**
 * Connects a [MetricReader] to instrument aggregations.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class Pipeline(
    val resource: Resource,
    val reader: MetricReader,
    val views: List<View>,
) : SdkProducer {
    private val aggregations = AtomicReference<PersistentMap<InstrumentationScope, List<InstrumentSync>>>(persistentMapOf())
    private val callbacks = AtomicReference<List<() -> Unit>>(emptyList())

    init {
        reader.registerPipeline(this)
    }

    fun addSync(
        scope: InstrumentationScope,
        sync: InstrumentSync,
    ) {
        while (true) {
            val cur = aggregations.load()
            val existing = cur[scope] ?: emptyList()
            val next = cur.putting(scope, existing + sync)
            if (aggregations.compareAndSet(cur, next)) {
                break
            }
        }
    }

    fun addCallback(cb: () -> Unit) {
        while (true) {
            val current = callbacks.load()
            val updated = current + cb
            if (callbacks.compareAndSet(current, updated)) {
                return
            }
        }
    }

    override fun produce(rm: ResourceMetrics): OTelSdkResult {
        for (cb in callbacks.load()) {
            cb()
        }

        rm.resource = this.resource

        val aggMap = aggregations.load()
        val scopeMetricsList = ArrayList<ScopeMetrics>(aggMap.size)

        for ((scope, instruments) in aggMap) {
            val metricList = ArrayList<Metric>(instruments.size)
            for (inst in instruments) {
                val (len, newAgg) = inst.compAgg.call(null)
                if (len > 0 && newAgg != null) {
                    metricList.add(
                        Metric(
                            name = inst.name,
                            descriptionText = inst.description,
                            unit = inst.unit,
                            data = newAgg,
                        ),
                    )
                }
            }
            if (metricList.isNotEmpty()) {
                scopeMetricsList.add(
                    ScopeMetrics(
                        scope = scope,
                        metrics = metricList,
                    ),
                )
            }
        }

        rm.scopeMetrics = scopeMetricsList
        return Result.success(Unit)
    }

    fun forceFlush(): OTelSdkResult = reader.forceFlush()

    fun shutdown(): OTelSdkResult = reader.shutdown()
}

/**
 * Group of pipelines connecting Readers with instrument measurements.
 */
internal class Pipelines(
    val pipes: List<Pipeline>,
) {
    fun registerCallback(callback: () -> Unit) {
        for (pipe in pipes) {
            pipe.addCallback(callback)
        }
    }

    fun forceFlush(): OTelSdkResult {
        for (pipe in pipes) {
            val res = pipe.forceFlush()
            if (res.isFailure) return res
        }
        return Result.success(Unit)
    }

    fun shutdown(): OTelSdkResult {
        for (pipe in pipes) {
            val res = pipe.shutdown()
            if (res.isFailure) return res
        }
        return Result.success(Unit)
    }

    companion object {
        fun create(
            resource: Resource,
            readers: List<MetricReader>,
            views: List<View>,
        ): Pipelines {
            val pipeList =
                readers.map { reader ->
                    Pipeline(
                        resource = resource,
                        reader = reader,
                        views = views,
                    )
                }
            return Pipelines(pipeList)
        }
    }
}

/**
 * Facilitates inserting new instruments from a single scope into a pipeline.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class Inserter<T>(
    val numberOps: NumberOps<T>,
    val pipeline: Pipeline,
    val views: AtomicReference<PersistentMap<String, InstrumentId>>,
) {
    private val aggregators = AtomicReference<PersistentMap<InstrumentId, Result<Measure<T>?>>>(persistentMapOf())

    fun instrument(
        inst: Instrument,
        boundaries: List<Double>?,
    ): Result<List<Measure<T>>> {
        var matched = false
        val measures = mutableListOf<Measure<T>>()
        val seen = HashSet<InstrumentId>()

        for (v in pipeline.views) {
            var stream = v.matchInst(inst) ?: continue
            matched = true

            if (stream.name == null) stream = stream.copy(name = inst.name)
            if (stream.descriptionText == null) stream = stream.copy(descriptionText = inst.descriptionText)
            if (stream.unit == null) stream = stream.copy(unit = inst.unit)

            val id = instId(inst.kind, stream)
            if (!seen.add(id)) {
                continue
            }

            val aggRes = cachedAggregator(inst.scope, inst.kind, stream)
            if (aggRes.isFailure) {
                return Result.failure(aggRes.exceptionOrNull()!!)
            }
            val agg = aggRes.getOrNull()
            if (agg != null) {
                measures.add(agg)
            }
        }

        if (matched) {
            return Result.success(measures)
        }

        // Apply default stream
        var stream =
            Stream(
                name = inst.name,
                descriptionText = inst.descriptionText,
                unit = inst.unit,
                aggregation = null,
                allowedAttributeKeys = null,
                cardinalityLimit = null,
            )

        if (boundaries != null) {
            stream =
                stream.copy(
                    aggregation =
                        Aggregation.ExplicitBucketHistogram(
                            boundaries = boundaries,
                            recordMinMax = true,
                        ),
                )
        }

        val aggRes = cachedAggregator(inst.scope, inst.kind, stream)
        if (aggRes.isFailure) {
            return Result.failure(aggRes.exceptionOrNull()!!)
        }
        val agg = aggRes.getOrNull()
        if (agg != null) {
            measures.add(agg)
        }
        return Result.success(measures)
    }

    fun cachedAggregator(
        scope: InstrumentationScope,
        kind: InstrumentKind,
        stream: Stream,
    ): Result<Measure<T>?> {
        val agg =
            when (val a = stream.aggregation) {
                null, Aggregation.Default -> defaultAggregationSelector(kind)
                else -> a
            }

        val compatRes = isAggregatorCompatible(kind, agg)
        if (compatRes.isFailure) {
            return Result.failure(compatRes.exceptionOrNull()!!)
        }

        val id = instId(kind, stream)
        id.normalize()

        val currentMap = aggregators.load()
        val cached = currentMap[id]
        if (cached != null) {
            return cached
        }

        val filterKeys = stream.allowedAttributeKeys
        val filterFn: ((KeyValue) -> Boolean)? =
            if (filterKeys != null) {
                { kv -> filterKeys.contains(kv.key) }
            } else {
                null
            }

        val cardinalityLimit = stream.cardinalityLimit ?: DEFAULT_CARDINALITY_LIMIT
        val builder =
            AggregateBuilder(
                numberOps = numberOps,
                temporality = pipeline.reader.temporality(kind),
                filter = filterFn,
                cardinalityLimit = cardinalityLimit,
            )

        val aggFnsRes = aggregateFn(builder, agg, kind)
        if (aggFnsRes.isFailure) {
            return Result.failure(aggFnsRes.exceptionOrNull()!!)
        }
        val aggFns = aggFnsRes.getOrNull()

        if (aggFns == null) {
            // Drop aggregation
            val res = Result.success<Measure<T>?>(null)
            while (true) {
                val cur = aggregators.load()
                if (aggregators.compareAndSet(cur, cur.putting(id, res))) break
            }
            return res
        }

        pipeline.addSync(
            scope = scope,
            sync =
                InstrumentSync(
                    name = stream.name ?: "",
                    description = stream.descriptionText ?: "",
                    unit = stream.unit ?: "",
                    compAgg = aggFns.collect,
                ),
        )

        val res = Result.success<Measure<T>?>(aggFns.measure)
        while (true) {
            val cur = aggregators.load()
            if (aggregators.compareAndSet(cur, cur.putting(id, res))) break
        }
        return res
    }

    private fun instId(
        kind: InstrumentKind,
        stream: Stream,
    ): InstrumentId =
        InstrumentId(
            name = stream.name ?: "",
            description = stream.descriptionText ?: "",
            kind = kind,
            unit = stream.unit ?: "",
            number = numberOps.typeName,
        )
}

/**
 * Facilitates resolving aggregate functions for an instrument.
 */
@OptIn(ExperimentalAtomicApi::class)
internal class Resolver<T>(
    val numberOps: NumberOps<T>,
    pipelines: Pipelines,
    viewCache: AtomicReference<PersistentMap<String, InstrumentId>>,
) {
    private val inserters: List<Inserter<T>> =
        pipelines.pipes.map { pipe ->
            Inserter(numberOps, pipe, viewCache)
        }

    fun measures(
        id: Instrument,
        boundaries: List<Double>?,
    ): Result<List<Measure<T>>> {
        val measures = mutableListOf<Measure<T>>()
        for (inserter in inserters) {
            val res = inserter.instrument(id, boundaries)
            if (res.isFailure) {
                return res
            }
            measures.addAll(res.getOrThrow())
        }
        return Result.success(measures)
    }
}

internal fun defaultAggregationSelector(kind: InstrumentKind): Aggregation =
    when (kind) {
        InstrumentKind.Counter,
        InstrumentKind.UpDownCounter,
        InstrumentKind.ObservableCounter,
        InstrumentKind.ObservableUpDownCounter,
        -> Aggregation.Sum
        InstrumentKind.Gauge,
        InstrumentKind.ObservableGauge,
        -> Aggregation.LastValue
        InstrumentKind.Histogram -> Aggregation.ExplicitBucketHistogram()
    }

internal fun <T> aggregateFn(
    b: AggregateBuilder<T>,
    agg: Aggregation,
    kind: InstrumentKind,
): Result<AggregateFns<T>?> =
    when (agg) {
        is Aggregation.Drop -> Result.success(null)
        is Aggregation.Default -> {
            val defaultAgg = defaultAggregationSelector(kind)
            aggregateFn(b, defaultAgg, kind)
        }
        is Aggregation.LastValue -> {
            val overwriteTemporality =
                when (kind) {
                    InstrumentKind.ObservableGauge -> Temporality.Cumulative
                    else -> null
                }
            Result.success(b.lastValue(overwriteTemporality))
        }
        is Aggregation.Sum -> {
            val monotonic =
                kind == InstrumentKind.Counter || kind == InstrumentKind.ObservableCounter
            Result.success(b.sum(monotonic))
        }
        is Aggregation.ExplicitBucketHistogram -> {
            val recordSum =
                kind != InstrumentKind.UpDownCounter &&
                    kind != InstrumentKind.ObservableUpDownCounter &&
                    kind != InstrumentKind.ObservableGauge
            Result.success(
                b.explicitBucketHistogram(
                    boundaries = agg.boundaries,
                    recordMinMax = agg.recordMinMax,
                    recordSum = recordSum,
                ),
            )
        }
        is Aggregation.Base2ExponentialHistogram -> {
            val recordSum =
                kind != InstrumentKind.UpDownCounter &&
                    kind != InstrumentKind.ObservableUpDownCounter &&
                    kind != InstrumentKind.ObservableGauge
            Result.success(
                b.exponentialBucketHistogram(
                    maxSize = agg.maxSize,
                    maxScale = agg.maxScale,
                    recordMinMax = agg.recordMinMax,
                    recordSum = recordSum,
                ),
            )
        }
    }

internal fun isAggregatorCompatible(
    kind: InstrumentKind,
    agg: Aggregation,
): Result<Unit> =
    when (agg) {
        is Aggregation.Default, is Aggregation.Drop -> Result.success(Unit)
        is Aggregation.ExplicitBucketHistogram, is Aggregation.Base2ExponentialHistogram ->
            when (kind) {
                InstrumentKind.Counter,
                InstrumentKind.UpDownCounter,
                InstrumentKind.Gauge,
                InstrumentKind.Histogram,
                InstrumentKind.ObservableCounter,
                InstrumentKind.ObservableUpDownCounter,
                InstrumentKind.ObservableGauge,
                -> Result.success(Unit)
            }
        is Aggregation.Sum ->
            when (kind) {
                InstrumentKind.ObservableCounter,
                InstrumentKind.ObservableUpDownCounter,
                InstrumentKind.Counter,
                InstrumentKind.Histogram,
                InstrumentKind.UpDownCounter,
                -> Result.success(Unit)
                else -> Result.failure(OTelSdkError.InternalFailure("Incompatible aggregation Sum for $kind"))
            }
        is Aggregation.LastValue ->
            when (kind) {
                InstrumentKind.Gauge, InstrumentKind.ObservableGauge -> Result.success(Unit)
                else -> Result.failure(OTelSdkError.InternalFailure("Incompatible aggregation LastValue for $kind"))
            }
    }
