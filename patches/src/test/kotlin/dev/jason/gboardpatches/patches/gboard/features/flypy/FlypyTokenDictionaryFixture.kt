package dev.jason.gboardpatches.patches.gboard.features.flypy

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.junit.Assert.assertEquals

/** Parsed view of the bundled zh-Hant flyPY ForwardTokenDictionary asset. */
internal class FlypyTokenDictionary(
    val trie: ByteArray,
    val meta: ByteArray,
    val tableCounts: IntArray,
    val tableBits: IntArray,
    val tokens: LongArray,
    val tail: ByteArray,
) {
    /** TW token the engine gets for a typed two-letter flyPY [code]. */
    fun token(code: String): Long =
        tokens[FLYPY_KEY_IDS[code] ?: error("no key id recorded for flyPY code '$code'")]

    companion object {
        const val UNUSED_TOKEN = 8448952L

        // The key ids in FLYPY_KEY_IDS are only valid for this marisa trie.
        const val TRIE_SHA256 = "78869c4a5213e8562a4fc39b36d6b1ab0ffac870a7b37f5daae30d2b1014f41d"

        private const val HEADER_SIZE = 0x30
        private const val TABLE_COUNT = 5

        fun loadBundled(): FlypyTokenDictionary = parse(
            object {}.javaClass.classLoader
                .getResourceAsStream("flypy-assets/tokens_shuangpin_flypy")
                ?.use { it.readBytes() }
                ?: error("flyPY token dictionary asset not found"),
        )

        fun parse(bytes: ByteArray): FlypyTokenDictionary {
            val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            assertEquals("ForwardTokenDictionary", String(bytes, 4, 22, StandardCharsets.US_ASCII))
            val trieSize = buffer.getLong(0x20).toInt()
            val trie = bytes.copyOfRange(HEADER_SIZE, HEADER_SIZE + trieSize)
            val rest = HEADER_SIZE + trieSize
            val metaSize = buffer.getInt(rest)
            val meta = bytes.copyOfRange(rest + 4, rest + 4 + metaSize)
            var offset = rest + ((4 + metaSize + 7) and 7.inv())
            val counts = IntArray(TABLE_COUNT)
            val bits = IntArray(TABLE_COUNT)
            var tokens = LongArray(0)
            for (table in 0 until TABLE_COUNT) {
                counts[table] = buffer.getInt(offset)
                bits[table] = buffer.getInt(offset + 4)
                val size = (((counts[table].toLong() * bits[table]) + 63) shr 3).toInt() and 7.inv()
                if (table == 0) {
                    tokens = unpack(bytes, offset + 8, counts[table], bits[table])
                }
                offset += 8 + size
            }
            return FlypyTokenDictionary(trie, meta, counts, bits, tokens, bytes.copyOfRange(offset, bytes.size))
        }

        private fun unpack(bytes: ByteArray, start: Int, count: Int, bits: Int): LongArray =
            LongArray(count) { index ->
                var value = 0L
                for (bit in 0 until bits) {
                    val position = index.toLong() * bits + bit
                    val byte = bytes[start + (position shr 3).toInt()].toInt()
                    value = value or (((byte shr (position and 7).toInt()) and 1).toLong() shl bit)
                }
                value
            }
    }
}

internal fun sha256(bytes: ByteArray): String =
    MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

// marisa key ids of flyPY codes in the bundled trie (see FlypyTokenDictionary.TRIE_SHA256)
internal val FLYPY_KEY_IDS = mapOf(
    "aa" to 24, "an" to 27, "bc" to 31, "bm" to 40, "bo" to 42, "bw" to 45, "da" to 65, "dm" to 76,
    "fg" to 99, "gc" to 115, "gs" to 126, "gu" to 127, "hc" to 134, "hk" to 141, "hx" to 150,
    "ia" to 153, "ie" to 156, "ii" to 160, "jp" to 178, "ju" to 183, "jv" to 184, "jx" to 185,
    "ka" to 187, "lb" to 207, "le" to 210, "li" to 214, "lm" to 218, "ls" to 224, "mm" to 244,
    "nd" to 255, "nq" to 268, "qu" to 309, "qv" to 310, "ri" to 321, "rz" to 336, "se" to 340,
    "sf" to 341, "su" to 351, "td" to 360, "ti" to 365, "tj" to 366, "tp" to 371, "ts" to 373,
    "uh" to 385, "ui" to 386, "uj" to 387, "vf" to 403, "vj" to 407, "vs" to 412, "vu" to 413,
    "wj" to 424, "wu" to 426, "xb" to 428, "xn" to 433, "xp" to 434, "xu" to 439, "xv" to 440,
    "yc" to 445, "ye" to 446, "yj" to 449, "yr" to 453, "yt" to 455, "yu" to 456, "yv" to 457,
    "yy" to 458, "yz" to 459,
)
