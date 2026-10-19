package dev.jason.gboardpatches.patches.gboard.registry

import dev.jason.gboardpatches.patches.gboard.features.spacebarswipefix.SPACE_BAR_SWIPE_FIX_FEATURE_MARKER
import dev.jason.gboardpatches.patches.gboard.features.spacebarswipefix.gboardSpaceBarSwipeFixFeatureMarkerPatch
import dev.jason.gboardpatches.patches.gboard.features.spacebarswipefix.gboardSpaceBarSwipeFixFlagValuePatch
import dev.jason.gboardpatches.patches.gboard.shared.GboardFlagFamilyFeature
import dev.jason.gboardpatches.patches.gboard.shared.gboardPatchesSettingsPatch
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeAbiCatalog
import dev.jason.gboardpatches.patches.gboard.shared.runtimeabi.RuntimeCallId
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GboardSpaceBarSwipeFixPatchContractTest {
    @Test
    fun `public patch is independently published and enabled by default`() {
        val patch = GboardPublishedPatchCatalog.morpheRegistrations.single { candidate ->
            candidate.name == "Space Bar Swipe Fix"
        }

        assertSame(gboardSpaceBarSwipeFixPatch, patch)
        assertTrue(patch.default)
        assertEquals(
            "還原 Google 的 key_edge_distance_threshold，讓空白鍵滑動移動游標而非觸發滑動輸入\n" +
                "Restore Google's key_edge_distance_threshold so swipes on the space bar " +
                "move the cursor instead of triggering glide typing.",
            patch.description,
        )
        assertTrue(patch.compatibility!!.single().targets.single().version ==
            "18.4.1.985164140-release-arm64-v8a")
    }

    @Test
    fun `public patch owns its settings marker and flag contribution`() {
        assertTrue(gboardSpaceBarSwipeFixPatch.dependencies.any { dependency ->
            dependency === gboardPatchesSettingsPatch
        })
        assertTrue(gboardSpaceBarSwipeFixPatch.dependencies.any { dependency ->
            dependency === gboardSpaceBarSwipeFixFeatureMarkerPatch
        })
        assertTrue(gboardSpaceBarSwipeFixPatch.dependencies.any { dependency ->
            dependency === gboardSpaceBarSwipeFixFlagValuePatch
        })
        assertEquals(
            "dev.jason.gboardpatches.feature.space_bar_swipe_fix",
            SPACE_BAR_SWIPE_FIX_FEATURE_MARKER,
        )
    }

    @Test
    fun `flag contribution reuses the version bound flag getter and name field`() {
        val abi = RuntimeAbiCatalog.abi(
            RuntimeCallId.SPACE_BAR_SWIPE_FIX_RUNTIME_APPLY_FLAG_VALUE,
        )
        assertEquals(
            "Ldev/jason/gboardpatches/extension/spacebarswipefix/" +
                "GboardSpaceBarSwipeFixRuntime;",
            abi.owner,
        )
        assertEquals(
            listOf("Ljava/lang/String;", "Ljava/lang/Object;"),
            abi.parameters,
        )
        assertEquals("Ljava/lang/Object;", abi.returnType)

        val featureSpec = GboardFlagFamilyFeature.SPACE_BAR_SWIPE_FIX.spec
        assertEquals(1400, featureSpec.feature.compositionOrder)
        assertEquals(
            GboardFlagFamilyFeature.SPACE_BAR_SWIPE_FIX.runtimeCall,
            featureSpec.runtimeCall,
        )
    }

    @Test
    fun `extension policy owns the exact stock flag name literal`() {
        val policySource = readExtensionSource(
            "spacebarswipefix/GboardSpaceBarSwipeFixPolicy.java",
        )
        assertTrue(policySource.contains("\"key_edge_distance_threshold\""))
    }

    private fun readExtensionSource(relativePath: String): String = Files.readString(
        repositoryRoot().resolve(
            "extensions/extension/src/main/java/dev/jason/gboardpatches/extension/$relativePath",
        ),
        StandardCharsets.UTF_8,
    )

    private fun repositoryRoot(): Path {
        val workingDirectory = Path.of("").toAbsolutePath().normalize()
        return generateSequence(workingDirectory) { directory -> directory.parent }
            .firstOrNull { candidate -> Files.isRegularFile(candidate.resolve("settings.gradle.kts")) }
            ?: error("Could not locate repository root from $workingDirectory")
    }
}
