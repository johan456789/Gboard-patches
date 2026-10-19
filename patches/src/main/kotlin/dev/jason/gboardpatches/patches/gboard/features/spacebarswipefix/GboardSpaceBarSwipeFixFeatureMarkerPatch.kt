package dev.jason.gboardpatches.patches.gboard.features.spacebarswipefix

import app.morphe.patcher.patch.resourcePatch
import dev.jason.gboardpatches.patches.gboard.features.featureflags.applyFeatureMarker
import dev.jason.gboardpatches.patches.shared.Constants.COMPATIBILITY_GBOARD

internal val gboardSpaceBarSwipeFixFeatureMarkerPatch = resourcePatch(
    description = "標記 Space Bar Swipe Fix feature 已被打入 target APK。",
) {
    compatibleWith(COMPATIBILITY_GBOARD)

    finalize {
        applyFeatureMarker(SPACE_BAR_SWIPE_FIX_FEATURE_MARKER)
    }
}

internal const val SPACE_BAR_SWIPE_FIX_FEATURE_MARKER =
    "dev.jason.gboardpatches.feature.space_bar_swipe_fix"
