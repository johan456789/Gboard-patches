package dev.jason.gboardpatches.patches.gboard.features.flypy

import dev.jason.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.jason.gboardpatches.patches.gboard.shared.childElements
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Document
import org.w3c.dom.Element

class GboardFlypySettingsResourcePatchTest {
    @Test
    fun `adds the pinyin scheme category and list preference to the traditional chinese screen`() {
        val document = parse(TRADITIONAL_CHINESE_SETTINGS_XML)

        applyGboardFlypySettings(document)

        val children = document.documentElement.childElements().toList()
        assertEquals(3, children.size)
        val category = children.last()
        assertEquals("androidx.preference.PreferenceCategory", category.tagName)
        assertEquals(PINYIN_SETTINGS_TITLE_REF, androidAttribute(category, "title"))

        val listPreference = category.childElements().single()
        assertEquals("ListPreference", listPreference.tagName)
        assertEquals(PINYIN_SCHEME_KEY_REF, androidAttribute(listPreference, "key"))
        assertEquals(PINYIN_SCHEME_TITLE_REF, androidAttribute(listPreference, "title"))
        assertEquals(PINYIN_SCHEME_TITLE_REF, androidAttribute(listPreference, "dialogTitle"))
        assertEquals(ENTRIES_REF, androidAttribute(listPreference, "entries"))
        assertEquals(ENTRY_VALUES_REF, androidAttribute(listPreference, "entryValues"))
        assertEquals("quanpin", androidAttribute(listPreference, "defaultValue"))
    }

    @Test
    fun `settings transformation is idempotent`() {
        val document = parse(TRADITIONAL_CHINESE_SETTINGS_XML)

        applyGboardFlypySettings(document)
        applyGboardFlypySettings(document)

        val children = document.documentElement.childElements().toList()
        assertEquals(3, children.size)
        assertEquals(
            1,
            children.count { element ->
                element.childElements().any { child ->
                    androidAttribute(child, "key") == PINYIN_SCHEME_KEY_REF
                }
            },
        )
    }

    @Test
    fun `adds two entry arrays with full pinyin and flypy options`() {
        val document = parse(ARRAYS_XML)

        applyGboardFlypySchemeArrays(document)
        applyGboardFlypySchemeArrays(document)

        val arrays = document.documentElement.childElements("array").toList()
        assertEquals(3, arrays.size)

        val entries = arrays.single { it.getAttribute("name") == "gboard_flypy_pinyin_scheme_entries" }
        assertEquals(
            listOf(FULL_PINYIN_REF, DOUBLE_PINYIN_FLYPY_REF),
            entries.childElements("item").map { it.textContent },
        )

        val values = arrays.single {
            it.getAttribute("name") == "gboard_flypy_pinyin_scheme_entry_values"
        }
        assertEquals(
            listOf(QUANPIN_VALUE_REF, FLYPY_VALUE_REF),
            values.childElements("item").map { it.textContent },
        )
    }

    @Test
    fun `bundled flypy token dictionary matches the generated traditional chinese payload`() {
        val bytes = object {}.javaClass.classLoader
            .getResourceAsStream("flypy-assets/tokens_shuangpin_flypy")
            ?.use { it.readBytes() }
            ?: error("flyPY token dictionary asset not found")
        assertEquals(9312, bytes.size)
        assertEquals("ForwardTokenDictionary", String(bytes, 4, 22, StandardCharsets.US_ASCII))
    }

    private fun parse(xml: String): Document = DocumentBuilderFactory.newInstance()
        .apply { isNamespaceAware = true }
        .newDocumentBuilder()
        .parse(ByteArrayInputStream(xml.trim().toByteArray(StandardCharsets.UTF_8)))

    private fun androidAttribute(element: Element, localName: String): String {
        element.getAttributeNS(ANDROID_NS, localName).takeIf(String::isNotEmpty)?.let { return it }
        return element.getAttribute("android:$localName")
    }

    private companion object {
        const val TRADITIONAL_CHINESE_SETTINGS_XML = """
<?xml version="1.0" encoding="utf-8"?>
<PreferenceScreen xmlns:android="http://schemas.android.com/apk/res/android">
    <SwitchPreferenceCompat
        android:persistent="true"
        android:title="@string/_0_resource_name_obfuscated_res_0x7f140d83"
        android:key="@string/_0_resource_name_obfuscated_res_0x7f140a1b"/>
    <SwitchPreferenceCompat
        android:persistent="true"
        android:title="@string/_0_resource_name_obfuscated_res_0x7f140cd8"
        android:key="@string/_0_resource_name_obfuscated_res_0x7f1409f1"
        android:summary="@string/_0_resource_name_obfuscated_res_0x7f140cd7"/>
</PreferenceScreen>
"""

        const val ARRAYS_XML = """
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <array name="existing_array">
        <item>1</item>
    </array>
</resources>
"""

        const val PINYIN_SETTINGS_TITLE_REF = "@string/string_0x7f140d41"
        const val PINYIN_SCHEME_TITLE_REF = "@string/string_0x7f140d40"
        const val PINYIN_SCHEME_KEY_REF = "@string/string_0x7f140b2e"
        const val FULL_PINYIN_REF = "@string/string_0x7f140bc4"
        const val DOUBLE_PINYIN_FLYPY_REF = "@string/string_0x7f140dff"
        const val QUANPIN_VALUE_REF = "@string/string_0x7f1409a4"
        const val FLYPY_VALUE_REF = "@string/string_0x7f1409a7"
        const val ENTRIES_REF = "@array/gboard_flypy_pinyin_scheme_entries"
        const val ENTRY_VALUES_REF = "@array/gboard_flypy_pinyin_scheme_entry_values"
    }
}
