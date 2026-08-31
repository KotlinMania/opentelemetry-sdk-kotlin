// port-lint: source opentelemetry_sdk/src/resource/mod.rs
package io.github.kotlinmania.opentelemetrysdk.resource

/**
 * An immutable key identifying a telemetry resource attribute.
 */
public data class Key(
    public val name: String,
) {
    override fun toString(): String = name

    public companion object {
        /**
         * Creates a [Key] from a string.
         */
        public fun fromStaticStr(name: String): Key = Key(name)
    }
}

/**
 * An OpenTelemetry resource attribute value.
 */
public sealed interface Value {
    /** String value. */
    public data class StringValue(
        public val value: String,
    ) : Value {
        override fun toString(): String = value
    }

    /** Integer / Long value. */
    public data class IntValue(
        public val value: Long,
    ) : Value {
        override fun toString(): String = value.toString()
    }

    /** Floating-point / Double value. */
    public data class DoubleValue(
        public val value: Double,
    ) : Value {
        override fun toString(): String = value.toString()
    }

    /** Boolean value. */
    public data class BooleanValue(
        public val value: Boolean,
    ) : Value {
        override fun toString(): String = value.toString()
    }

    /** Array value. */
    public data class ArrayValue(
        public val value: List<Value>,
    ) : Value

    public companion object {
        /** Creates a [StringValue]. */
        public fun of(value: String): Value = StringValue(value)

        /** Creates an [IntValue]. */
        public fun of(value: Long): Value = IntValue(value)

        /** Creates an [IntValue] from Int. */
        public fun of(value: Int): Value = IntValue(value.toLong())

        /** Creates a [DoubleValue]. */
        public fun of(value: Double): Value = DoubleValue(value)

        /** Creates a [BooleanValue]. */
        public fun of(value: Boolean): Value = BooleanValue(value)
    }
}

/**
 * An OpenTelemetry key-value attribute pair.
 */
public data class KeyValue(
    public val key: Key,
    public val value: Value,
) {
    public constructor(key: String, value: String) : this(Key(key), Value.of(value))
    public constructor(key: String, value: Long) : this(Key(key), Value.of(value))
    public constructor(key: String, value: Int) : this(Key(key), Value.of(value.toLong()))
    public constructor(key: String, value: Double) : this(Key(key), Value.of(value))
    public constructor(key: String, value: Boolean) : this(Key(key), Value.of(value))
    public constructor(key: String, value: Value) : this(Key(key), value)
}

/**
 * ResourceDetector detects OpenTelemetry resource information.
 *
 * Implementations of this interface can be passed to
 * [ResourceBuilder.withDetectors] to generate a Resource from gathered information.
 */
public interface ResourceDetector {
    /**
     * Detects and returns an initialized Resource based on gathered information.
     */
    public fun detect(): Resource
}

/**
 * An immutable representation of the entity producing telemetry as attributes.
 */
public data class Resource(
    public val attrs: Map<Key, Value>,
    public val schemaUrl: String? = null,
) {
    /** Number of attributes in this resource. */
    public val size: Int get() = attrs.size

    /** Number of attributes in this resource. */
    public fun len(): Int = attrs.size

    /** Returns `true` if this resource has no attributes. */
    public fun isEmpty(): Boolean = attrs.isEmpty()

    /**
     * Gets an iterator over the key-value entries of this resource.
     */
    public fun iter(): Iterator<Pair<Key, Value>> =
        attrs.entries.map { it.key to it.value }.iterator()

    /** Returns iterator over key-value entries. */
    public operator fun iterator(): Iterator<Pair<Key, Value>> = iter()

    /** Retrieves the value associated with the given key. */
    public fun get(key: Key): Value? = attrs[key]

    /** Retrieves the value associated with the given key string. */
    public fun get(key: String): Value? = attrs[Key(key)]

    /**
     * Creates a new [Resource] by combining this resource with `other`.
     *
     * Keys from `other` take priority over keys from this resource.
     */
    public fun merge(other: Resource): Resource {
        if (this.isEmpty() && this.schemaUrl == null) {
            return other
        }
        if (other.isEmpty() && other.schemaUrl == null) {
            return this
        }
        val combinedAttrs = LinkedHashMap(this.attrs)
        combinedAttrs.putAll(other.attrs)

        val combinedSchemaUrl =
            when {
                this.schemaUrl != null && other.schemaUrl != null && this.schemaUrl == other.schemaUrl -> this.schemaUrl
                this.schemaUrl != null && other.schemaUrl != null -> null
                this.schemaUrl == null && other.schemaUrl != null -> other.schemaUrl
                this.schemaUrl != null -> this.schemaUrl
                else -> null
            }

        return Resource(
            attrs = combinedAttrs,
            schemaUrl = combinedSchemaUrl,
        )
    }

    /**
     * Gets the service name from attributes if set.
     */
    public fun serviceName(): String? =
        (get(SERVICE_NAME) as? Value.StringValue)?.value

    public companion object {
        /**
         * Creates a [ResourceBuilder] that includes default detectors.
         */
        public fun builder(): ResourceBuilder =
            ResourceBuilder(
                fromDetectors(
                    listOf(
                        SdkProvidedResourceDetector(),
                        TelemetryResourceDetector(),
                        EnvResourceDetector.new(),
                    ),
                ),
            )

        /**
         * Creates an empty [ResourceBuilder].
         */
        public fun builderEmpty(): ResourceBuilder = ResourceBuilder(empty())

        /**
         * Creates an empty resource with no attributes and no schema URL.
         */
        public fun empty(): Resource = Resource(attrs = emptyMap(), schemaUrl = null)

        /**
         * Creates a new [Resource] from key-value pairs.
         */
        public fun new(kvs: Iterable<KeyValue>): Resource {
            val map = LinkedHashMap<Key, Value>()
            for (kv in kvs) {
                map[kv.key] = kv.value
            }
            return Resource(attrs = map, schemaUrl = null)
        }

        /**
         * Creates a new [Resource] from key-value pairs and a schema URL.
         */
        public fun fromSchemaUrl(kvs: Iterable<KeyValue>, schemaUrl: String?): Resource {
            val normalizedSchemaUrl = if (schemaUrl.isNullOrEmpty()) null else schemaUrl
            val map = LinkedHashMap<Key, Value>()
            for (kv in kvs) {
                map[kv.key] = kv.value
            }
            return Resource(attrs = map, schemaUrl = normalizedSchemaUrl)
        }

        /**
         * Creates a new [Resource] by running the given detectors in order.
         */
        public fun fromDetectors(detectors: List<ResourceDetector>): Resource {
            var resource = empty()
            for (detector in detectors) {
                val detected = detector.detect()
                val map = LinkedHashMap(resource.attrs)
                map.putAll(detected.attrs)
                resource = Resource(attrs = map, schemaUrl = resource.schemaUrl ?: detected.schemaUrl)
            }
            return resource
        }
    }
}

/**
 * Builder for constructing a [Resource].
 */
public class ResourceBuilder internal constructor(
    private var resource: Resource,
) {
    /**
     * Adds a single [ResourceDetector] to the resource.
     */
    public fun withDetector(detector: ResourceDetector): ResourceBuilder =
        withDetectors(listOf(detector))

    /**
     * Adds multiple [ResourceDetector]s to the resource.
     */
    public fun withDetectors(detectors: List<ResourceDetector>): ResourceBuilder {
        resource = resource.merge(Resource.fromDetectors(detectors))
        return this
    }

    /**
     * Adds a single [KeyValue] attribute to the resource.
     */
    public fun withAttribute(kv: KeyValue): ResourceBuilder =
        withAttributes(listOf(kv))

    /**
     * Adds multiple [KeyValue] attributes to the resource.
     */
    public fun withAttributes(kvs: Iterable<KeyValue>): ResourceBuilder {
        resource = resource.merge(Resource.new(kvs))
        return this
    }

    /**
     * Sets the service name attribute.
     */
    public fun withServiceName(name: Value): ResourceBuilder =
        withAttribute(KeyValue(Key(SERVICE_NAME), name))

    /**
     * Sets the service name attribute from a string.
     */
    public fun withServiceName(name: String): ResourceBuilder =
        withServiceName(Value.of(name))

    /**
     * Merges attributes with the specified schema URL.
     */
    public fun withSchemaUrl(attributes: Iterable<KeyValue>, schemaUrl: String): ResourceBuilder {
        resource = Resource.fromSchemaUrl(attributes, schemaUrl).merge(resource)
        return this
    }

    /**
     * Builds and returns the [Resource].
     */
    public fun build(): Resource = resource
}
