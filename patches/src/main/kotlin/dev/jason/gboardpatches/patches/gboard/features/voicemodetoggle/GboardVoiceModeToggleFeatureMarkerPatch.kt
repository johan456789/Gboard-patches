package dev.jason.gboardpatches.patches.gboard.features.voicemodetoggle

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardVoiceModeToggleFeatureMarkerPatch = resourcePatch(
    description = "標記 Voice Mode Toggle feature 已被打入 target APK。",
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(VOICE_MODE_TOGGLE_FEATURE_MARKER)
    }
}

internal const val VOICE_MODE_TOGGLE_FEATURE_MARKER =
    "dev.jason.gboardpatches.feature.voice_mode_toggle"
