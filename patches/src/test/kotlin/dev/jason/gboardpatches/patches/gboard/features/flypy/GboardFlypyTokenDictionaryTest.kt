package dev.jason.gboardpatches.patches.gboard.features.flypy

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Checks the bundled zh-Hant flyPY ForwardTokenDictionary against regressions that the
 * TW pinyin engine accepts silently (enrollment succeeds, typing just shows Latin letters).
 */
class GboardFlypyTokenDictionaryTest {
    private val dictionary = FlypyTokenDictionary.loadBundled()

    @Test
    fun `keeps the flyPY trie the key ids were taken from`() {
        assertEquals(FlypyTokenDictionary.TRIE_SHA256, sha256(dictionary.trie))
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
        listOf("jv" to "ju", "qv" to "qu", "xv" to "xu", "yv" to "yu").forEach { (vCode, uCode) ->
            assertNotEquals(FlypyTokenDictionary.UNUSED_TOKEN, dictionary.token(uCode))
            assertEquals(dictionary.token(uCode), dictionary.token(vCode))
        }
    }

    @Test
    fun `disables only the flyPY codes whose syllable does not exist in TW`() {
        // 42 codes (biang, din, fai, ...) plus the <unused_token> entry itself.
        assertEquals(43, dictionary.tokens.count { it == FlypyTokenDictionary.UNUSED_TOKEN })
    }

    private fun hex(value: String): ByteArray =
        ByteArray(value.length / 2) { value.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    private companion object {
        const val TW_META_HEX = "080012012a1a00204528003001380140004800580060ffffffffffffffffff01"
    }
}
