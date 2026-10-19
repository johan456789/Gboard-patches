package dev.jason.gboardpatches.patches.gboard.features.writingtools

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GboardAiWritingTools1803AutoFixRoutePatchSourceTest {
    @Test
    fun `formal 1803 routes only auto fix through the stock writing tools v2 builder`() {
        val source = Files.readString(
            repositoryRoot().resolve(
                "patches/src/main/kotlin/dev/jason/gboardpatches/patches/gboard/" +
                    "features/writingtools/GboardAiWritingToolsFlagValuePatch.kt",
            ),
        )

        assertTrue(source.contains("classType = \"Lnzq;\""))
        assertTrue(source.contains("name = \"s\""))
        assertTrue(source.contains("returnType = \"Lwfr;\""))
        assertTrue(source.contains("Laagx;->c:Laagx;"))
        assertTrue(source.contains("Lwfg;-><init>(I)V"))
        assertTrue(source.contains("Lwfm;->g()Lwfr;"))
        assertTrue(source.contains("Lwfm;->h(Ljava/lang/Object;)V"))
        assertTrue(source.contains("builderInitIndices.single() + 1"))
        assertTrue(!source.contains("freezeIndices.single(),"))
        assertTrue(source.contains("classType = \"Lgys;\""))
        assertTrue(source.contains("name = \"a\""))
        assertTrue(source.contains("parameterTypes = listOf(\"Lgzb;\")"))
        assertTrue(source.contains("returnType = \"Lmdk;\""))
        assertTrue(source.contains("Laagx;->c:Laagx;"))
        assertTrue(!source.contains("Lgzb;->f:Z"))
        assertTrue(!source.contains("Lgzb;->g:Laagw;"))
        assertTrue(!source.contains("Laagw;->i:Laagw;"))
        assertTrue(source.contains("Lmdk;->e:Lmdk;"))
        assertTrue(!source.contains("intent_classifier_threshold_for_writing_tools_v2"))
        assertEquals(5, Regex("findMutableMethodOrThrow\\(").findAll(source).count())
    }

    private fun repositoryRoot(): Path {
        val workingDirectory = Path.of("").toAbsolutePath().normalize()
        return generateSequence(workingDirectory) { it.parent }
            .firstOrNull { Files.isRegularFile(it.resolve("settings.gradle.kts")) }
            ?: error("Could not locate repository root from $workingDirectory")
    }
}
