// port-lint: source opentelemetry_sdk/src/resource/env.rs
package io.github.kotlinmania.opentelemetrysdk.resource

/** Environment variable for setting resource attributes. */
public const val OTEL_RESOURCE_ATTRIBUTES: String = "OTEL_RESOURCE_ATTRIBUTES"

/** Environment variable for setting the service name. */
public const val OTEL_SERVICE_NAME: String = "OTEL_SERVICE_NAME"

/**
 * Extracts key value pairs and constructs a resource from a resource string like `key1=value1,key2=value2,...`.
 */
internal fun constructOtelResources(s: String): Resource {
    val kvs = mutableListOf<KeyValue>()
    for (entry in s.split(",")) {
        val trimmed = entry.trim()
        if (trimmed.isEmpty()) continue
        val parts = trimmed.split("=", limit = 2)
        if (parts.size == 2) {
            val key = parts[0].trim()
            val value = parts[1].trim()
            if (key.isNotEmpty()) {
                kvs.add(KeyValue(Key(key), Value.of(value)))
            }
        }
    }
    return Resource
        .builderEmpty()
        .withAttributes(kvs)
        .build()
}

/**
 * Extracts resource attributes from the environment variable `OTEL_RESOURCE_ATTRIBUTES`.
 */
public class EnvResourceDetector(
    private val getEnv: (String) -> String? = { null },
) : ResourceDetector {
    override fun detect(): Resource {
        val value = getEnv(OTEL_RESOURCE_ATTRIBUTES)
        return if (!value.isNullOrEmpty()) {
            constructOtelResources(value)
        } else {
            Resource.empty()
        }
    }

    public companion object {
        /** Creates a new [EnvResourceDetector] instance. */
        public fun new(): EnvResourceDetector = EnvResourceDetector()

        /** Returns the default [EnvResourceDetector] instance. */
        public fun default(): EnvResourceDetector = new()
    }
}

/**
 * Detects SDK-provided resource attributes such as `service.name`.
 */
public class SdkProvidedResourceDetector(
    private val getEnv: (String) -> String? = { null },
) : ResourceDetector {
    override fun detect(): Resource {
        val serviceName =
            getEnv(OTEL_SERVICE_NAME)?.takeIf { it.isNotEmpty() }
                ?: EnvResourceDetector(getEnv).detect().get(Key(SERVICE_NAME))?.toString()
                ?: "unknown_service"

        return Resource
            .builderEmpty()
            .withAttributes(listOf(KeyValue(Key(SERVICE_NAME), Value.of(serviceName))))
            .build()
    }

    public companion object {
        /** Creates a new [SdkProvidedResourceDetector] instance. */
        public fun new(): SdkProvidedResourceDetector = SdkProvidedResourceDetector()

        /** Returns the default [SdkProvidedResourceDetector] instance. */
        public fun default(): SdkProvidedResourceDetector = new()
    }
}
