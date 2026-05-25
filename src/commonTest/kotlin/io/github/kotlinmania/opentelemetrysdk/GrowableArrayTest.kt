// port-lint: tests src/growable_array.rs
package io.github.kotlinmania.opentelemetrysdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GrowableArrayTest {
    private fun newIntArray(): GrowableArray<Int> = GrowableArray(default = { 0 })

    @Test
    fun testPushAndGet() {
        val collection = newIntArray()
        for (i in 0 until 15) {
            collection.push(i)
        }
        for (i in 0 until 15) {
            assertEquals(i, collection.get(i))
        }
    }

    @Test
    fun testLen() {
        val collection = newIntArray()
        for (i in 0 until 15) {
            collection.push(i)
        }
        assertEquals(15, collection.len())
    }

    @Test
    fun testIntoIter() {
        val collection = newIntArray()
        for (i in 0 until 15) {
            collection.push(i)
        }
        val iter = collection.intoIter()
        for (i in 0 until 15) {
            assertEquals(i, iter.next())
        }
        assertEquals(false, iter.hasNext())
    }

    @Test
    fun testRefIter() {
        val collection = newIntArray()
        for (i in 0 until 15) {
            collection.push(i)
        }
        var count = 0
        for (value in collection.iter()) {
            assertEquals(count, value)
            count += 1
        }
        assertEquals(15, count)
    }

    // Upstream `test_key_value_pair_storage_growable_array` exercises GrowableArray with
    // `Option<(opentelemetry::Key, opentelemetry::logs::AnyValue)>`. Those types live in the
    // `opentelemetry-kotlin` sibling repo, which has no Maven Central publication yet, so this
    // crate cannot declare a Gradle dependency on it. The shape under test — pushing nullable
    // tuples, iterating, and reading them back — is exercised by `testPushAndGet` and
    // `testIntoIter` above using `Int?`. When `opentelemetry-kotlin` ships a Maven artifact,
    // port the original test verbatim against `Key` + `AnyValue`.

    @Test
    fun testEmptyAttributes() {
        val collection = GrowableArray<Pair<Int, Int>?>(default = { null })
        assertEquals(0, collection.len())
        assertNull(collection.get(0))

        val iter = collection.intoIter()
        assertEquals(false, iter.hasNext())
    }

    @Test
    fun testLessThanMaxStackCapacity() {
        val collection = newIntArray()
        for (i in 0 until DEFAULT_MAX_INLINE_CAPACITY - 1) {
            collection.push(i)
        }
        assertEquals(DEFAULT_MAX_INLINE_CAPACITY - 1, collection.len())

        for (i in 0 until DEFAULT_MAX_INLINE_CAPACITY - 1) {
            assertEquals(i, collection.get(i))
        }
        assertNull(collection.get(DEFAULT_MAX_INLINE_CAPACITY - 1))
        assertNull(collection.get(DEFAULT_MAX_INLINE_CAPACITY))

        val iter = collection.intoIter()
        for (i in 0 until DEFAULT_MAX_INLINE_CAPACITY - 1) {
            assertEquals(i, iter.next())
        }
        assertEquals(false, iter.hasNext())
    }

    @Test
    fun testExactlyMaxStackCapacity() {
        val collection = newIntArray()
        for (i in 0 until DEFAULT_MAX_INLINE_CAPACITY) {
            collection.push(i)
        }
        assertEquals(DEFAULT_MAX_INLINE_CAPACITY, collection.len())

        for (i in 0 until DEFAULT_MAX_INLINE_CAPACITY) {
            assertEquals(i, collection.get(i))
        }
        assertNull(collection.get(DEFAULT_MAX_INLINE_CAPACITY))

        val iter = collection.intoIter()
        for (i in 0 until DEFAULT_MAX_INLINE_CAPACITY) {
            assertEquals(i, iter.next())
        }
        assertEquals(false, iter.hasNext())
    }

    @Test
    fun testExceedsStackButNotInitialVecCapacity() {
        val collection = newIntArray()
        val total = DEFAULT_MAX_INLINE_CAPACITY + DEFAULT_INITIAL_OVERFLOW_CAPACITY - 1
        for (i in 0 until total) {
            collection.push(i)
        }
        assertEquals(total, collection.len())

        for (i in 0 until total) {
            assertEquals(i, collection.get(i))
        }
        assertNull(collection.get(total))
        assertNull(collection.get(total + 1))

        val iter = collection.intoIter()
        for (i in 0 until total) {
            assertEquals(i, iter.next())
        }
        assertEquals(false, iter.hasNext())
    }

    @Test
    fun testExceedsBothStackAndInitialVecCapacities() {
        val collection = newIntArray()
        val total = DEFAULT_MAX_INLINE_CAPACITY + DEFAULT_INITIAL_OVERFLOW_CAPACITY + 5
        for (i in 0 until total) {
            collection.push(i)
        }
        assertEquals(total, collection.len())

        for (i in 0 until total) {
            assertEquals(i, collection.get(i))
        }
        assertNull(collection.get(total))

        val iter = collection.intoIter()
        for (i in 0 until total) {
            assertEquals(i, iter.next())
        }
        assertEquals(false, iter.hasNext())
    }
}
