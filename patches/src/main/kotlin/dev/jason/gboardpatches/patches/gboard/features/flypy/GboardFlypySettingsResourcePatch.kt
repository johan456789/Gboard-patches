package dev.jason.gboardpatches.patches.gboard.features.flypy

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.shared.ANDROID_NS
import dev.jason.gboardpatches.patches.gboard.shared.childElements
import dev.jason.gboardpatches.patches.gboard.shared.elements
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD
import org.w3c.dom.Document
import org.w3c.dom.Element

internal val gboardFlypySettingsResourcePatch = resourcePatch(
    description = "在繁拼設定加入拼音方案選項（全拼 / 雙拼 - flyPY）。"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyGboardFlypySettingsXml()
        applyGboardFlypySchemeArrays()
    }
}

context(context: ResourcePatchContext)
private fun applyGboardFlypySettingsXml() = with(context) {
    // Gboard swaps in the second XML when a physical QWERTY keyboard is attached
    // (<if physical_keyboard="qwerty"> in the zh-TW ime definitions), so patch both.
    val paths = FLYPY_SETTINGS_XML_CANDIDATES.mapNotNull { names ->
        names.firstOrNull { candidate -> this[candidate, false].exists() }
    }
    if (paths.isEmpty()) {
        error(
            "Could not find the Traditional Chinese pinyin settings XML; tried " +
                FLYPY_SETTINGS_XML_CANDIDATES,
        )
    }
    paths.forEach { path -> document(path).use { document -> applyGboardFlypySettings(document) } }
}

context(context: ResourcePatchContext)
private fun applyGboardFlypySchemeArrays() = with(context) {
    document(ARRAYS_XML).use { document -> applyGboardFlypySchemeArrays(document) }
}

internal fun applyGboardFlypySettings(document: Document) {
    val screen = document.documentElement
    val alreadyPresent = screen.getElementsByTagName("ListPreference").elements().any { element ->
        element.androidAttribute("key") == PINYIN_SCHEME_KEY_REF
    }
    if (alreadyPresent) {
        return
    }

    val category = document.createElement("androidx.preference.PreferenceCategory")
    category.setAndroidAttribute("title", PINYIN_SETTINGS_TITLE_REF)

    val listPreference = document.createElement("ListPreference")
    listPreference.setAndroidAttribute("entries", ENTRIES_REF)
    listPreference.setAndroidAttribute("title", PINYIN_SCHEME_TITLE_REF)
    listPreference.setAndroidAttribute("key", PINYIN_SCHEME_KEY_REF)
    listPreference.setAndroidAttribute("summary", "%s")
    listPreference.setAndroidAttribute("dialogTitle", PINYIN_SCHEME_TITLE_REF)
    listPreference.setAndroidAttribute("entryValues", ENTRY_VALUES_REF)
    listPreference.setAndroidAttribute("defaultValue", DEFAULT_SCHEME_VALUE)

    category.appendChild(listPreference)
    screen.appendChild(category)
}

internal fun applyGboardFlypySchemeArrays(document: Document) {
    val root = document.documentElement
    val existingNames = root.childElements().mapNotNull { element ->
        element.getAttribute("name").takeIf(String::isNotEmpty)
    }.toSet()

    if (ENTRIES_ARRAY_NAME !in existingNames) {
        root.appendChild(stringArray(document, ENTRIES_ARRAY_NAME, ENTRY_STRING_REFS))
    }
    if (ENTRY_VALUES_ARRAY_NAME !in existingNames) {
        root.appendChild(stringArray(document, ENTRY_VALUES_ARRAY_NAME, ENTRY_VALUE_STRING_REFS))
    }
}

private fun stringArray(document: Document, name: String, itemRefs: List<String>): Element {
    val array = document.createElement("array")
    array.setAttribute("name", name)
    itemRefs.forEach { ref ->
        val item = document.createElement("item")
        item.textContent = ref
        array.appendChild(item)
    }
    return array
}

private fun Element.setAndroidAttribute(localName: String, value: String) {
    setAttributeNS(ANDROID_NS, "android:$localName", value)
}

private fun Element.androidAttribute(localName: String): String {
    getAttributeNS(ANDROID_NS, localName).takeIf(String::isNotEmpty)?.let { return it }
    getAttribute("android:$localName").takeIf(String::isNotEmpty)?.let { return it }
    for (index in 0 until attributes.length) {
        val attribute = attributes.item(index)
        if (attribute.localName == localName ||
            attribute.nodeName.substringAfterLast(':') == localName
        ) {
            return attribute.nodeValue.orEmpty()
        }
    }
    return ""
}

private const val ARRAYS_XML = "res/values/arrays.xml"
private const val DEFAULT_SCHEME_VALUE = "quanpin"
private const val ENTRIES_ARRAY_NAME = "gboard_flypy_pinyin_scheme_entries"
private const val ENTRY_VALUES_ARRAY_NAME = "gboard_flypy_pinyin_scheme_entry_values"
private const val ENTRIES_REF = "@array/$ENTRIES_ARRAY_NAME"
private const val ENTRY_VALUES_REF = "@array/$ENTRY_VALUES_ARRAY_NAME"

private const val PINYIN_SETTINGS_TITLE_REF = "@string/string_0x7f140c43"
private const val PINYIN_SCHEME_TITLE_REF = "@string/string_0x7f140c42"
private const val PINYIN_SCHEME_KEY_REF = "@string/string_0x7f140a4a"
private const val FULL_PINYIN_REF = "@string/string_0x7f140ae0"
private const val DOUBLE_PINYIN_FLYPY_REF = "@string/string_0x7f140cfc"
private const val QUANPIN_VALUE_REF = "@string/string_0x7f1408e5"
private const val FLYPY_VALUE_REF = "@string/string_0x7f1408e8"

private val ENTRY_STRING_REFS = listOf(FULL_PINYIN_REF, DOUBLE_PINYIN_FLYPY_REF)
private val ENTRY_VALUE_STRING_REFS = listOf(QUANPIN_VALUE_REF, FLYPY_VALUE_REF)

private val FLYPY_SETTINGS_XML_CANDIDATES = listOf(
    listOf(
        "res/xml/xml_0x7f17124a.xml",
        "res/xml/_0_resource_name_obfuscated_res_0x7f17124a.xml",
    ),
    listOf(
        "res/xml/xml_0x7f17124b.xml",
        "res/xml/_0_resource_name_obfuscated_res_0x7f17124b.xml",
    ),
)
