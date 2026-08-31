// port-lint: source metrics/instrument.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.resource.Key

public const val INSTRUMENT_NAME_MAX_LENGTH: Int = 255
public const val INSTRUMENT_UNIT_NAME_MAX_LENGTH: Int = 63

public const val INSTRUMENT_NAME_EMPTY: String = "name must be non-empty"
public const val INSTRUMENT_NAME_LENGTH: String = "name must be less than 256 characters"
public const val INSTRUMENT_NAME_INVALID_CHAR: String =
    "characters in name must be ASCII and belong to the alphanumeric characters, '_', '.', '-' and '/'"
public const val INSTRUMENT_NAME_FIRST_ALPHABETIC: String =
    "name must start with an alphabetic character"

public const val INSTRUMENT_UNIT_LENGTH: String = "unit must be less than 64 characters"
public const val INSTRUMENT_UNIT_INVALID_CHAR: String = "characters in unit must be ASCII"

private val ALLOWED_NON_ALPHANUMERIC_CHARS = setOf('_', '.', '-', '/')

/**
 * The identifier of a group of instruments that all perform the same function.
 */
public enum class InstrumentKind {
    /**
     * Identifies a group of instruments that record increasing values synchronously
     * with the code path they are measuring.
     */
    Counter,

    /**
     * A group of instruments that record increasing and decreasing values
     * synchronously with the code path they are measuring.
     */
    UpDownCounter,

    /**
     * A group of instruments that record a distribution of values synchronously with
     * the code path they are measuring.
     */
    Histogram,

    /**
     * A group of instruments that record increasing values in an asynchronous callback.
     */
    ObservableCounter,

    /**
     * A group of instruments that record increasing and decreasing values in an
     * asynchronous callback.
     */
    ObservableUpDownCounter,

    /**
     * A group of instruments that record current value synchronously with
     * the code path they are measuring.
     */
    Gauge,

    /**
     * A group of instruments that record current values in an asynchronous callback.
     */
    ObservableGauge,

    ;

    /**
     * Select the [Temporality] preference based on [InstrumentKind].
     */
    public fun temporalityPreference(temporality: Temporality): Temporality =
        when (temporality) {
            Temporality.Cumulative -> Temporality.Cumulative
            Temporality.Delta ->
                when (this) {
                    Counter, Histogram, ObservableCounter, Gauge, ObservableGauge -> Temporality.Delta
                    UpDownCounter, ObservableUpDownCounter -> Temporality.Cumulative
                }
            Temporality.LowMemory ->
                when (this) {
                    Counter, Histogram -> Temporality.Delta
                    ObservableCounter, Gauge, ObservableGauge, UpDownCounter, ObservableUpDownCounter ->
                        Temporality.Cumulative
                }
        }
}

/**
 * Describes the properties of an instrument at creation, used for filtering in views.
 */
public data class Instrument(
    public val name: String,
    public val descriptionText: String = "",
    public val kind: InstrumentKind,
    public val unit: String = "",
    public val scope: InstrumentationScope = InstrumentationScope.EMPTY,
) {
    public fun description(): String = descriptionText
}

/**
 * Describes the stream of data an instrument produces.
 */
public data class Stream(
    public val name: String? = null,
    public val descriptionText: String? = null,
    public val unit: String? = null,
    public val aggregation: Aggregation? = null,
    public val allowedAttributeKeys: Set<Key>? = null,
    public val cardinalityLimit: Int? = null,
) {
    public fun description(): String? = descriptionText

    public companion object {
        public fun builder(): StreamBuilder = StreamBuilder()
    }
}

/**
 * A builder for creating [Stream] objects.
 */
public class StreamBuilder {
    private var name: String? = null
    private var description: String? = null
    private var unit: String? = null
    private var aggregation: Aggregation? = null
    private var allowedAttributeKeys: Set<Key>? = null
    private var cardinalityLimit: Int? = null

    public fun withName(name: String): StreamBuilder {
        this.name = name
        return this
    }

    public fun withDescription(description: String): StreamBuilder {
        this.description = description
        return this
    }

    public fun withUnit(unit: String): StreamBuilder {
        this.unit = unit
        return this
    }

    public fun withAggregation(aggregation: Aggregation): StreamBuilder {
        this.aggregation = aggregation
        return this
    }

    public fun withAllowedAttributeKeys(attributeKeys: Iterable<Key>): StreamBuilder {
        this.allowedAttributeKeys = attributeKeys.toSet()
        return this
    }

    public fun withCardinalityLimit(limit: Int): StreamBuilder {
        this.cardinalityLimit = limit
        return this
    }

    public fun build(): Result<Stream> {
        val streamName = name
        if (streamName != null) {
            if (streamName.isEmpty()) {
                return Result.failure(IllegalArgumentException(INSTRUMENT_NAME_EMPTY))
            }
            if (streamName.length > INSTRUMENT_NAME_MAX_LENGTH) {
                return Result.failure(IllegalArgumentException(INSTRUMENT_NAME_LENGTH))
            }
            val firstChar = streamName.first()
            if (!((firstChar in 'a'..'z') || (firstChar in 'A'..'Z'))) {
                return Result.failure(IllegalArgumentException(INSTRUMENT_NAME_FIRST_ALPHABETIC))
            }
            for (c in streamName) {
                val isAlphaNum = (c in 'a'..'z') || (c in 'A'..'Z') || (c in '0'..'9')
                if (!isAlphaNum && c !in ALLOWED_NON_ALPHANUMERIC_CHARS) {
                    return Result.failure(IllegalArgumentException(INSTRUMENT_NAME_INVALID_CHAR))
                }
            }
        }

        val streamUnit = unit
        if (streamUnit != null) {
            if (streamUnit.length > INSTRUMENT_UNIT_NAME_MAX_LENGTH) {
                return Result.failure(IllegalArgumentException(INSTRUMENT_UNIT_LENGTH))
            }
            for (c in streamUnit) {
                if (c.code > 127) {
                    return Result.failure(IllegalArgumentException(INSTRUMENT_UNIT_INVALID_CHAR))
                }
            }
        }

        val limit = cardinalityLimit
        if (limit != null && limit <= 0) {
            return Result.failure(IllegalArgumentException("Cardinality limit must be greater than 0"))
        }

        val agg = aggregation
        if (agg is Aggregation.ExplicitBucketHistogram) {
            for (boundary in agg.boundaries) {
                if (boundary.isNaN() || boundary.isInfinite()) {
                    return Result.failure(
                        IllegalArgumentException("Bucket boundaries must not contain NaN, Infinity, or -Infinity"),
                    )
                }
            }
            for (i in 1 until agg.boundaries.size) {
                if (agg.boundaries[i] <= agg.boundaries[i - 1]) {
                    return Result.failure(
                        IllegalArgumentException("Bucket boundaries must be sorted and not contain any duplicates"),
                    )
                }
            }
        }

        return Result.success(
            Stream(
                name = name,
                descriptionText = description,
                unit = unit,
                aggregation = aggregation,
                allowedAttributeKeys = allowedAttributeKeys,
                cardinalityLimit = cardinalityLimit,
            ),
        )
    }
}
