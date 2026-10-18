package io.github.kotlinmania.opentelemetrysdk.metrics.internal

import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Marks a type that can have a value added and retrieved atomically.
 */
internal interface AtomicTracker<T> {
    fun store(value: T)

    fun add(value: T)

    fun getValue(): T

    fun getAndResetValue(): T
}

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicLongTracker(
    init: Long = 0L,
) : AtomicTracker<Long> {
    private val ref = AtomicLong(init)

    override fun store(value: Long) {
        ref.store(value)
    }

    override fun add(value: Long) {
        ref.addAndFetch(value)
    }

    override fun getValue(): Long = ref.load()

    override fun getAndResetValue(): Long = ref.exchange(0L)
}

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicULongTracker(
    init: ULong = 0uL,
) : AtomicTracker<ULong> {
    private val ref = AtomicLong(init.toLong())

    override fun store(value: ULong) {
        ref.store(value.toLong())
    }

    override fun add(value: ULong) {
        ref.addAndFetch(value.toLong())
    }

    override fun getValue(): ULong = ref.load().toULong()

    override fun getAndResetValue(): ULong = ref.exchange(0L).toULong()
}

@OptIn(ExperimentalAtomicApi::class)
internal class AtomicDoubleTracker(
    init: Double = 0.0,
) : AtomicTracker<Double> {
    private val ref = AtomicLong(init.toRawBits())

    override fun store(value: Double) {
        ref.store(value.toRawBits())
    }

    override fun add(value: Double) {
        while (true) {
            val currentBits = ref.load()
            val currentVal = Double.fromBits(currentBits)
            val newVal = currentVal + value
            val newBits = newVal.toRawBits()
            if (ref.compareAndSet(currentBits, newBits)) {
                return
            }
        }
    }

    override fun getValue(): Double = Double.fromBits(ref.load())

    override fun getAndResetValue(): Double = Double.fromBits(ref.exchange(0.0.toRawBits()))
}
