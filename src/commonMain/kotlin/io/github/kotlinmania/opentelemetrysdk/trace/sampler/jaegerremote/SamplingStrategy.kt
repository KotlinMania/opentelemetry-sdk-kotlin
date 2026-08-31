// port-lint: source opentelemetry_sdk/src/trace/sampler/jaeger_remote/sampling_strategy.rs
package io.github.kotlinmania.opentelemetrysdk.trace.sampler.jaegerremote

import io.github.kotlinmania.opentelemetrysdk.trace.Sampler
import io.github.kotlinmania.opentelemetrysdk.trace.SamplingDecision
import io.github.kotlinmania.opentelemetrysdk.trace.SamplingResult
import io.github.kotlinmania.opentelemetrysdk.trace.SpanContext
import io.github.kotlinmania.opentelemetrysdk.trace.TraceId
import io.github.kotlinmania.opentelemetrysdk.trace.TraceState
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal sealed class Strategy {
    data class Probabilistic(
        val rate: Double,
    ) : Strategy()

    data class RateLimiting(
        val leakyBucket: LeakyBucket,
    ) : Strategy()

    data class PerOperation(
        val strategies: PerOperationStrategies,
    ) : Strategy()
}

internal class PerOperationStrategies(
    var defaultProb: Double = 0.0,
    var defaultLowerBoundTracesPerSecond: Double = 0.0,
    var operationProb: Map<String, Double> = emptyMap(),
    var defaultUpperBoundTracesPerSecond: Double = 0.0,
) {
    fun update(remoteStrategies: PerOperationSamplingStrategies) {
        this.defaultProb = remoteStrategies.defaultSamplingProbability
        this.defaultLowerBoundTracesPerSecond = remoteStrategies.defaultLowerBoundTracesPerSecond
        this.defaultUpperBoundTracesPerSecond = remoteStrategies.defaultUpperBoundTracesPerSecond
        this.operationProb =
            remoteStrategies.perOperationStrategies.associate {
                it.operation to it.probabilisticSampling.samplingRate
            }
    }

    fun getProbability(operation: String): Double =
        operationProb[operation] ?: defaultProb
}

@OptIn(ExperimentalAtomicApi::class)
internal class Inner(
    val leakyBucketSize: Double,
) {
    private val strategyRef = AtomicReference<Strategy?>(null)

    fun update(remoteStrategyResp: SamplingStrategyResponse) {
        val newStrategy =
            initStrategy(
                remoteStrategyResp.operationSampling,
                remoteStrategyResp.rateLimitingSampling,
                remoteStrategyResp.probabilisticSampling,
            )
        if (newStrategy != null) {
            strategyRef.store(newStrategy)
        }
    }

    private fun initStrategy(
        operationSampling: PerOperationSamplingStrategies?,
        rateLimitingSampling: RateLimitingSamplingStrategy?,
        probabilisticSampling: ProbabilisticSamplingStrategy?,
    ): Strategy? =
        when {
            operationSampling != null -> {
                val perOps = PerOperationStrategies()
                perOps.update(operationSampling)
                Strategy.PerOperation(perOps)
            }
            rateLimitingSampling != null -> {
                Strategy.RateLimiting(
                    LeakyBucket(
                        bucketSize = leakyBucketSize,
                        spanPerSec = rateLimitingSampling.maxTracesPerSecond.toDouble(),
                    ),
                )
            }
            probabilisticSampling != null -> {
                Strategy.Probabilistic(probabilisticSampling.samplingRate)
            }
            else -> null
        }

    fun shouldSample(
        parentContext: SpanContext?,
        traceId: TraceId,
        name: String,
    ): SamplingResult? {
        val currentStrategy = strategyRef.load() ?: return null
        val decision =
            when (currentStrategy) {
                is Strategy.RateLimiting -> {
                    if (currentStrategy.leakyBucket.shouldSample()) {
                        SamplingDecision.RECORD_AND_SAMPLE
                    } else {
                        SamplingDecision.DROP
                    }
                }
                is Strategy.Probabilistic -> {
                    Sampler.TraceIdRatioBased.sampleBasedOnProbability(currentStrategy.rate, traceId)
                }
                is Strategy.PerOperation -> {
                    val prob = currentStrategy.strategies.getProbability(name)
                    Sampler.TraceIdRatioBased.sampleBasedOnProbability(prob, traceId)
                }
            }
        return SamplingResult(
            decision = decision,
            attributes = emptyList(),
            traceState = parentContext?.traceState ?: TraceState.DEFAULT,
        )
    }
}
