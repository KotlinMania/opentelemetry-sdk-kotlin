// port-lint: source propagation/baggage.rs
package io.github.kotlinmania.opentelemetrysdk.propagation

import io.github.kotlinmania.opentelemetry.BaggageMetadata
import io.github.kotlinmania.opentelemetry.Context
import io.github.kotlinmania.opentelemetry.KeyValue
import io.github.kotlinmania.opentelemetry.KeyValueMetadata
import io.github.kotlinmania.opentelemetry.propagation.Extractor
import io.github.kotlinmania.opentelemetry.propagation.FieldIter
import io.github.kotlinmania.opentelemetry.propagation.Injector
import io.github.kotlinmania.opentelemetry.propagation.TextMapPropagator

private data class SdkBaggageHolder(
    val entries: List<KeyValue>,
)

public val Context.baggage: List<KeyValue>
    get() = get<SdkBaggageHolder>()?.entries ?: emptyList()

public fun Context.withBaggage(baggage: Iterable<KeyValue>): Context =
    withValue(SdkBaggageHolder(this.baggage + baggage))

public fun Context.Companion.currentWithBaggage(baggage: Iterable<KeyValue>): Context =
    current().withBaggage(baggage)

public fun keyValueMetadataWithDefault(
    key: String,
    value: String,
    metadata: String = "",
): KeyValueMetadata =
    KeyValueMetadata.new(key, value, BaggageMetadata.from(metadata).asStr())

/**
 * Propagates name-value pairs in W3C Baggage format under `baggage` header.
 */
public class BaggagePropagator : TextMapPropagator {
    override fun injectContext(cx: Context, injector: Injector) {
        val baggage = cx.baggage
        if (baggage.isNotEmpty()) {
            val headerValue =
                baggage.joinToString(",") { kv ->
                    val name = percentEncode(kv.key.name.trim())
                    val valueStr = percentEncode(kv.value.toString().trim())
                    "$name=$valueStr"
                }
            injector.set(BAGGAGE_HEADER, headerValue)
        }
    }

    override fun extractWithContext(cx: Context, extractor: Extractor): Context {
        val headerValue = extractor.get(BAGGAGE_HEADER) ?: return cx
        val entries = mutableListOf<KeyValue>()

        for (item in headerValue.split(',')) {
            val trimmed = item.trim()
            if (trimmed.isEmpty()) continue

            val semiParts = trimmed.split(';')
            val nameValue = semiParts[0]
            val eqIdx = nameValue.indexOf('=')
            if (eqIdx <= 0) continue

            val rawKey = nameValue.substring(0, eqIdx).trim()
            val rawVal = nameValue.substring(eqIdx + 1).trim()

            val key = percentDecode(rawKey)
            val value = percentDecode(rawVal)

            entries.add(KeyValue(key, value))
        }

        return cx.withBaggage(entries)
    }

    override fun fields(): FieldIter = FieldIter.new(listOf(BAGGAGE_HEADER))

    public companion object {
        public const val BAGGAGE_HEADER: String = "baggage"

        public fun new(): BaggagePropagator = BaggagePropagator()

        public fun default(): BaggagePropagator = new()

        public fun baggageFields(): List<String> = listOf(BAGGAGE_HEADER)

        private val RESERVED_CHARS: Set<Char> = setOf(' ', '"', ';', ',', '=')

        public fun percentEncode(input: String): String {
            val sb = StringBuilder()
            val bytes = input.encodeToByteArray()
            for (b in bytes) {
                val c = b.toInt().toChar()
                if (b in 0x21..0x7e && c !in RESERVED_CHARS && c != '%') {
                    sb.append(c)
                } else {
                    val unsigned = b.toInt() and 0xFF
                    val hex = unsigned.toString(16).uppercase().padStart(2, '0')
                    sb.append('%').append(hex)
                }
            }
            return sb.toString()
        }

        public fun percentDecode(input: String): String {
            val bytes = mutableListOf<Byte>()
            var i = 0
            while (i < input.length) {
                val c = input[i]
                if (c == '%' && i + 2 < input.length) {
                    val hex = input.substring(i + 1, i + 3)
                    val b = hex.toIntOrNull(16)
                    if (b != null) {
                        bytes.add(b.toByte())
                        i += 3
                        continue
                    }
                }
                bytes.addAll(c.toString().encodeToByteArray().toList())
                i++
            }
            return bytes.toByteArray().decodeToString()
        }
    }
}
