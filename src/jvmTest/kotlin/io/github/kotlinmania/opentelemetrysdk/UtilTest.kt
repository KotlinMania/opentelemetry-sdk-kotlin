package io.github.kotlinmania.opentelemetrysdk

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class UtilTest {
    @Test
    fun testIntervalFlow() {
        runBlocking {
            val result = intervalFlow(10.milliseconds).take(3).toList()
            assertEquals(3, result.size)
        }
    }
}
