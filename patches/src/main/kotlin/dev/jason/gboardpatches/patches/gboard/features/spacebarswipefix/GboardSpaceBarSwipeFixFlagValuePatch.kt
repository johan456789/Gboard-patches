package dev.jason.gboardpatches.patches.gboard.features.spacebarswipefix

import dev.jason.gboardpatches.patches.gboard.shared.GboardFlagFamilyFeature
import dev.jason.gboardpatches.patches.gboard.shared.gboardFlagFamilyFeaturePatch

internal val gboardSpaceBarSwipeFixFlagValuePatch = gboardFlagFamilyFeaturePatch(
    description = "在 key_edge_distance_threshold stock result 後套用 Google 伺服器值。",
    feature = GboardFlagFamilyFeature.SPACE_BAR_SWIPE_FIX,
)
