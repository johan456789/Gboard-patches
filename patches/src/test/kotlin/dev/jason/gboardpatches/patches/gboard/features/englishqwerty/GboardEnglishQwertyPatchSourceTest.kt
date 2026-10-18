package dev.jason.gboardpatches.patches.gboard.features.englishqwerty

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GboardEnglishQwertyPatchSourceTest {
    @Test
    fun softKeyPatchUses1803ActionModelAndOnlyTheTargetEnglishKeyIds() {
        val source = readSoftKeySource()

        assertTrue(source.contains("SoftKeyDef;->d:I"))
        assertTrue(source.contains("SoftKeyDef;->g:[Ljava/lang/CharSequence;"))
        assertTrue(source.contains("SoftKeyDef;->g(Lpti;)Lcom/google/android/libraries/inputmethod/metadata/ActionDef;"))
        assertTrue(source.contains("ActionDef;->d:[Lpue;"))
        assertTrue(source.contains("Lpue;->e:Ljava/lang/Object;"))
        assertTrue(source.contains("Lptj;->a:Lpti;"))
        assertTrue(source.contains("Lptj;->c:[Ljava/lang/String;"))
        assertTrue(source.contains("Lptj;->q(ILpud;Ljava/lang/Object;)V"))
        assertTrue(source.contains("Lptj;->c()Lcom/google/android/libraries/inputmethod/metadata/ActionDef;"))
        assertTrue(source.contains("Lpvz;->k(Lcom/google/android/libraries/inputmethod/metadata/SoftKeyDef;)V"))
        assertTrue(source.contains("Lpvz;->u(Lcom/google/android/libraries/inputmethod/metadata/ActionDef;)V"))
        assertTrue(source.contains("Lpvz;->d()Ljava/lang/Object;"))

        val admittedIds = Regex("""(?m)^\s*(0x[0-9a-f]+) -> :sswitch_0$""")
            .findAll(source)
            .map { match -> match.groupValues[1] }
            .toSet()
        assertEquals(TARGET_ENGLISH_QWERTY_KEY_IDS, admittedIds)
        assertFalse(admittedIds.contains("0x7f0b19aa"))
        assertFalse(admittedIds.contains("0x7f0b1899"))

        listOf("Lots;", "Lofw;", "Lodk;", "Lodl;", "Lodn;", "Loef;")
            .forEach { legacyDescriptor -> assertFalse(source.contains(legacyDescriptor)) }
        assertFalse(source.contains("0x7f0b0607"))
    }

    @Test
    fun softKeyPatchPreservesDisabledAndExistingNativeSlideUpMetadata() {
        val source = readSoftKeySource()

        assertTrue(source.contains("ENGLISH_UPPERCASE_TOGGLE_RUNTIME_IS_ENABLED"))
        assertTrue(source.contains("ENGLISH_UPPERCASE_TOGGLE_RUNTIME_IS_PATCHED_METADATA"))
        assertTrue(source.contains("ENGLISH_UPPERCASE_TOGGLE_RUNTIME_GET_CACHED_PATCHED_METADATA"))
        assertTrue(source.contains("ENGLISH_UPPERCASE_TOGGLE_RUNTIME_CACHE_PATCHED_METADATA"))
        assertFalse(source.contains("->markPatchedMetadata(Ljava/lang/Object;)V"))
        assertTrue(source.contains("sget-object v0, Lpti;->c:Lpti;"))
        assertTrue(source.contains(
            "invoke-virtual {p1, v0}, Lcom/google/android/libraries/inputmethod/metadata/" +
                "SoftKeyDef;->g(Lpti;)Lcom/google/android/libraries/inputmethod/metadata/ActionDef;"
        ))
        assertFalse(source.contains("jasondevStripSlideUpAction"))
        assertFalse(source.contains("Ljava/util/EnumMap;->remove"))
    }

    @Test
    fun softKeyCacheRunsAfterEnabledAndNativeGuardsAndBeforeCloning() {
        val source = readSoftKeySource()
        val patchBody = tripleQuotedValue(source, "PATCH_INCOMING_METADATA_BODY")
        val enabledIndex = patchBody.indexOf("ENGLISH_UPPERCASE_TOGGLE_RUNTIME_IS_ENABLED")
        val nativeLookupIndex = patchBody.indexOf(
            "invoke-virtual {p1, v0}, Lcom/google/android/libraries/inputmethod/metadata/" +
                "SoftKeyDef;->g(Lpti;)Lcom/google/android/libraries/inputmethod/metadata/ActionDef;"
        )
        val cacheGetIndex = patchBody.indexOf("ENGLISH_UPPERCASE_TOGGLE_RUNTIME_GET_CACHED_PATCHED_METADATA")
        val builderIndex = patchBody.indexOf("new-instance v4, Lpvz;")
        val cachePutIndex = patchBody.indexOf("ENGLISH_UPPERCASE_TOGGLE_RUNTIME_CACHE_PATCHED_METADATA")

        assertTrue(enabledIndex >= 0)
        assertTrue(enabledIndex < nativeLookupIndex)
        assertTrue(nativeLookupIndex < cacheGetIndex)
        assertTrue(cacheGetIndex < builderIndex)
        assertTrue(builderIndex < cachePutIndex)
        assertTrue(patchBody.contains(
            "check-cast v0, Lcom/google/android/libraries/inputmethod/metadata/SoftKeyDef;"
        ))
        assertTrue(patchBody.contains(":catch_0\n    return-object p1"))
    }

    @Test
    fun softKeyPatchOnlyBuildsAsciiCaseToggleWithoutEnglishHintOrSlideDown() {
        val source = readSoftKeySource()

        assertTrue(source.contains("jasondevToggleAsciiCase"))
        assertTrue(source.contains("sget-object v6, Lpti;->c:Lpti;"))
        assertFalse(source.contains("jasondevResolveEnglishSlideDown"))
        assertFalse(source.contains("jasondevSyncSyntheticEnglishHint"))
        assertFalse(source.contains("jasondevSyncEnglishHintView"))
        assertFalse(source.contains("Lpti;->d:Lpti;"))
        assertFalse(source.contains("slidedown_data"))
    }

    @Test
    fun softKeyPatchFailsClosedAndReturnsOriginalMetadataOnRuntimeError() {
        val source = readSoftKeySource()

        assertTrue(source.contains(".catch Ljava/lang/Throwable;"))
        assertTrue(source.contains(":catch_0"))
        assertTrue(source.contains(":cond_return_original"))
        assertTrue(source.contains("return-object p1"))
    }

    @Test
    fun softKeyContributionDelegatesOrderingToTheFamilyComposer() {
        val source = readSoftKeySource()

        assertTrue(source.contains("GboardSoftKeyFamilyFeature.ENGLISH_QWERTY"))
        assertTrue(source.contains("gboardSoftKeyFamilyFeaturePatch"))
        assertFalse(source.contains("topRowDelegateCallIndex"))
        assertFalse(source.contains("mutableMethod.addInstructions"))
    }

    private fun readSoftKeySource(): String =
        readSource(
            "src/main/kotlin/dev/jason/gboardpatches/patches/gboard/features/englishqwerty/" +
                "GboardEnglishQwertySoftKeyPatch.kt"
        )

    private fun readSource(relativePath: String): String =
        String(Files.readAllBytes(Path.of(relativePath)), StandardCharsets.UTF_8)

    private fun tripleQuotedValue(source: String, name: String): String =
        Regex(
            "(?:private|internal) val $name = " + "\"\"\"" + "(.*?)" + "\"\"\"",
            setOf(RegexOption.DOT_MATCHES_ALL)
        ).find(source)?.groupValues?.get(1) ?: error("$name body not found")

    private companion object {
        val TARGET_ENGLISH_QWERTY_KEY_IDS = setOf(
            "0x7f0b198c", "0x7f0b1875", "0x7f0b19fc", "0x7f0b18a3",
            "0x7f0b1903", "0x7f0b1811", "0x7f0b1990", "0x7f0b1876",
            "0x7f0b19c3", "0x7f0b1884", "0x7f0b1a04", "0x7f0b18a7",
            "0x7f0b19e4", "0x7f0b188e", "0x7f0b1932", "0x7f0b1836",
            "0x7f0b196f", "0x7f0b185e", "0x7f0b1985", "0x7f0b1873",
            "0x7f0b18d2", "0x7f0b17f5", "0x7f0b199b", "0x7f0b187b",
            "0x7f0b18f5", "0x7f0b180d", "0x7f0b1919", "0x7f0b1824",
            "0x7f0b191d", "0x7f0b1826", "0x7f0b192c", "0x7f0b1831",
            "0x7f0b1943", "0x7f0b1847", "0x7f0b1947", "0x7f0b1849",
            "0x7f0b194f", "0x7f0b184d", "0x7f0b1a0b", "0x7f0b18ab",
            "0x7f0b1a01", "0x7f0b18a6", "0x7f0b18e7", "0x7f0b1806",
            "0x7f0b19f9", "0x7f0b18a1", "0x7f0b18e2", "0x7f0b1804",
            "0x7f0b1963", "0x7f0b1855", "0x7f0b195c", "0x7f0b1851"
        )
    }
}
