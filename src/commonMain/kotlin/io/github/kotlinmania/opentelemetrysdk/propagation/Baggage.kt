// port-lint: source propagation/baggage.rs
package io.github.kotlinmania.opentelemetrysdk.propagation

import io.github.kotlinmania.opentelemetrysdk.Context
import io.github.kotlinmania.opentelemetrysdk.resource.Key
import io.github.kotlinmania.opentelemetrysdk.resource.KeyValue
import io.github.kotlinmania.opentelemetrysdk.resource.Value

/**
 * Metadata associated with a baggage entry.
 */
public data class BaggageMetadata(
    public val value: String = "",
) {
    public companion object {
        public val DEFAULT: BaggageMetadata = BaggageMetadata("")
    }
}

/**
 * Baggage key-value entry with optional metadata properties.
 */
public data class KeyValueMetadata(
    public val key: Key,
    public val value: Value,
    public val metadata: BaggageMetadata = BaggageMetadata.DEFAULT,
) {
    public constructor(key: String, value: String, metadata: String = "") : this(
        Key(key),
        Value.of(value),
        BaggageMetadata(metadata),
    )
}

/**
 * Propagates name-value pairs in W3C Baggage format under `baggage` header.
 */
public class BaggagePropagator : TextMapPropagator {
    override fun injectContext(context: Context, injector: Injector) {
        val baggage = context.baggage
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

    override fun extractWithContext(context: Context, extractor: Extractor): Context {
        val headerValue = extractor.get(BAGGAGE_HEADER) ?: return context
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

        return context.withBaggage(entries)
    }

    override fun fields(): List<String> = listOf(BAGGAGE_HEADER)

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
