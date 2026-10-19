package dev.jason.gboardpatches.patches.gboard.features.splitkeyboard

import dev.jason.gboardpatches.patches.gboard.shared.GboardFieldTarget
import dev.jason.gboardpatches.patches.gboard.shared.GboardMethodTarget

internal object GboardSplitKeyboard1803Targets {
    const val ORDER_RETURN_TYPE = "Lwfr;"

    // 18.4.1 merges the access point class into Lsyb;, so the name field is typed Object.
    val accessPointName = GboardFieldTarget(
        classType = "Lsyb;",
        name = "d",
        type = "Ljava/lang/Object;",
    )
    val accessPointState = GboardMethodTarget(
        classType = "Lsyb;",
        name = "e",
        parameterTypes = listOf("I"),
        returnType = "V",
    )
    val accessPointOrder = GboardMethodTarget(
        classType = "Lmpw;",
        name = "c",
        parameterTypes = emptyList(),
        returnType = ORDER_RETURN_TYPE,
    )
    val modeAttach = GboardMethodTarget(
        classType = "Lpki;",
        name = "E",
        parameterTypes = listOf("Lpjf;", "Loyg;", "Lpdi;", "Z"),
        returnType = "V",
    )
    val modeEnvironmentReload = GboardMethodTarget(
        classType = "Lpki;",
        name = "Q",
        parameterTypes = emptyList(),
        returnType = "V",
    )
    val splitAction = GboardMethodTarget(
        classType = "Lpki;",
        name = "W",
        parameterTypes = emptyList(),
        returnType = "V",
    )
}
