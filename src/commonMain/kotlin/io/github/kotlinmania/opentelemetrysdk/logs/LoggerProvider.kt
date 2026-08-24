// port-lint: source logs/logger_provider.rs
package io.github.kotlinmania.opentelemetrysdk.logs

import io.github.kotlinmania.opentelemetrysdk.InstrumentationScope
import io.github.kotlinmania.opentelemetrysdk.OTelSdkError
import io.github.kotlinmania.opentelemetrysdk.OTelSdkResult
import io.github.kotlinmania.opentelemetrysdk.resource.Resource
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalAtomicApi::class)
internal class LoggerProviderInner(
    val processors: List<LogProcessor>,
    val isShutdown: AtomicBoolean = AtomicBoolean(false),
)

/**
 * Handles the creation and coordination of [SdkLogger]s.
 */
@OptIn(ExperimentalAtomicApi::class)
public class SdkLoggerProvider internal constructor(
    private val inner: LoggerProviderInner,
) : LoggerProvider {
    override fun logger(name: String): SdkLogger {
        val scope = InstrumentationScope.builder(name).build()
        return loggerWithScope(scope)
    }

    override fun loggerWithScope(scope: InstrumentationScope): SdkLogger {
        if (inner.isShutdown.load()) {
            return SdkLogger(scope, NOOP_LOGGER_PROVIDER)
        }
        return SdkLogger(scope, this)
    }

    public fun logProcessors(): List<LogProcessor> = inner.processors

    public fun forceFlush(): OTelSdkResult {
        val results = inner.processors.map { it.forceFlush() }
        return if (results.all { it.isSuccess }) {
            Result.success(Unit)
        } else {
            Result.failure(OTelSdkError.InternalFailure("Force flush errors: $results"))
        }
    }

    public fun shutdownWithTimeout(timeout: Duration): OTelSdkResult {
        if (inner.isShutdown.compareAndSet(false, true)) {
            val results = inner.processors.map { it.shutdownWithTimeout(timeout) }
            return if (results.all { it.isSuccess }) {
                Result.success(Unit)
            } else {
                Result.failure(OTelSdkError.InternalFailure("Shutdown errors: $results"))
            }
        } else {
            return Result.failure(OTelSdkError.AlreadyShutdown)
        }
    }

    public fun shutdown(): OTelSdkResult = shutdownWithTimeout(5.seconds)

    public companion object {
        public fun builder(): LoggerProviderBuilder = LoggerProviderBuilder()

        private val NOOP_LOGGER_PROVIDER: SdkLoggerProvider by lazy {
            SdkLoggerProvider(LoggerProviderInner(emptyList(), AtomicBoolean(true)))
        }
    }
}

/**
 * Builder for [SdkLoggerProvider].
 */
@OptIn(ExperimentalAtomicApi::class)
public class LoggerProviderBuilder {
    private val processors: MutableList<LogProcessor> = mutableListOf()
    private var resource: Resource? = null

    public fun withSimpleExporter(exporter: LogExporter): LoggerProviderBuilder {
        processors.add(SimpleLogProcessor.new(exporter))
        return this
    }

    public fun withBatchExporter(exporter: LogExporter): LoggerProviderBuilder {
        processors.add(BatchLogProcessor.builder(exporter).build())
        return this
    }

    public fun withLogProcessor(processor: LogProcessor): LoggerProviderBuilder {
        processors.add(processor)
        return this
    }

    public fun withResource(resource: Resource): LoggerProviderBuilder {
        this.resource = this.resource?.merge(resource) ?: resource
        return this
    }

    public fun build(): SdkLoggerProvider {
        val effectiveResource = resource ?: Resource.builder().build()
        for (processor in processors) {
            processor.setResource(effectiveResource)
        }
        val inner = LoggerProviderInner(
            processors = processors.toList(),
            isShutdown = AtomicBoolean(false),
        )
        return SdkLoggerProvider(inner)
    }

    public companion object {
        public fun new(): LoggerProviderBuilder = LoggerProviderBuilder()
    }
}
