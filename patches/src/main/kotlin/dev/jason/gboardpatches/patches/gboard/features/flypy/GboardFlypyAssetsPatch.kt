package dev.jason.gboardpatches.patches.gboard.features.flypy

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardFlypyAssetsPatch = resourcePatch(
    description = "複製 flyPY Traditional Chinese token dictionary 到 target APK。"
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize { copyFlypyDictionaryAsset() }
}

context(context: ResourcePatchContext)
private fun copyFlypyDictionaryAsset() = with(context) {
    val loader = object {}.javaClass.classLoader
    val bytes = loader.getResourceAsStream("$RESOURCE_ROOT/$FILE_NAME")?.use { it.readBytes() }
        ?: error("flyPY token dictionary asset not found")
    val target = this["assets/$ASSET_ROOT/$FILE_NAME", false]
    target.parentFile?.mkdirs()
    target.outputStream().use { it.write(bytes) }
}

private const val RESOURCE_ROOT = "flypy-assets"
private const val ASSET_ROOT = "gboard-flypy"
private const val FILE_NAME = "tokens_shuangpin_flypy"
