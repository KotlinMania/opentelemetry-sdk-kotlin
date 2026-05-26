// port-lint: source src/growable_array.rs
package io.github.kotlinmania.opentelemetrysdk

/** The default max capacity for the stack portion of [GrowableArray]. */
internal const val DEFAULT_MAX_INLINE_CAPACITY: Int = 10

/** The default initial capacity for the vector portion of [GrowableArray]. */
internal const val DEFAULT_INITIAL_OVERFLOW_CAPACITY: Int = 5

/**
 * A hybrid vector that starts with a fixed-size array and grows dynamically with a vector.
 *
 * [GrowableArray] uses an internal fixed-size array (`inline`) for storing elements until it
 * reaches [maxInlineCapacity]. When this capacity is exceeded, additional elements are stored
 * in a heap-allocated list ([overflow]). This structure allows for efficient use of the
 * inline buffer for small numbers of elements, while still supporting dynamic growth.
 *
 * The upstream Rust type uses const generics for the inline / initial-overflow capacities.
 * Kotlin Multiplatform does not have value-level generics, so those are translated to
 * regular constructor parameters with the same default values.
 */
internal class GrowableArray<T>(
    private val default: () -> T,
    private val maxInlineCapacity: Int = DEFAULT_MAX_INLINE_CAPACITY,
    private val initialOverflowCapacity: Int = DEFAULT_INITIAL_OVERFLOW_CAPACITY,
) {
    private val inline: MutableList<T> = MutableList(maxInlineCapacity) { default() }
    private var overflow: MutableList<T>? = null
    private var count: Int = 0

    /**
     * Pushes a value into the [GrowableArray].
     *
     * If the inline buffer has reached its capacity ([maxInlineCapacity]), the value is pushed
     * into the heap-allocated overflow list. Otherwise, it is stored in the inline buffer.
     */
    fun push(value: T) {
        if (count < maxInlineCapacity) {
            inline[count] = value
            count += 1
        } else {
            val list = overflow ?: ArrayList<T>(initialOverflowCapacity).also { overflow = it }
            list.add(value)
        }
    }

    /**
     * Gets the value at the specified index.
     *
     * Returns `null` if the index is out of bounds.
     */
    fun get(index: Int): T? {
        if (index < 0) return null
        if (index < count) return inline[index]
        val tail = overflow ?: return null
        val offset = index - maxInlineCapacity
        if (offset < 0 || offset >= tail.size) return null
        return tail[offset]
    }

    /** Returns the number of elements in the [GrowableArray]. */
    fun len(): Int = count + (overflow?.size ?: 0)

    /**
     * Returns a sequence over the elements in the [GrowableArray].
     *
     * The sequence yields elements from the inline buffer first, followed by elements from
     * the overflow list if present. This allows for efficient iteration over both the inline
     * and the heap-allocated portions.
     */
    fun iter(): Sequence<T> {
        val tail = overflow
        val inlinePart = inline.asSequence().take(count)
        return if (tail == null || tail.isEmpty()) {
            inlinePart
        } else {
            inlinePart + tail.asSequence()
        }
    }

    /** Consumes this collection and returns an iterator that walks all elements in order. */
    fun intoIter(): Iterator<T> = GrowableArrayIntoIter(this)

    /** Structural equality on (maxInlineCapacity, count, inline-prefix, overflow). */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GrowableArray<*>) return false
        if (maxInlineCapacity != other.maxInlineCapacity) return false
        if (count != other.count) return false
        for (i in 0 until count) {
            if (inline[i] != other.inline[i]) return false
        }
        return overflow == other.overflow
    }

    override fun hashCode(): Int {
        var result = maxInlineCapacity.hashCode()
        result = 31 * result + count.hashCode()
        for (i in 0 until count) {
            result = 31 * result + (inline[i]?.hashCode() ?: 0)
        }
        result = 31 * result + (overflow?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String =
        "GrowableArray(count=$count, inline=${inline.subList(0, count)}, overflow=$overflow)"
}

/** Iterator for consuming a [GrowableArray]. */
internal class GrowableArrayIntoIter<T>(source: GrowableArray<T>) : Iterator<T> {
    private val backing: Iterator<T> = source.iter().iterator()
    override fun hasNext(): Boolean = backing.hasNext()
    override fun next(): T = backing.next()
}
