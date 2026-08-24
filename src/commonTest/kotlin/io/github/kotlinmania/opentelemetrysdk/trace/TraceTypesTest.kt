package io.github.kotlinmania.opentelemetrysdk.trace

import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Value
import kotlin.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TraceTypesTest {
    @Test
    fun testSpanLimitsDefaults() {
        val limits = SpanLimits.defaultLimits()
        assertEquals(DEFAULT_MAX_EVENT_PER_SPAN, limits.maxEventsPerSpan)
        assertEquals(DEFAULT_MAX_ATTRIBUTES_PER_SPAN, limits.maxAttributesPerSpan)
        assertEquals(DEFAULT_MAX_LINKS_PER_SPAN, limits.maxLinksPerSpan)
        assertEquals(DEFAULT_MAX_ATTRIBUTES_PER_EVENT, limits.maxAttributesPerEvent)
        assertEquals(DEFAULT_MAX_ATTRIBUTES_PER_LINK, limits.maxAttributesPerLink)
    }

    @Test
    fun testSpanLimitsCustom() {
        val limits = SpanLimits(
            maxEventsPerSpan = 64u,
            maxAttributesPerSpan = 32u,
            maxLinksPerSpan = 16u,
            maxAttributesPerEvent = 8u,
            maxAttributesPerLink = 4u,
        )
        assertEquals(64u, limits.maxEventsPerSpan)
        assertEquals(32u, limits.maxAttributesPerSpan)
        assertEquals(16u, limits.maxLinksPerSpan)
        assertEquals(8u, limits.maxAttributesPerEvent)
        assertEquals(4u, limits.maxAttributesPerLink)
    }

    @Test
    fun testTraceErrorVariants() {
        val timeoutErr = TraceError.ExportTimedOut(kotlin.time.Duration.parse("5s"))
        assertTrue(timeoutErr.message!!.contains("5 seconds"))

        val shutdownErr = TraceError.TracerProviderAlreadyShutdown
        assertEquals("TracerProvider already shutdown", shutdownErr.message)

        val otherErr = TraceError.from("custom error message")
        assertEquals("custom error message", otherErr.message)
    }

    @Test
    fun testSpanEventsLifecycle() {
        val emptyEvents = SpanEvents.EMPTY
        assertTrue(emptyEvents.isEmpty)
        assertEquals(0, emptyEvents.size)

        val event = Event(
            name = "test-event",
            timestamp = Instant.fromEpochMilliseconds(1000L),
            attributes = listOf(KeyValue(Key("attr"), Value.of("val"))),
        )
        val withEvent = emptyEvents.withAddedEvent(event)
        assertFalse(withEvent.isEmpty)
        assertEquals(1, withEvent.size)
        assertEquals(event, withEvent[0])
        assertEquals(1, withEvent.toList().size)
    }

    @Test
    fun testSpanLinksLifecycle() {
        val emptyLinks = SpanLinks.EMPTY
        assertTrue(emptyLinks.isEmpty)
        assertEquals(0, emptyLinks.size)

        val context = SpanContext.emptyContext()
        val link = Link.withContext(context)
        val withLink = emptyLinks.withAddedLink(link)
        assertFalse(withLink.isEmpty)
        assertEquals(1, withLink.size)
        assertEquals(link, withLink[0])
        assertEquals(1, withLink.toList().size)
    }
}
