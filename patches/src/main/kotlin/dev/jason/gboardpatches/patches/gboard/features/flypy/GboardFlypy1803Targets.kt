package dev.jason.gboardpatches.patches.gboard.features.flypy

import dev.jason.gboardpatches.patches.gboard.shared.GboardMethodTarget

internal object GboardFlypy1803Targets {
    const val ENGINE_FACTORY = "Lkkb;"
    const val PREFERENCE_MANAGER = "Lqhy;"
    const val PREFERENCE_LISTENER = "Lqhx;"
    const val ENGINE_FACTORY_BASE = "Lhbd;"

    const val SETUP_HELPER_NAME = "gboardPatchesFlypySetup"
    const val ENROLL_HELPER_NAME = "gboardPatchesFlypyEnroll"
    const val PINYIN_SCHEME_PREF_RES_ID = 0x7f140b2e

    val engineFactoryOnCreate = GboardMethodTarget(
        classType = ENGINE_FACTORY,
        name = "c",
        parameterTypes = emptyList(),
        returnType = "V",
    )

    val engineFactoryOnInitialize = GboardMethodTarget(
        classType = ENGINE_FACTORY,
        name = "i",
        parameterTypes = emptyList(),
        returnType = "V",
    )

    val engineFactoryApplySettingScheme = GboardMethodTarget(
        classType = ENGINE_FACTORY,
        name = "q",
        parameterTypes = listOf("I", "Lzta;"),
        returnType = "V",
    )
}
