// port-lint: source lib.rs
package io.github.kotlinmania.opentelemetrysdk

import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue

/**
 * An instrumentation scope, identifying the library that produced telemetry.
 */
public data class InstrumentationScope(
    public val name: String,
    public val version: String? = null,
    public val schemaUrl: String? = null,
    public val attributes: List<KeyValue> = emptyList(),
) {
    /**
     * Builder for [InstrumentationScope].
     */
    public class Builder(
        private val name: String,
    ) {
        private var version: String? = null
        private var schemaUrl: String? = null
        private var attributes: List<KeyValue> = emptyList()

        public fun withVersion(version: String): Builder {
            this.version = version
            return this
        }

        public fun withSchemaUrl(schemaUrl: String): Builder {
            this.schemaUrl = schemaUrl
            return this
        }

        public fun withAttributes(attributes: Iterable<KeyValue>): Builder {
            this.attributes = attributes.distinct().sortedWith(compareBy({ it.key.name }, { it.value.toString() }))
            return this
        }

        public fun build(): InstrumentationScope =
            InstrumentationScope(
                name = name,
                version = version,
                schemaUrl = schemaUrl,
                attributes = attributes,
            )
    }

    public companion object {
        public fun builder(name: String): Builder = Builder(name)

        public val EMPTY: InstrumentationScope = InstrumentationScope("")
    }
}
