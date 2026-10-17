package dev.jason.gboardpatches.patches.gboard.features.calculator

import dev.jason.gboardpatches.patches.gboard.shared.GboardFieldTarget
import dev.jason.gboardpatches.patches.gboard.shared.GboardMethodTarget

internal object GboardCalculator1803Targets {
    val inputView = GboardFieldTarget(
        classType = "Lpbb;",
        name = "i",
        type = "Lcom/google/android/libraries/inputmethod/inputview/InputView;",
    )
    val onStartInputView = GboardMethodTarget(
        classType = "Lpbb;",
        name = "onStartInputView",
        parameterTypes = listOf("Landroid/view/inputmethod/EditorInfo;", "Z"),
        returnType = "V",
    )
    val onUpdateSelection = GboardMethodTarget(
        classType = "Lpbb;",
        name = "onUpdateSelection",
        parameterTypes = listOf("I", "I", "I", "I", "I", "I"),
        returnType = "V",
    )
    val onWindowHidden = GboardMethodTarget(
        classType = "Lpbb;",
        name = "onWindowHidden",
        parameterTypes = emptyList(),
        returnType = "V",
    )
}
