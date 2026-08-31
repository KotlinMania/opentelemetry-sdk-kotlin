// port-lint: source opentelemetry_sdk/src/trace/sampler/jaeger_remote/remote.rs
package io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * ProbabilisticSamplingStrategy samples traces with a fixed probability.
 */
@Serializable
internal data class ProbabilisticSamplingStrategy(
    @SerialName("samplingRate")
    val samplingRate: Double,
)

/**
 * RateLimitingSamplingStrategy samples a fixed number of traces per time interval.
 */
@Serializable
internal data class RateLimitingSamplingStrategy(
    @SerialName("maxTracesPerSecond")
    val maxTracesPerSecond: Int,
)

/**
 * OperationSamplingStrategy is a sampling strategy for a given operation.
 */
@Serializable
internal data class OperationSamplingStrategy(
    @SerialName("operation")
    val operation: String,
    @SerialName("probabilisticSampling")
    val probabilisticSampling: ProbabilisticSamplingStrategy,
)

/**
 * PerOperationSamplingStrategies is a combination of strategies for different endpoints.
 */
@Serializable
internal data class PerOperationSamplingStrategies(
    @SerialName("defaultSamplingProbability")
    val defaultSamplingProbability: Double = 0.0,
    @SerialName("defaultLowerBoundTracesPerSecond")
    val defaultLowerBoundTracesPerSecond: Double = 0.0,
    @SerialName("perOperationStrategies")
    val perOperationStrategies: List<OperationSamplingStrategy> = emptyList(),
    @SerialName("defaultUpperBoundTracesPerSecond")
    val defaultUpperBoundTracesPerSecond: Double = 0.0,
)

@Serializable
internal enum class SamplingStrategyType {
    @SerialName("PROBABILISTIC")
    PROBABILISTIC,

    @SerialName("RATE_LIMITING")
    RATE_LIMITING,
}

/**
 * SamplingStrategyResponse contains an overall sampling strategy for a given service.
 */
@Serializable
internal data class SamplingStrategyResponse(
    @SerialName("strategyType")
    val strategyType: SamplingStrategyType? = null,
    @SerialName("probabilisticSampling")
    val probabilisticSampling: ProbabilisticSamplingStrategy? = null,
    @SerialName("rateLimitingSampling")
    val rateLimitingSampling: RateLimitingSamplingStrategy? = null,
    @SerialName("operationSampling")
    val operationSampling: PerOperationSamplingStrategies? = null,
)

/**
 * SamplingStrategyParameters defines request parameters for remote sampler.
 */
@Serializable
internal data class SamplingStrategyParameters(
    @SerialName("serviceName")
    val serviceName: String,
)
