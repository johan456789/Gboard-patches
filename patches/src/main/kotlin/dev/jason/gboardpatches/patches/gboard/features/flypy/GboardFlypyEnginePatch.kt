package dev.jason.gboardpatches.patches.gboard.features.flypy

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.util.proxy.mutableTypes.MutableClass
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod
import com.android.tools.smali.dexlib2.AccessFlags
import dev.jason.gboardpatches.patches.gboard.shared.addHelperMethodIfMissing
import dev.jason.gboardpatches.patches.gboard.shared.gboardPatchesExtensionCarrierPatch
import dev.jason.gboardpatches.patches.gboard.shared.isMethodReference
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeAbiCatalog
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallEmitter
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallId
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

private val SELECT_DICTIONARY_CALL = RuntimeCallId.FLYPY_RUNTIME_MAYBE_SELECT_TOKEN_DICTIONARY
private val SELECT_DICTIONARY_ABI = RuntimeAbiCatalog.abi(SELECT_DICTIONARY_CALL)

internal val gboardFlypyEnginePatch = bytecodePatch(
    description = "讓繁拼引擎在選擇 flyPY 雙拼時使用 flyPY token dictionary。",
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    dependsOn(gboardPatchesExtensionCarrierPatch)

    execute {
        GboardFlypy1803Targets.engineFactoryOnCreate.ownerClass(this).installFlypyHelpers()

        GboardFlypy1803Targets.engineFactoryOnCreate.resolve(this)
            .applyHelperEntryDelegate(GboardFlypy1803Targets.SETUP_HELPER_NAME)
        GboardFlypy1803Targets.engineFactoryOnInitialize.resolve(this)
            .applyHelperEntryDelegate(GboardFlypy1803Targets.ENROLL_HELPER_NAME)
        GboardFlypy1803Targets.engineFactoryApplySettingScheme.resolve(this).applyEntryDelegate(
            matchReference = SELECT_DICTIONARY_ABI.reference,
            instruction = RuntimeCallEmitter.invoke(SELECT_DICTIONARY_CALL, "p2, p1"),
        )
    }
}

private fun MutableClass.installFlypyHelpers() {
    addHelperMethodIfMissing(
        name = GboardFlypy1803Targets.SETUP_HELPER_NAME,
        parameterTypes = emptyList(),
        returnType = "V",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
        registerCount = SETUP_HELPER_REGISTER_COUNT,
        body = setupHelperBody(),
    )
    addHelperMethodIfMissing(
        name = GboardFlypy1803Targets.ENROLL_HELPER_NAME,
        parameterTypes = emptyList(),
        returnType = "V",
        accessFlags = AccessFlags.PRIVATE.value or AccessFlags.FINAL.value,
        registerCount = ENROLL_HELPER_REGISTER_COUNT,
        body = enrollHelperBody(),
    )
}

private fun MutableMethod.applyHelperEntryDelegate(helperName: String) {
    val reference = "${definingClass}->$helperName()V"
    applyEntryDelegate(reference, "invoke-direct {p0}, $reference")
}

private fun MutableMethod.applyEntryDelegate(matchReference: String, instruction: String) {
    val instructions = implementation?.instructions
        ?: error("No instructions in $definingClass->$name")
    val existing = instructions.count { it.isMethodReference(matchReference) }
    if (existing > 0) {
        check(existing == 1 && instructions[0].isMethodReference(matchReference)) {
            "Malformed flyPY entry delegate in $definingClass->$name"
        }
        return
    }
    addInstructions(0, instruction)
}

private fun setupHelperBody(): String {
    val invoke = RuntimeCallEmitter.invoke(
        RuntimeCallId.FLYPY_RUNTIME_ON_ENGINE_FACTORY_CREATED,
        "p0",
    )
    return """
        $invoke
        return-void
    """.trimIndent()
}

private fun enrollHelperBody(): String {
    val invoke = RuntimeCallEmitter.invoke(
        RuntimeCallId.FLYPY_RUNTIME_ON_ENGINE_FACTORY_INITIALIZED,
        "p0",
    )
    return """
        $invoke
        return-void
    """.trimIndent()
}

private const val SETUP_HELPER_REGISTER_COUNT = 1
private const val ENROLL_HELPER_REGISTER_COUNT = 1
