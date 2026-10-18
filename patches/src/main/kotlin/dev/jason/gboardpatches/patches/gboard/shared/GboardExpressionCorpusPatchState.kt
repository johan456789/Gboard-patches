package dev.jason.gboardpatches.patches.gboard.shared

import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags

internal enum class GboardExpressionCorpusPatchState {
    STOCK,
    CUSTOM_ONLY,
    TAB_ONLY,
    COMPOSED,
}

internal fun MutableMethod.requireGboardExpressionCorpusPatchState():
    GboardExpressionCorpusPatchState {
    check(
        definingClass == EXPRESSION_CORPUS_MANAGER_CLASS &&
            name == EXPRESSION_CORPUS_MANAGER_METHOD &&
            returnType == EXPRESSION_CORPUS_MANAGER_RETURN_TYPE &&
            parameterTypes == EXPRESSION_CORPUS_MANAGER_PARAMETERS,
    ) {
        "Refusing non-target expression corpus method " +
            "$definingClass->$name(${parameterTypes.joinToString("")})$returnType"
    }
    check(accessFlags == EXPECTED_ACCESS_FLAGS) {
        "Unexpected access flags in $EXPRESSION_CORPUS_MANAGER_DESCRIPTOR: $accessFlags"
    }
    val fingerprint = gboardStructuralFingerprint()
    return when (fingerprint) {
        STOCK_FINGERPRINT -> GboardExpressionCorpusPatchState.STOCK
        CUSTOM_ONLY_FINGERPRINT -> GboardExpressionCorpusPatchState.CUSTOM_ONLY
        TAB_ONLY_FINGERPRINT -> GboardExpressionCorpusPatchState.TAB_ONLY
        COMPOSED_FINGERPRINT -> GboardExpressionCorpusPatchState.COMPOSED
        else -> error(
            "Unexpected expression corpus structure in $EXPRESSION_CORPUS_MANAGER_DESCRIPTOR: " +
                fingerprint,
        )
    }
}

private const val EXPRESSION_CORPUS_MANAGER_CLASS = "Lgtv;"
private const val EXPRESSION_CORPUS_MANAGER_METHOD = "a"
private const val EXPRESSION_CORPUS_MANAGER_RETURN_TYPE = "Lwfr;"
private val EXPRESSION_CORPUS_MANAGER_PARAMETERS =
    listOf("Landroid/view/inputmethod/EditorInfo;", "Z")
private const val EXPRESSION_CORPUS_MANAGER_DESCRIPTOR =
    "Lgtv;->a(Landroid/view/inputmethod/EditorInfo;Z)Lwfr;"
private val EXPECTED_ACCESS_FLAGS = AccessFlags.PUBLIC.value or AccessFlags.FINAL.value
private const val STOCK_FINGERPRINT =
    "086e3ab57b3470afcdc43b3394114d2b8294ca818ca0863612b3524d3b40e89b"
private const val CUSTOM_ONLY_FINGERPRINT =
    "b2f68a0b6bec5e211f7c7f95069c5ccd6a45308eee61cf55f4d409768f56f0a1"
private const val TAB_ONLY_FINGERPRINT =
    "2ba3ab83e57c753d93544d919439e07ea16f04b1f6beb9e6022cbb1dd4d0d782"
private const val COMPOSED_FINGERPRINT =
    "ca4083859038f75ddadd281d4a389dff842c6a7399764d9f5dfcaeb17b292c61"
