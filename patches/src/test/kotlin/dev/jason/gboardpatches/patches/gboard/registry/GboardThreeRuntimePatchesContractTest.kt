package dev.jason.gboardpatches.patches.gboard.registry

import dev.jason.gboardpatches.patches.gboard.features.closeproactivesuggestions.gboardCloseProactiveSuggestionsFeatureMarkerPatch
import dev.jason.gboardpatches.patches.gboard.features.closeproactivesuggestions.gboardCloseProactiveSuggestionsFlagValuePatch
import dev.jason.gboardpatches.patches.gboard.features.quickinsert.gboardQuickInsertFeatureMarkerPatch
import dev.jason.gboardpatches.patches.gboard.features.quickinsert.gboardQuickInsertFlagValuePatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GboardThreeRuntimePatchesContractTest {
    @Test
    fun `three features are independently published and enabled by default`() {
        val registrations = GboardPublishedPatchCatalog.morpheRegistrations
        val close = registrations.single { patch ->
            patch.name == "Close Proactive Suggestions"
        }
        val quick = registrations.single { patch -> patch.name == "Quick Insert" }

        assertSame(gboardCloseProactiveSuggestionsPatch, close)
        assertSame(gboardQuickInsertPatch, quick)

        // Gboard 18.4.1 compiles out the stock FlowModeDetector, so Hyperspeed Typing Animation is
        // not admitted on this target and is not published.
        assertFalse(registrations.any { patch ->
            patch.name == "Hyperspeed Typing Animation"
        })

        val generated = generatedPublishedPatches().associateBy { patch ->
            patch["name"].asString
        }
        assertTrue(generated.getValue(close.name!!)["use"].asBoolean)
        assertTrue(generated.getValue(quick.name!!)["use"].asBoolean)
        assertFalse(generated.containsKey("Hyperspeed Typing Animation"))
    }

    @Test
    fun `each public patch owns only its feature marker and runtime contributions`() {
        assertTrue(gboardCloseProactiveSuggestionsPatch.dependencies.any { dependency ->
            dependency === gboardCloseProactiveSuggestionsFeatureMarkerPatch
        })
        assertTrue(gboardCloseProactiveSuggestionsPatch.dependencies.any { dependency ->
            dependency === gboardCloseProactiveSuggestionsFlagValuePatch
        })

        // Hyperspeed Typing Animation is not admitted on Gboard 18.4.1, so its public patch owns
        // no feature marker or runtime contribution wiring on this target.
        assertTrue(gboardFlowModeAnimationPatch.dependencies.isEmpty())

        assertTrue(gboardQuickInsertPatch.dependencies.any { dependency ->
            dependency === gboardQuickInsertFeatureMarkerPatch
        })
        assertTrue(gboardQuickInsertPatch.dependencies.any { dependency ->
            dependency === gboardQuickInsertFlagValuePatch
        })
    }

    @Test
    fun `public descriptions match the requested bilingual product copy`() {
        assertEquals(
            "在主動建議列顯示關閉按鈕\n" +
                "Show a dismiss button in the proactive suggestions bar.",
            gboardCloseProactiveSuggestionsPatch.description,
        )
        assertEquals(
            "持續快速輸入時顯示動畫，並支援所有鍵盤\n" +
                "Show the animation during sustained fast typing with support for all keyboards.",
            gboardFlowModeAnimationPatch.description,
        )
        assertEquals(
            "啟用快速插入面板與工具列入口\n" +
                "Enable the Quick Insert panel and toolbar access point.",
            gboardQuickInsertPatch.description,
        )
    }
}
