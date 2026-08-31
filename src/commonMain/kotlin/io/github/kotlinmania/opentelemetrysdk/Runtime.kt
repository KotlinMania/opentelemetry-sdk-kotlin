// port-lint: source opentelemetry_sdk/src/runtime.rs
package io.github.kotlinmania.opentelemetrysdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * A runtime is an abstraction of an async runtime. It allows
 * OpenTelemetry to work with any current and future runtime implementations.
 */
public interface Runtime {
    /**
     * Spawn a new task or thread, which executes the given block.
     */
    public fun spawn(block: suspend () -> Unit)

    /**
     * Return a delay that completes after the specified [duration].
     */
    public suspend fun delay(duration: Duration)
}

/**
 * Uses the given runtime to produce an interval stream.
 */
internal fun <T : Runtime> toIntervalStream(
    runtime: T,
    interval: Duration,
): Flow<Unit> =
    flow {
        while (true) {
            runtime.delay(interval)
            emit(Unit)
        }
    }

/**
 * Runtime implementation, which works with a standard coroutine scope and dispatcher.
 */
public class Tokio internal constructor(
    internal val scope: CoroutineScope,
) : Runtime,
    RuntimeChannel {
    public constructor() : this(CoroutineScope(Dispatchers.Default))

    override fun spawn(block: suspend () -> Unit) {
        scope.launch { block() }
    }

    override suspend fun delay(duration: Duration) {
        kotlinx.coroutines.delay(duration)
    }

    override fun <T : Any> batchMessageChannel(capacity: Int): Pair<Sender<T>, Receiver<T>> {
        val channel = Channel<T>(capacity)
        return Pair(ChannelSender(channel), ChannelReceiver(channel))
    }

    override fun toString(): String = "Tokio"
}

/**
 * Runtime implementation, which works with a current-thread / unconfined coroutine scope.
 */
public class TokioCurrentThread internal constructor(
    internal val scope: CoroutineScope,
) : Runtime,
    RuntimeChannel {
    public constructor() : this(CoroutineScope(Dispatchers.Unconfined))

    override fun spawn(block: suspend () -> Unit) {
        scope.launch { block() }
    }

    override suspend fun delay(duration: Duration) {
        kotlinx.coroutines.delay(duration)
    }

    override fun <T : Any> batchMessageChannel(capacity: Int): Pair<Sender<T>, Receiver<T>> {
        val channel = Channel<T>(capacity)
        return Pair(ChannelSender(channel), ChannelReceiver(channel))
    }

    override fun toString(): String = "TokioCurrentThread"
}

/**
 * Error returned by a [TrySend] implementation.
 */
public sealed class TrySendError(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    /** Send failed due to the channel being full. */
    public class ChannelFull : TrySendError("cannot send message to batch processor as the channel is full")

    /** Send failed due to the channel being closed. */
    public class ChannelClosed : TrySendError("cannot send message to batch processor as the channel is closed")

    /** Any other send error that isn't covered above. */
    public class Other(
        cause: Throwable,
    ) : TrySendError(cause.message ?: "other send error", cause)
}

/**
 * TrySend is an abstraction of Sender that is capable of sending messages.
 */
public interface TrySend<T> {
    /**
     * Try to send a message batch to a worker.
     */
    public fun trySend(item: T): Result<Unit>
}

/**
 * Sender handle for batch message channels.
 */
public interface Sender<T> : TrySend<T>

/**
 * Receiver handle for batch message channels.
 */
public interface Receiver<T> {
    public fun asFlow(): Flow<T>
}

internal class ChannelSender<T>(
    private val channel: Channel<T>,
) : Sender<T> {
    override fun trySend(item: T): Result<Unit> {
        val res = channel.trySend(item)
        return when {
            res.isSuccess -> Result.success(Unit)
            res.isClosed -> Result.failure(TrySendError.ChannelClosed())
            else -> Result.failure(TrySendError.ChannelFull())
        }
    }
}

internal class ChannelReceiver<T>(
    private val channel: Channel<T>,
) : Receiver<T> {
    override fun asFlow(): Flow<T> = channel.receiveAsFlow()
}

/**
 * RuntimeChannel is an extension to [Runtime]. Currently, it provides a
 * channel that is used by the log and span batch processors.
 */
public interface RuntimeChannel : Runtime {
    /**
     * Return the sender and receiver used to send batch messages.
     */
    public fun <T : Any> batchMessageChannel(capacity: Int): Pair<Sender<T>, Receiver<T>>
}
