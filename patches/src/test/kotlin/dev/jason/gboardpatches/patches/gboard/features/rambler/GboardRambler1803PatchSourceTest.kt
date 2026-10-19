package dev.jason.gboardpatches.patches.gboard.features.rambler

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GboardRambler1803PatchSourceTest {
    @Test
    fun `official selector replaces all retired launch fallback and session injections`() {
        val source = Files.readString(
            repositoryRoot().resolve(
                "patches/src/main/kotlin/dev/jason/gboardpatches/patches/gboard/" +
                    "features/rambler/GboardRambler1803OfficialSelectorPatch.kt",
            ),
            StandardCharsets.UTF_8,
        )

        assertTrue(source.contains("VoiceSettingsFragment"))
        assertTrue(source.contains("method(\"Lmvq;\", \"a\""))
        assertTrue(source.contains("method(\"Lfde;\", \"hT\""))
        assertEquals(6, Regex("findMutableMethodOrThrow\\(").findAll(source).count())
        assertFalse(source.contains("ramblerSwitcherLaunch"))
        assertFalse(source.contains("ramblerSwitcherFallback"))
        assertFalse(source.contains("ramblerSessionBuilder"))
        assertFalse(source.contains("Lhpz;"))
        assertFalse(source.contains("Lhoa;"))
        assertFalse(source.contains("Ljrl;"))
        assertFalse(source.contains("Ljrm;"))
    }

    private fun repositoryRoot(): Path {
        val workingDirectory = Path.of("").toAbsolutePath().normalize()
        return generateSequence(workingDirectory) { it.parent }
            .firstOrNull { Files.isRegularFile(it.resolve("settings.gradle.kts")) }
            ?: error("Could not locate repository root from $workingDirectory")
    }
}
