// port-lint: tests resource/mod.rs
package io.github.kotlinmania.opentelemetrysdk.resource

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResourceTest {
    @Test
    fun newResource() {
        val resource =
            Resource
                .builderEmpty()
                .withAttributes(listOf(KeyValue("a", ""), KeyValue("a", "final")))
                .build()

        assertEquals(mapOf(Key("a") to Value.of("final")), resource.attrs)
        assertNull(resource.schemaUrl)

        val resource2 =
            Resource
                .builderEmpty()
                .withAttributes(listOf(KeyValue("a", "final"), KeyValue("a", "")))
                .build()

        assertEquals(mapOf(Key("a") to Value.of("")), resource2.attrs)
        assertNull(resource2.schemaUrl)
    }

    @Test
    fun mergeResourceKeyValuePairs() {
        val resourceA =
            Resource
                .builderEmpty()
                .withAttributes(
                    listOf(
                        KeyValue("a", ""),
                        KeyValue("b", "b-value"),
                        KeyValue("d", "d-value"),
                    ),
                ).build()

        val resourceB =
            Resource
                .builderEmpty()
                .withAttributes(
                    listOf(
                        KeyValue("a", "a-value"),
                        KeyValue("c", "c-value"),
                        KeyValue("d", ""),
                    ),
                ).build()

        val expectedAttrs =
            mapOf(
                Key("a") to Value.of("a-value"),
                Key("b") to Value.of("b-value"),
                Key("c") to Value.of("c-value"),
                Key("d") to Value.of(""),
            )

        val expectedResource = Resource(attrs = expectedAttrs, schemaUrl = null)
        assertEquals(expectedResource, resourceA.merge(resourceB))
    }

    @Test
    fun mergeResourceSchemaUrl() {
        val cases =
            listOf(
                Triple("http://schema/a", null, "http://schema/a"),
                Triple("http://schema/a", "http://schema/b", null),
                Triple(null, "http://schema/b", "http://schema/b"),
                Triple("http://schema/a", "http://schema/a", "http://schema/a"),
                Triple(null, null, null),
            )

        for ((schemaUrlA, schemaUrlB, expectedSchemaUrl) in cases) {
            val resourceA = Resource.fromSchemaUrl(listOf(KeyValue("key", "")), schemaUrlA)
            val resourceB = Resource.fromSchemaUrl(listOf(KeyValue("key", "")), schemaUrlB)

            val merged = resourceA.merge(resourceB)
            assertEquals(
                expectedSchemaUrl,
                merged.schemaUrl,
                "Merging schemaUrlA $schemaUrlA with schemaUrlB $schemaUrlB failed",
            )
        }
    }

    @Test
    fun mergeResourceWithMissingAttributes() {
        val cases =
            listOf(
                Triple(
                    listOf<KeyValue>(),
                    listOf(KeyValue("key", "b")),
                    Triple("http://schema/a", null, "http://schema/a"),
                ),
                Triple(
                    listOf(KeyValue("key", "a")),
                    listOf(KeyValue("key", "b")),
                    Triple("http://schema/a", null, "http://schema/a"),
                ),
                Triple(
                    listOf(KeyValue("key", "a")),
                    listOf(KeyValue("key", "b")),
                    Triple("http://schema/a", "http://schema/b", null),
                ),
                Triple(
                    listOf(KeyValue("key", "a")),
                    listOf(KeyValue("key", "b")),
                    Triple(null, "http://schema/b", "http://schema/b"),
                ),
            )

        for ((kvsA, kvsB, schemas) in cases) {
            val (schemaA, schemaB, expectedSchema) = schemas
            val resourceA =
                if (schemaA != null) {
                    Resource.fromSchemaUrl(kvsA, schemaA)
                } else {
                    Resource.new(kvsA)
                }

            val resourceB =
                if (schemaB != null) {
                    Resource.builderEmpty().withSchemaUrl(kvsB, schemaB).build()
                } else {
                    Resource.new(kvsB)
                }

            assertEquals(expectedSchema, resourceA.merge(resourceB).schemaUrl)
        }
    }

    @Test
    fun detectResource() {
        val env =
            mapOf(
                "OTEL_RESOURCE_ATTRIBUTES" to "key=value, k = v , a= x, a=z",
                "IRRELEVANT" to "20200810",
            )
        val detector = EnvResourceDetector(getEnv = { env[it] })
        val resource = Resource.fromDetectors(listOf(detector))
        val expected =
            Resource
                .builderEmpty()
                .withAttributes(
                    listOf(
                        KeyValue("key", "value"),
                        KeyValue("k", "v"),
                        KeyValue("a", "x"),
                        KeyValue("a", "z"),
                    ),
                ).build()
        assertEquals(expected, resource)
    }

    @Test
    fun builderWithSchemaUrl() {
        val cases =
            listOf(
                Triple("http://schema/a", "http://schema/b", null),
                Triple(null, "http://schema/b", "http://schema/b"),
                Triple("http://schema/a", "http://schema/a", "http://schema/a"),
            )

        for ((schemaUrlA, schemaUrlB, expectedSchemaUrl) in cases) {
            val baseBuilder =
                if (schemaUrlA != null) {
                    Resource.fromSchemaUrl(listOf(KeyValue("key", "")), schemaUrlA).let {
                        ResourceBuilder(it)
                    }
                } else {
                    Resource.builderEmpty()
                }

            val resource =
                baseBuilder
                    .withSchemaUrl(listOf(KeyValue("key", "")), schemaUrlB)
                    .build()

            assertEquals(
                expectedSchemaUrl,
                resource.schemaUrl,
                "Merging schemaUrlA $schemaUrlA with schemaUrlB $schemaUrlB failed",
            )
        }
    }

    @Test
    fun builderDetectResource() {
        val env =
            mapOf(
                "OTEL_RESOURCE_ATTRIBUTES" to "key=value, k = v , a= x, a=z",
                "IRRELEVANT" to "20200810",
            )
        val resource =
            Resource
                .builderEmpty()
                .withDetector(EnvResourceDetector(getEnv = { env[it] }))
                .withServiceName("testing_service")
                .withAttribute(KeyValue("test1", "test_value"))
                .withAttributes(
                    listOf(
                        KeyValue("test1", "test_value1"),
                        KeyValue("test2", "test_value2"),
                    ),
                ).build()

        val expected =
            Resource
                .builderEmpty()
                .withAttributes(
                    listOf(
                        KeyValue("key", "value"),
                        KeyValue("test1", "test_value1"),
                        KeyValue("test2", "test_value2"),
                        KeyValue(SERVICE_NAME, "testing_service"),
                        KeyValue("k", "v"),
                        KeyValue("a", "x"),
                        KeyValue("a", "z"),
                    ),
                ).build()

        assertEquals(expected, resource)
    }
}
