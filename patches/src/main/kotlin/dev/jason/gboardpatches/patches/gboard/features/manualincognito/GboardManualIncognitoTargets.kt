package dev.jason.gboardpatches.patches.gboard.features.manualincognito

import dev.jason.gboardpatches.patches.gboard.shared.GboardMethodTarget

internal object GboardManualIncognitoTargets {
    val onStartInput = method(
        owner = "Lpbb;",
        name = "onStartInput",
        parameters = listOf("Landroid/view/inputmethod/EditorInfo;", "Z"),
        returns = "V",
    )
    val onStartInputView = method(
        owner = "Lpbb;",
        name = "onStartInputView",
        parameters = listOf("Landroid/view/inputmethod/EditorInfo;", "Z"),
        returns = "V",
    )
    val onWindowHidden = method("Lpbb;", "onWindowHidden", emptyList(), "V")
    val incognitoPredicate = method("Lesi;", "I", emptyList(), "Z")
    val clipboardListener = method("Lfmg;", "onPrimaryClipChanged", emptyList(), "V")
    val voiceEligibility = method(
        owner = "Luwm;",
        name = "cs",
        parameters = listOf(
            "Landroid/content/Context;",
            "Landroid/view/inputmethod/EditorInfo;",
            "Z",
        ),
        returns = "Z",
    )
    val dictationEligibility = method(
        owner = "Lslv;",
        name = "b",
        parameters = listOf("Leon;", "Z", "Z"),
        returns = "Lkrp;",
    )
    private fun method(
        owner: String,
        name: String,
        parameters: List<String>,
        returns: String,
    ) = GboardMethodTarget(owner, name, parameters, returns)
}
