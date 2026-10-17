package dev.jason.gboardpatches.patches.gboard.features.flypy

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Taiwan phrases verified by typing on an emulator. The native decoder cannot run in a unit
 * test, so this checks the part the bundled dictionary controls: every typed two-letter flyPY
 * code resolves to the TW engine's own toneless token for the intended syllable.
 */
class GboardFlypyPhraseTokenTest {
    private val dictionary = FlypyTokenDictionary.loadBundled()

    @Test
    fun `phrase codes follow the flyPY scheme`() {
        PHRASES.forEach { phrase ->
            assertEquals(phrase.text, phrase.typed, phrase.syllables.joinToString("") { flypyCode(it) })
        }
    }

    @Test
    fun `every phrase syllable resolves to its TW token`() {
        PHRASES.forEach { phrase ->
            phrase.syllables.forEachIndexed { index, syllable ->
                val code = phrase.typed.substring(index * 2, index * 2 + 2)
                assertEquals("${phrase.text}: $code ($syllable)", twToken(syllable), dictionary.token(code))
            }
        }
    }

    @Test
    fun `v spelling of ju qu xu yu resolves like the u spelling`() {
        V_SPELLING_PHRASES.forEach { phrase ->
            phrase.syllables.forEachIndexed { index, syllable ->
                val code = phrase.typed.substring(index * 2, index * 2 + 2)
                assertEquals("${phrase.text}: $code ($syllable)", twToken(syllable), dictionary.token(code))
            }
        }
    }

    private fun twToken(syllable: String): Long =
        TW_TOKENS[syllable] ?: error("no TW token recorded for '$syllable'")

    private class Phrase(val text: String, pinyin: String, val typed: String) {
        val syllables = pinyin.split(' ')
    }

    private companion object {
        val PHRASES = listOf(
            Phrase("珍珠奶茶", "zhen zhu nai cha", "vfvundia"),
            Phrase("捷運", "jie yun", "jpyy"),
            Phrase("台灣", "tai wan", "tdwj"),
            Phrase("牛肉麵", "niu rou mian", "nqrzmm"),
            Phrase("悠遊卡", "you you ka", "yzyzka"),
            Phrase("高鐵", "gao tie", "gctp"),
            Phrase("阿里山", "a li shan", "aaliuj"),
            Phrase("中華電信", "zhong hua dian xin", "vshxdmxb"),
            Phrase("好吃", "hao chi", "hcii"),
            Phrase("謝謝", "xie xie", "xpxp"),
            Phrase("加油", "jia you", "jxyz"),
            Phrase("大安森林公園", "da an sen lin gong yuan", "daansflbgsyr"),
            Phrase("便利商店", "bian li shang dian", "bmliuhdm"),
            Phrase("小籠包", "xiao long bao", "xnlsbc"),
            Phrase("夜市", "ye shi", "yeui"),
            Phrase("日月潭", "ri yue tan", "riyttj"),
            Phrase("故宮博物院", "gu gong bo wu yuan", "gugsbowuyr"),
            Phrase("台北車站", "tai bei che zhan", "tdbwievj"),
            Phrase("鳳梨酥", "feng li su", "fglisu"),
            // Taiwan reads 垃圾 as le4 se4
            Phrase("垃圾桶", "le se tong", "lesets"),
            Phrase("連結", "lian jie", "lmjp"),
            Phrase("取消", "qu xiao", "quxn"),
            Phrase("懷", "huai", "hk"),
            Phrase("啊", "a", "aa"),
        )

        // ü after j/q/x/y typed as v instead of u
        val V_SPELLING_PHRASES = listOf(
            Phrase("取消", "qu xiao", "qvxn"),
            Phrase("需要", "xu yao", "xvyc"),
            Phrase("具體", "ju ti", "jvti"),
            Phrase("語言", "yu yan", "yvyj"),
        )

        // Toneless token of each syllable in Gboard 18.0.3's zh_TW pinyin_token dictionary.
        val TW_TOKENS = mapOf(
            "a" to 33613358L, "an" to 33613389L, "bao" to 33611800L, "bei" to 33611795L,
            "bian" to 33611836L, "bo" to 33611784L, "cha" to 33612959L, "che" to 33612964L,
            "chi" to 33613858L, "da" to 33612056L, "dian" to 33612107L, "feng" to 33612043L,
            "gao" to 33612454L, "gong" to 33612512L, "gu" to 33612479L, "hao" to 33612606L,
            "hua" to 33612639L, "huai" to 33612649L, "jia" to 33612683L, "jie" to 33612688L,
            "ju" to 33612718L, "ka" to 33612517L, "le" to 33612331L, "li" to 33612365L,
            "lian" to 33612386L, "lin" to 33612390L, "long" to 33612422L, "mian" to 33611997L,
            "nai" to 33612242L, "niu" to 33612284L, "qu" to 33612783L, "ri" to 33613856L,
            "rou" to 33613128L, "se" to 33613308L, "sen" to 33613325L, "shan" to 33613071L,
            "shang" to 33613080L, "shi" to 33613859L, "su" to 33613333L, "tai" to 33612149L,
            "tan" to 33612166L, "ti" to 33612178L, "tie" to 33612183L, "tong" to 33612226L,
            "wan" to 33613484L, "wu" to 33613861L, "xiao" to 33612820L, "xie" to 33612814L,
            "xin" to 33612834L, "xu" to 33612849L, "yan" to 33613440L, "yao" to 33613430L,
            "ye" to 33613422L, "you" to 33613435L, "yu" to 33613862L, "yuan" to 33613511L,
            "yue" to 33613508L, "yun" to 33613516L, "zhan" to 33612903L, "zhen" to 33612907L,
            "zhong" to 33612950L, "zhu" to 33612919L,
        )

        // 小鶴雙拼 (flyPY) rules, written independently of the bundled data.
        val INITIALS = mapOf("zh" to "v", "ch" to "i", "sh" to "u")
        val FINALS = mapOf(
            "ai" to "d", "ei" to "w", "ao" to "c", "ou" to "z", "an" to "j", "en" to "f",
            "ang" to "h", "eng" to "g", "ong" to "s", "ia" to "x", "ie" to "p", "iao" to "n",
            "iu" to "q", "ian" to "m", "in" to "b", "iang" to "l", "ing" to "k", "iong" to "s",
            "ua" to "x", "uo" to "o", "uai" to "k", "ui" to "v", "uan" to "r", "un" to "y",
            "uang" to "l", "ue" to "t", "ve" to "t",
        )

        fun flypyCode(syllable: String): String {
            if (syllable.first() in "aoe") {
                // Zero initial: a -> aa, an -> an, ang -> ah
                return when (syllable.length) {
                    1 -> syllable + syllable
                    2 -> syllable
                    else -> syllable.first() + finalCode(syllable)
                }
            }
            val initial = INITIALS.keys.firstOrNull { syllable.startsWith(it) } ?: syllable.take(1)
            return (INITIALS[initial] ?: initial) + finalCode(syllable.removePrefix(initial))
        }

        fun finalCode(final: String): String =
            if (final.length == 1) final else FINALS[final] ?: error("unknown final '$final'")
    }
}
