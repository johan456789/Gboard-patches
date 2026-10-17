package dev.jason.gboardpatches.patches.gboard.features.flypy

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Checks the bundled zh-Hant flyPY ForwardTokenDictionary against regressions that the
 * TW pinyin engine accepts silently (enrollment succeeds, typing just shows Latin letters).
 */
class GboardFlypyTokenDictionaryTest {
    private val dictionary = TokenDictionary.parse(
        object {}.javaClass.classLoader
            .getResourceAsStream("flypy-assets/tokens_shuangpin_flypy")
            ?.use { it.readBytes() }
            ?: error("flyPY token dictionary asset not found"),
    )

    @Test
    fun `keeps the flyPY trie the key ids below were taken from`() {
        assertEquals(TRIE_SHA256, sha256(dictionary.trie))
    }

    @Test
    fun `uses the TW container layout`() {
        // With the CN layout (tbl1 column, CN meta) the TW engine returns no words for TW tokens.
        assertArrayEquals(hex(TW_META_HEX), dictionary.meta)
        assertEquals(0, dictionary.tableCounts[1])
        assertEquals(0, dictionary.tableBits[1])
        assertArrayEquals(ByteArray(8), dictionary.tail)
    }

    @Test
    fun `maps the v spelling of ju qu xu yu to the same TW token`() {
        listOf(JV to JU, QV to QU, XV to XU, YV to YU).forEach { (vKey, uKey) ->
            assertNotEquals(UNUSED_TOKEN, dictionary.token(uKey))
            assertEquals(dictionary.token(uKey), dictionary.token(vKey))
        }
    }

    @Test
    fun `disables only the flyPY codes whose syllable does not exist in TW`() {
        // 42 codes (biang, din, fai, ...) plus the <unused_token> entry itself.
        assertEquals(43, dictionary.tokens.count { it == UNUSED_TOKEN })
    }

    private class TokenDictionary(
        val trie: ByteArray,
        val meta: ByteArray,
        val tableCounts: IntArray,
        val tableBits: IntArray,
        val tokens: LongArray,
        val tail: ByteArray,
    ) {
        fun token(keyId: Int): Long = tokens[keyId]

        companion object {
            fun parse(bytes: ByteArray): TokenDictionary {
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
                return TokenDictionary(trie, meta, counts, bits, tokens, bytes.copyOfRange(offset, bytes.size))
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

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private fun hex(value: String): ByteArray =
        ByteArray(value.length / 2) { value.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private companion object {
        const val HEADER_SIZE = 0x30
        const val TABLE_COUNT = 5
        const val UNUSED_TOKEN = 8448952L
        const val TRIE_SHA256 = "78869c4a5213e8562a4fc39b36d6b1ab0ffac870a7b37f5daae30d2b1014f41d"
        const val TW_META_HEX = "080012012a1a00204528003001380140004800580060ffffffffffffffffff01"

        // marisa key ids of the flyPY codes in the bundled trie
        const val JU = 183
        const val JV = 184
        const val QU = 309
        const val QV = 310
        const val XU = 439
        const val XV = 440
        const val YU = 456
        const val YV = 457
    }
}
