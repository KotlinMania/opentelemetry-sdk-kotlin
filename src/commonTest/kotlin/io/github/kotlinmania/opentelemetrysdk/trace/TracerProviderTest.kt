// port-lint: tests trace/provider.rs
package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.OpenTelemetrySdk
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import io.github.kotlinmania.opentelemetrysdk.resource.SERVICE_NAME
import io.github.kotlinmania.opentelemetrysdk.resource.TELEMETRY_SDK_LANGUAGE
import io.github.kotlinmania.opentelemetrysdk.resource.TELEMETRY_SDK_NAME
import io.github.kotlinmania.opentelemetrysdk.resource.TELEMETRY_SDK_VERSION
import io.github.kotlinmania.opentelemetrysdk.resource.Value
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration

@OptIn(ExperimentalAtomicApi::class)
class TracerProviderTest {
    private class AssertInfo {
        val startedSpan = AtomicInt(0)
        val isShutdown = AtomicBoolean(false)
    }

    private class SharedAssertInfo(
        val info: AssertInfo = AssertInfo(),
    ) {
        fun startedSpanCount(count: Int): Boolean = info.startedSpan.load() == count
    }

    private class TestSpanProcessor(
        val success: Boolean,
        val assertInfo: SharedAssertInfo = SharedAssertInfo(),
    ) : SpanProcessor {
        fun assertInfo(): SharedAssertInfo = assertInfo

        override fun onStart(span: Span, parentContext: SpanContext?) {
            assertInfo.info.startedSpan.addAndFetch(1)
        }

        override fun onEnd(span: SpanData) {
            // ignore
        }

        override fun forceFlush(): OTelSdkResult =
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(OTelSdkError.InternalFailure("cannot export"))
            }

        override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult =
            if (assertInfo.info.isShutdown.load()) {
                Result.success(Unit)
            } else {
                assertInfo.info.isShutdown.compareAndSet(false, true)
                forceFlush()
            }
    }

    private class CountingShutdownProcessor(
        val shutdownCount: AtomicInt,
    ) : SpanProcessor {
        override fun onStart(span: Span, parentContext: SpanContext?) {}

        override fun onEnd(span: SpanData) {}

        override fun forceFlush(): OTelSdkResult = Result.success(Unit)

        override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
            shutdownCount.addAndFetch(1)
            return Result.success(Unit)
        }
    }

    @Test
    fun testForceFlush() {
        val tracerProvider =
            SdkTracerProvider
                .builder()
                .withSpanProcessor(TestSpanProcessor(true))
                .withSpanProcessor(TestSpanProcessor(false))
                .build()

        val results = tracerProvider.forceFlush()
        assertTrue(results.isFailure)
    }

    @Test
    fun testTracerProviderDefaultResource() {
        val defaultProvider = SdkTracerProvider.builder().build()
        val resource = defaultProvider.config().resource
        assertEquals(Value.of("unknown_service"), resource.get(Key(SERVICE_NAME)))
        assertEquals(Value.of("kotlin"), resource.get(Key(TELEMETRY_SDK_LANGUAGE)))
        assertEquals(Value.of("opentelemetry"), resource.get(Key(TELEMETRY_SDK_NAME)))
        assertEquals(Value.of(OpenTelemetrySdk.SPECIFICATION_VERSION), resource.get(Key(TELEMETRY_SDK_VERSION)))

        val customProvider =
            SdkTracerProvider
                .builder()
                .withResource(
                    Resource
                        .builderEmpty()
                        .withServiceName("test_service")
                        .build(),
                ).build()
        assertEquals(Value.of("test_service"), customProvider.config().resource.get(Key(SERVICE_NAME)))
        assertEquals(1, customProvider.config().resource.len())

        val noServiceName =
            SdkTracerProvider
                .builder()
                .withResource(Resource.empty())
                .build()
        assertEquals(0, noServiceName.config().resource.len())
    }

    @Test
    fun testShutdownNoops() {
        val processor = TestSpanProcessor(false)
        val assertHandle = processor.assertInfo()
        val tracerProvider =
            SdkTracerProvider
                .builder()
                .withSpanProcessor(processor)
                .build()

        val testTracer1 = tracerProvider.tracer("test1")
        testTracer1.start("test")
        assertTrue(assertHandle.startedSpanCount(1))

        testTracer1.start("test")
        assertTrue(assertHandle.startedSpanCount(2))

        val shutdownResult = tracerProvider.shutdown()
        assertTrue(shutdownResult.isFailure)

        val noopTracer = tracerProvider.tracer("noop")
        noopTracer.start("test")
        assertTrue(assertHandle.startedSpanCount(2))
        assertTrue(tracerProvider.isShutdown())

        testTracer1.start("test")
        assertTrue(assertHandle.startedSpanCount(2))
        assertTrue(testTracer1.provider().isShutdown())
    }

    @Test
    fun withResourceMultipleCallsEnsureAdditive() {
        val resource =
            SdkTracerProvider
                .builder()
                .withResource(Resource.new(listOf(KeyValue(Key("key1"), Value.of("value1")))))
                .withResource(Resource.new(listOf(KeyValue(Key("key2"), Value.of("value2")))))
                .withResource(
                    Resource
                        .builderEmpty()
                        .withSchemaUrl(emptyList(), "http://example.com")
                        .build(),
                ).withResource(Resource.new(listOf(KeyValue(Key("key3"), Value.of("value3")))))
                .build()
                .config()
                .resource

        assertEquals(Value.of("value1"), resource.get(Key("key1")))
        assertEquals(Value.of("value2"), resource.get(Key("key2")))
        assertEquals(Value.of("value3"), resource.get(Key("key3")))
        assertEquals("http://example.com", resource.schemaUrl)
    }

    @Test
    fun dropTestWithMultipleProviders() {
        val shutdownCount = AtomicInt(0)
        val processor = CountingShutdownProcessor(shutdownCount)
        val provider1 = SdkTracerProvider.builder().withSpanProcessor(processor).build()
        val tracer1 = provider1.tracer("test-tracer1")
        tracer1.start("span1")
        assertEquals(0, shutdownCount.load())
        provider1.shutdown()
        assertEquals(1, shutdownCount.load())
    }

    @Test
    fun dropAfterShutdownTestWithMultipleProviders() {
        val shutdownCount = AtomicInt(0)
        val processor = CountingShutdownProcessor(shutdownCount)
        val provider1 = SdkTracerProvider.builder().withSpanProcessor(processor).build()
        val shutdownResult = provider1.shutdown()
        assertTrue(shutdownResult.isSuccess)
        assertEquals(1, shutdownCount.load())
    }
}
