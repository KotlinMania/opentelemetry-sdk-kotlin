package io.github.kotlinmania.opentelemetrysdk

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuntimeTest {
    @Test
    fun testTokioRuntime() {
        val runtime = Tokio()
        assertEquals("Tokio", runtime.toString())

        val (tx, rx) = runtime.batchMessageChannel<String>(10)
        val sendRes = tx.trySend("hello")
        assertTrue(sendRes.isSuccess)
    }

    @Test
    fun testTokioCurrentThreadRuntime() {
        val runtime = TokioCurrentThread()
        assertEquals("TokioCurrentThread", runtime.toString())

        val (tx, rx) = runtime.batchMessageChannel<Int>(10)
        val sendRes = tx.trySend(42)
        assertTrue(sendRes.isSuccess)
    }

    @Test
    fun testTrySendErrorTypes() {
        val fullErr = TrySendError.ChannelFull()
        assertTrue(fullErr.message!!.contains("full"))

        val closedErr = TrySendError.ChannelClosed()
        assertTrue(closedErr.message!!.contains("closed"))

        val customErr = TrySendError.Other(IllegalArgumentException("bad"))
        assertEquals("bad", customErr.message)
    }
}
