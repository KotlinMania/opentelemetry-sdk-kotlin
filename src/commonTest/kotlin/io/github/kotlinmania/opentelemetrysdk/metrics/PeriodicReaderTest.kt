// port-lint: tests opentelemetry_sdk/src/metrics/periodic_reader.rs
package io.github.kotlinmania.opentelemetrysdk.metrics

import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.metrics.data.ResourceMetrics
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
class PeriodicReaderTest {
    private class MetricExporterThatFailsOnlyOnFirst(
        val count: AtomicInt = AtomicInt(0),
    ) : PushMetricExporter {
        fun getCount(): Int = count.load()

        override fun export(metrics: ResourceMetrics): OTelSdkResult =
            if (count.addAndFetch(1) == 1) {
                Result.failure(OTelSdkError.InternalFailure("export failed"))
            } else {
                Result.success(Unit)
            }

        override fun forceFlush(): OTelSdkResult = Result.success(Unit)

        override fun shutdown(): OTelSdkResult = Result.success(Unit)

        override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult = Result.success(Unit)

        override fun temporality(): Temporality = Temporality.Cumulative
    }

    private class MockMetricExporter(
        val isShutdown: AtomicBoolean = AtomicBoolean(false),
    ) : PushMetricExporter {
        override fun export(metrics: ResourceMetrics): OTelSdkResult = Result.success(Unit)

        override fun forceFlush(): OTelSdkResult = Result.success(Unit)

        override fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

        override fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
            isShutdown.store(true)
            return Result.success(Unit)
        }

        override fun temporality(): Temporality = Temporality.Cumulative
    }

    @Test
    fun periodicReaderCreationAndConfiguration() {
        val exporter = InMemoryMetricExporter.default()
        val reader =
            PeriodicReader
                .builder(exporter)
                .withInterval(30.seconds)
                .build()

        assertEquals(30.seconds, reader.intervalDuration)
        assertEquals(Temporality.Cumulative, reader.temporality(InstrumentKind.Counter))
    }

    @Test
    fun shutdownRepeat() {
        val exporter = InMemoryMetricExporter.default()
        val reader = PeriodicReader.builder(exporter).build()
        val meterProvider = SdkMeterProvider.builder().withReader(reader).build()

        val result1 = meterProvider.shutdown()
        assertTrue(result1.isSuccess)

        val result2 = meterProvider.shutdown()
        assertTrue(result2.isFailure)
        assertEquals(OTelSdkError.AlreadyShutdown, result2.exceptionOrNull())

        val result3 = meterProvider.shutdown()
        assertTrue(result3.isFailure)
        assertEquals(OTelSdkError.AlreadyShutdown, result3.exceptionOrNull())
    }

    @Test
    fun flushAfterShutdown() {
        val exporter = InMemoryMetricExporter.default()
        val reader = PeriodicReader.builder(exporter).build()
        val meterProvider = SdkMeterProvider.builder().withReader(reader).build()

        val resultFlush = meterProvider.forceFlush()
        assertTrue(resultFlush.isSuccess)

        val resultShutdown = meterProvider.shutdown()
        assertTrue(resultShutdown.isSuccess)

        val resultFlushAfter = meterProvider.forceFlush()
        assertTrue(resultFlushAfter.isFailure)
    }

    @Test
    fun flushRepeat() {
        val exporter = InMemoryMetricExporter.default()
        val reader = PeriodicReader.builder(exporter).build()
        val meterProvider = SdkMeterProvider.builder().withReader(reader).build()

        val result1 = meterProvider.forceFlush()
        assertTrue(result1.isSuccess)

        val result2 = meterProvider.forceFlush()
        assertTrue(result2.isSuccess)
    }

    @Test
    fun periodicReaderWithoutPipeline() {
        val exporter = InMemoryMetricExporter.default()
        val reader = PeriodicReader.builder(exporter).build()

        val rm = ResourceMetrics(Resource.empty(), emptyList())
        val resultCollect = reader.collect(rm)
        assertTrue(resultCollect.isFailure)

        val resultFlush = reader.forceFlush()
        assertTrue(resultFlush.isFailure)

        val meterProvider = SdkMeterProvider.builder().withReader(reader).build()

        val resultCollectAfter = reader.collect(rm)
        assertTrue(resultCollectAfter.isSuccess)

        val resultFlushAfter = meterProvider.forceFlush()
        assertTrue(resultFlushAfter.isSuccess)
    }

    @Test
    fun shutdownPassedToExporter() {
        val exporter = MockMetricExporter()
        val reader = PeriodicReader.builder(exporter).build()
        val meterProvider = SdkMeterProvider.builder().withReader(reader).build()

        val result = meterProvider.shutdown()
        assertTrue(result.isSuccess)
        assertTrue(exporter.isShutdown.load())
    }

    @Test
    fun exporterFailuresAreHandled() {
        val exporter = MetricExporterThatFailsOnlyOnFirst()
        val rm = ResourceMetrics(Resource.empty(), emptyList())
        val firstExport = exporter.export(rm)
        assertTrue(firstExport.isFailure)
        val secondExport = exporter.export(rm)
        assertTrue(secondExport.isSuccess)
        assertEquals(2, exporter.getCount())
    }
}
