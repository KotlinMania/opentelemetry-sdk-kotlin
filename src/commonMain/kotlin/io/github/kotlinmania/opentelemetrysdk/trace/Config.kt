// port-lint: source trace/config.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.resource.Resource

/**
 * Global tracing configuration.
 */
public data class Config(
    public val sampler: ShouldSample = Sampler.ParentBased(Sampler.AlwaysOn),
    public val idGenerator: IdGenerator = RandomIdGenerator.DEFAULT,
    public val spanLimits: SpanLimits = SpanLimits.defaultLimits(),
    public val resource: Resource = Resource.builder().build(),
) {
    public companion object {
        public const val OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT: String = "OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT"
        public const val OTEL_SPAN_EVENT_COUNT_LIMIT: String = "OTEL_SPAN_EVENT_COUNT_LIMIT"
        public const val OTEL_SPAN_LINK_COUNT_LIMIT: String = "OTEL_SPAN_LINK_COUNT_LIMIT"
        public const val OTEL_TRACES_SAMPLER: String = "OTEL_TRACES_SAMPLER"
        public const val OTEL_TRACES_SAMPLER_ARG: String = "OTEL_TRACES_SAMPLER_ARG"

        public fun defaultConfig(getEnv: (String) -> String? = { null }): Config {
            var spanLimits = SpanLimits.defaultLimits()
            val maxAttr = getEnv(OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT)?.toUIntOrNull()
            if (maxAttr != null) {
                spanLimits = spanLimits.copy(maxAttributesPerSpan = maxAttr)
            }
            val maxEvents = getEnv(OTEL_SPAN_EVENT_COUNT_LIMIT)?.toUIntOrNull()
            if (maxEvents != null) {
                spanLimits = spanLimits.copy(maxEventsPerSpan = maxEvents)
            }
            val maxLinks = getEnv(OTEL_SPAN_LINK_COUNT_LIMIT)?.toUIntOrNull()
            if (maxLinks != null) {
                spanLimits = spanLimits.copy(maxLinksPerSpan = maxLinks)
            }

            val samplerArg = getEnv(OTEL_TRACES_SAMPLER_ARG)
            val sampler: ShouldSample =
                when (getEnv(OTEL_TRACES_SAMPLER)) {
                    "always_on" -> Sampler.AlwaysOn
                    "always_off" -> Sampler.AlwaysOff
                    "traceidratio" -> {
                        val ratio = samplerArg?.toDoubleOrNull() ?: 1.0
                        Sampler.TraceIdRatioBased(ratio)
                    }
                    "parentbased_always_on" -> Sampler.ParentBased(Sampler.AlwaysOn)
                    "parentbased_always_off" -> Sampler.ParentBased(Sampler.AlwaysOff)
                    "parentbased_traceidratio" -> {
                        val ratio = samplerArg?.toDoubleOrNull() ?: 1.0
                        Sampler.ParentBased(Sampler.TraceIdRatioBased(ratio))
                    }
                    else -> Sampler.ParentBased(Sampler.AlwaysOn)
                }

            return Config(
                sampler = sampler,
                idGenerator = RandomIdGenerator.DEFAULT,
                spanLimits = spanLimits,
                resource = Resource.builder().build(),
            )
        }
    }
}
