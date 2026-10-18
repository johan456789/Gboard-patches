package dev.jason.gboardpatches.patches.gboard.features.editingaccesspoints

import app.morphe.patcher.patch.BytecodePatchContext
import com.android.tools.smali.dexlib2.AccessFlags
import dev.jason.gboardpatches.patches.gboard.shared.GboardFieldTarget
import dev.jason.gboardpatches.patches.gboard.shared.GboardMethodTarget
import dev.jason.gboardpatches.patches.gboard.shared.mutableClass

/** Reviewed target shape. No editor policy belongs in this version adapter. */
internal object GboardEditingAccessPoints1803Targets {
    private const val OWNER = "Lpbb;"
    val startView = lifecycle("onStartInputView", "Landroid/view/inputmethod/EditorInfo;", "Z")
    val resetMethods = listOf(
        startView,
        lifecycle("onStartInput", "Landroid/view/inputmethod/EditorInfo;", "Z"),
        lifecycle("onFinishInputView", "Z"),
        lifecycle("onFinishInput"),
        lifecycle("onWindowHidden"),
        lifecycle("onDestroy"),
    )
    private fun lifecycle(name: String, vararg parameters: String) =
        GboardMethodTarget(OWNER, name, parameters.toList(), "V")

    private val descriptorMethods = listOf(
        GboardMethodTarget("Lmng;", "c", emptyList(), "Lmnb;") to true,
        GboardMethodTarget("Lmnb;", "l", listOf("Ljava/lang/String;"), "V") to false,
        GboardMethodTarget("Lmnb;", "i", listOf("I"), "V") to false,
        GboardMethodTarget("Lmnb;", "q", listOf("Ljava/lang/Runnable;"), "V") to false,
        GboardMethodTarget("Lmnb;", "a", emptyList(), "Lmng;") to false,
        GboardMethodTarget("Lmqk;", "g", listOf("Lmng;", "Z"), "V") to false,
        GboardMethodTarget("Lwgz;", "n", listOf("Ljava/util/Collection;"), "Lwgz;") to true,
    )

    fun validate(context: BytecodePatchContext) {
        var owner: String? = OWNER
        val seen = mutableSetOf<String>()
        while (owner != null && owner != "Landroid/inputmethodservice/InputMethodService;" &&
            seen.add(owner)) {
            owner = context.mutableClass(owner).superclass
        }
        check(owner == "Landroid/inputmethodservice/InputMethodService;") {
            "Editing buttons lifecycle owner must extend InputMethodService"
        }
        (descriptorMethods + resetMethods.map { it to false }).forEach { (target, isStatic) ->
            val method = target.resolve(context)
            check(AccessFlags.STATIC.isSet(method.accessFlags) == isStatic) {
                "Unexpected static shape: ${target.reference}"
            }
            check(method.implementation != null) { "Missing body: ${target.reference}" }
        }
        // 18.4.1 builders keep only the label field; the content description was removed.
        listOf("d").forEach { name ->
            val field = GboardFieldTarget("Lmnb;", name, "Ljava/lang/String;").resolve(context)
            check(!AccessFlags.STATIC.isSet(field.accessFlags)) { "Unexpected builder field: $name" }
        }
    }
}
