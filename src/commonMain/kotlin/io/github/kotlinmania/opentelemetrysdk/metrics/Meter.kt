// port-lint: source opentelemetry_sdk/src/metrics/meter.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.DoubleOps
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.LongOps
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.Measure
import io.github.kotlinmania.opentelemetrysdk.metrics.internal.ULongOps
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import kotlinx.collections.immutable.PersistentMap
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal fun validateInstrumentConfig(
    name: String,
    unit: String,
): Result<Unit> {
    if (name.isEmpty()) {
        return Result.failure(OTelSdkError.InternalFailure(INSTRUMENT_NAME_EMPTY))
    }
    if (name.length > INSTRUMENT_NAME_MAX_LENGTH) {
        return Result.failure(OTelSdkError.InternalFailure(INSTRUMENT_NAME_LENGTH))
    }
    val firstChar = name.first()
    if (!((firstChar in 'a'..'z') || (firstChar in 'A'..'Z'))) {
        return Result.failure(OTelSdkError.InternalFailure(INSTRUMENT_NAME_FIRST_ALPHABETIC))
    }
    val allowedChars = setOf('_', '.', '-', '/')
    for (c in name) {
        val isAlphaNum = (c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9')
        if (!isAlphaNum && c !in allowedChars) {
            return Result.failure(OTelSdkError.InternalFailure(INSTRUMENT_NAME_INVALID_CHAR))
        }
    }
    if (unit.length > INSTRUMENT_UNIT_NAME_MAX_LENGTH) {
        return Result.failure(OTelSdkError.InternalFailure(INSTRUMENT_UNIT_LENGTH))
    }
    for (c in unit) {
        if (c.code > 127) {
            return Result.failure(OTelSdkError.InternalFailure(INSTRUMENT_UNIT_INVALID_CHAR))
        }
    }
    return Result.success(Unit)
}

// generic by design: callers choose the instrument numeric type.
public class Counter<T> internal constructor(
    private val measures: List<Measure<T>>,
) {
    public fun add(
        value: T,
        attributes: List<KeyValue> = emptyList(),
    ) {
        for (m in measures) {
            m.call(value, attributes)
        }
    }
}

// generic by design: callers choose the instrument numeric type.
public class UpDownCounter<T> internal constructor(
    private val measures: List<Measure<T>>,
) {
    public fun add(
        value: T,
        attributes: List<KeyValue> = emptyList(),
    ) {
        for (m in measures) {
            m.call(value, attributes)
        }
    }
}

// generic by design: callers choose the instrument numeric type.
public class Gauge<T> internal constructor(
    private val measures: List<Measure<T>>,
) {
    public fun record(
        value: T,
        attributes: List<KeyValue> = emptyList(),
    ) {
        for (m in measures) {
            m.call(value, attributes)
        }
    }
}

// generic by design: callers choose the instrument numeric type.
public class Histogram<T> internal constructor(
    private val measures: List<Measure<T>>,
) {
    public fun record(
        value: T,
        attributes: List<KeyValue> = emptyList(),
    ) {
        for (m in measures) {
            m.call(value, attributes)
        }
    }
}

// generic by design: callers choose the instrument numeric type.
public fun interface Observer<T> {
    public fun observe(
        value: T,
        attributes: List<KeyValue>,
    )
}

public fun interface MetricCallback {
    public operator fun invoke()
}

/**
 * Handles the creation and coordination of all metric instruments for a scope.
 */
@OptIn(ExperimentalAtomicApi::class)
public class SdkMeter internal constructor(
    public val scope: InstrumentationScope,
    internal val pipelines: Pipelines,
    viewCache: AtomicReference<PersistentMap<String, InstrumentId>>,
) {
    private val u64Resolver = Resolver(ULongOps, pipelines, viewCache)
    private val i64Resolver = Resolver(LongOps, pipelines, viewCache)
    private val f64Resolver = Resolver(DoubleOps, pipelines, viewCache)

    public fun counter(
        name: String,
        description: String = "",
        unit: String = "",
    ): Counter<Long> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return Counter(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.Counter,
                unit = unit,
                scope = scope,
            )
        val measures = i64Resolver.measures(inst, null).getOrDefault(emptyList())
        return Counter(measures)
    }

    public fun counterU64(
        name: String,
        description: String = "",
        unit: String = "",
    ): Counter<ULong> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return Counter(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.Counter,
                unit = unit,
                scope = scope,
            )
        val measures = u64Resolver.measures(inst, null).getOrDefault(emptyList())
        return Counter(measures)
    }

    public fun counterF64(
        name: String,
        description: String = "",
        unit: String = "",
    ): Counter<Double> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return Counter(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.Counter,
                unit = unit,
                scope = scope,
            )
        val measures = f64Resolver.measures(inst, null).getOrDefault(emptyList())
        return Counter(measures)
    }

    public fun upDownCounter(
        name: String,
        description: String = "",
        unit: String = "",
    ): UpDownCounter<Long> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return UpDownCounter(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.UpDownCounter,
                unit = unit,
                scope = scope,
            )
        val measures = i64Resolver.measures(inst, null).getOrDefault(emptyList())
        return UpDownCounter(measures)
    }

    public fun upDownCounterF64(
        name: String,
        description: String = "",
        unit: String = "",
    ): UpDownCounter<Double> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return UpDownCounter(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.UpDownCounter,
                unit = unit,
                scope = scope,
            )
        val measures = f64Resolver.measures(inst, null).getOrDefault(emptyList())
        return UpDownCounter(measures)
    }

    public fun gauge(
        name: String,
        description: String = "",
        unit: String = "",
    ): Gauge<Double> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return Gauge(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.Gauge,
                unit = unit,
                scope = scope,
            )
        val measures = f64Resolver.measures(inst, null).getOrDefault(emptyList())
        return Gauge(measures)
    }

    public fun gaugeI64(
        name: String,
        description: String = "",
        unit: String = "",
    ): Gauge<Long> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return Gauge(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.Gauge,
                unit = unit,
                scope = scope,
            )
        val measures = i64Resolver.measures(inst, null).getOrDefault(emptyList())
        return Gauge(measures)
    }

    public fun histogram(
        name: String,
        description: String = "",
        unit: String = "",
        boundaries: List<Double>? = null,
    ): Histogram<Double> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return Histogram(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.Histogram,
                unit = unit,
                scope = scope,
            )
        val measures = f64Resolver.measures(inst, boundaries).getOrDefault(emptyList())
        return Histogram(measures)
    }

    public fun histogramI64(
        name: String,
        description: String = "",
        unit: String = "",
        boundaries: List<Double>? = null,
    ): Histogram<Long> {
        val validation = validateInstrumentConfig(name, unit)
        if (validation.isFailure) {
            return Histogram(emptyList())
        }
        val inst =
            Instrument(
                name = name,
                descriptionText = description,
                kind = InstrumentKind.Histogram,
                unit = unit,
                scope = scope,
            )
        val measures = i64Resolver.measures(inst, boundaries).getOrDefault(emptyList())
        return Histogram(measures)
    }

    public fun registerCallback(
        instruments: List<Instrument>,
        callback: MetricCallback,
    ) {
        pipelines.registerCallback { callback() }
    }
}
