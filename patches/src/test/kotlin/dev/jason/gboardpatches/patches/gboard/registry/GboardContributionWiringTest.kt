package dev.jason.gboardpatches.patches.gboard.registry

import com.google.gson.JsonParser
import dev.jason.gboardpatches.patches.gboard.shared.generated.GboardTargetAdmission
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class GboardContributionWiringTest {
    @Test
    fun `typed wiring covers every multi-contribution catalog feature`() {
        val catalog = JsonParser.parseString(
            Files.readString(
                Path.of("src/main/resources/gboard/gboard-port-product-catalog.json"),
            ),
        ).asJsonObject
        val expected = catalog.getAsJsonArray("features")
            .map { feature -> feature.asJsonObject }
            .filter { feature -> feature.getAsJsonArray("contributions").size() > 1 }
            .filter { feature -> feature.get("feature_id").asString !in UNSUPPORTED_FEATURE_IDS }
            .associate { feature ->
                val featureId = feature.get("feature_id").asString
                featureId to GboardTargetAdmission.admittedContributionIdsByFeature
                    .getValue(featureId)
            }

        assertEquals(expected, GboardContributionWiring.registeredContributionIdsByFeature)
        UNSUPPORTED_FEATURE_IDS.forEach { featureId ->
            assertFalse(
                GboardTargetAdmission.admittedContributionIdsByFeature.containsKey(featureId),
            )
            assertFalse(
                GboardContributionWiring.registeredContributionIdsByFeature
                    .containsKey(featureId),
            )
        }
    }

    @Test
    fun `independent execution units can be admitted without sibling mutations`() {
        assertEquals(
            setOf("zhuyin_slide_input.soft_key"),
            GboardContributionWiring.activeContributionIds(
                "zhuyin_slide_input",
                setOf("zhuyin_slide_input.soft_key"),
            ),
        )
        assertEquals(
            setOf("long_press_editing_shortcuts.pointer"),
            GboardContributionWiring.activeContributionIds(
                "long_press_editing_shortcuts",
                setOf("long_press_editing_shortcuts.pointer"),
            ),
        )
    }

    @Test
    fun `indivisible shared patch stages fail closed when partially admitted`() {
        assertThrows(IllegalStateException::class.java) {
            GboardContributionWiring.activeContributionIds(
                "swipeable_custom_top_row",
                setOf("swipeable_custom_top_row.soft_key"),
            )
        }
    }

    private companion object {
        // Gboard 18.4.1 compiles the stock FlowModeDetector out, so Hyperspeed Typing
        // Animation is catalogued but never admitted or wired on this target.
        val UNSUPPORTED_FEATURE_IDS = setOf("flow_mode_animation")
    }
}
