package dev.jason.gboardpatches.patches.gboard.features.packagerename

import com.google.gson.JsonParser
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.w3c.dom.Element

class GboardPackageRenameFixtureProvenanceTest {
    @Test
    fun `fixture pins exact apk decoded manifest and normalization hashes`() {
        val provenance = JsonParser.parseString(readText(PROVENANCE_FIXTURE)).asJsonObject

        assertEquals(SOURCE_PACKAGE, provenance.get("targetPackage").asString)
        assertEquals(TARGET_VERSION, provenance.get("targetVersion").asString)
        assertEquals(TARGET_APK_SHA256, provenance.get("targetApkSha256").asString)
        assertEquals(DECODED_MANIFEST_SHA256, provenance.get("decodedManifestSha256").asString)
        assertEquals(MANIFEST_FIXTURE_SHA256, provenance.get("manifestFixtureSha256").asString)
        assertEquals(15, provenance.get("selectedManifestAttributeCount").asInt)
        assertEquals(
            "UTF-8 without BOM; CRLF normalized to LF; XML content otherwise verbatim",
            provenance.get("manifestFixtureNormalization").asString,
        )
        assertEquals(
            MANIFEST_FIXTURE_SHA256,
            sha256(normalizeLineEndings(readBytes(MANIFEST_FIXTURE))),
        )
    }

    @Test
    fun `normalized target manifest independently contains exactly fifteen stock package attributes`() {
        val manifestBytes = readBytes(MANIFEST_FIXTURE)
        assertFalse(
            manifestBytes.size >= 3 &&
                manifestBytes[0] == 0xEF.toByte() &&
                manifestBytes[1] == 0xBB.toByte() &&
                manifestBytes[2] == 0xBF.toByte(),
        )
        val manifest = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }.newDocumentBuilder().parse(ByteArrayInputStream(manifestBytes))
        val selected = manifest.getElementsByTagName("*").asList()
            .filterIsInstance<Element>()
            .flatMap { element -> element.attributes.asList().map { element to it } }
            .filter { (_, attribute) -> attribute.nodeValue.contains(SOURCE_PACKAGE) }

        assertEquals(15, selected.size)
        assertEquals(SOURCE_PACKAGE, manifest.documentElement.getAttribute("package"))
        assertTrue(selected.any { (element, attribute) ->
            element.tagName == "manifest" &&
                attribute.nodeName == "package" &&
                attribute.nodeValue == SOURCE_PACKAGE
        })
    }

    @Test
    fun `retained literal evidence pins all three decoded resource sources`() {
        val provenance = JsonParser.parseString(readText(PROVENANCE_FIXTURE)).asJsonObject
        val resources = provenance.getAsJsonArray("resourceSources").map { it.asJsonObject }
            .associate { resource ->
                resource.get("path").asString to resource.get("sha256").asString
            }

        assertEquals(
            mapOf(
                "res/values/arrays.xml" to
                    "85F20A3B71530537C3CD93C7DD1F921B6DB9EFB4DB26C0739400663C42536F61",
                "res/values/strings.xml" to
                    "62C68DCC621E695FA0482D99C981CEF2D6BA48CF0B30C64B4236AE54A976CC5A",
                "res/xml/_0_resource_name_obfuscated_res_0x7f170022.xml" to
                    "EDE5CC6D50476B326A02E4E7DF1FD509AECDE5FE8590919B86490358B5A91845",
            ),
            resources,
        )
        val literals = readText(RETAINED_LITERALS_FIXTURE)
        assertEquals(4, Regex(Regex.escape(SOURCE_PACKAGE)).findAll(literals).count())
        assertTrue(literals.contains("market://details?id=$SOURCE_PACKAGE"))
        assertTrue(literals.contains("http://play.google.com/store/apps/details?id=$SOURCE_PACKAGE"))
        assertTrue(literals.contains("phenotype-configuration-package"))
    }

    @Test
    fun `settings fixtures pin both official 1841 settings resources`() {
        val provenance = JsonParser.parseString(readText(PROVENANCE_FIXTURE)).asJsonObject
        val resources = provenance.getAsJsonArray("settingsSources").map { it.asJsonObject }
            .associate { resource ->
                resource.get("path").asString to resource.get("sha256").asString
            }

        assertEquals(
            mapOf(
                "res/xml/settings.xml" to
                    "E82ECBD2BC4EC46915F6F82ACE53AA468EE23AD691FC5C470E4A4FCF71413A55",
                "res/xml/settings_legacy.xml" to
                    "79A03A1581DF3F216B76289B91A121E9AB4767A8738758D12757E1B63CE6EE95",
            ),
            resources,
        )
        assertEquals(
            "E82ECBD2BC4EC46915F6F82ACE53AA468EE23AD691FC5C470E4A4FCF71413A55",
            sha256(normalizeLineEndings(readBytes(SETTINGS_FIXTURE))),
        )
        assertEquals(
            "79A03A1581DF3F216B76289B91A121E9AB4767A8738758D12757E1B63CE6EE95",
            sha256(normalizeLineEndings(readBytes(SETTINGS_LEGACY_FIXTURE))),
        )
    }

    @Test
    fun `all four split manifests stay on the official package and require standalone renamed output`() {
        val inventory = JsonParser.parseString(readText(SPLIT_MANIFEST_INVENTORY)).asJsonObject
        val apks = inventory.getAsJsonArray("apks").map { it.asJsonObject }

        assertEquals(BUNDLE_VERSION, inventory.get("targetVersion").asString)
        assertEquals(BUNDLE_VERSION_CODE, inventory.get("versionCode").asInt)
        assertEquals(SOURCE_PACKAGE, inventory.get("sourcePackage").asString)
        assertEquals(4, apks.size)
        assertEquals(1, apks.count { it.get("split").isJsonNull })
        assertEquals(3, apks.count { !it.get("split").isJsonNull })
        assertEquals(2, apks.count { it.get("isFeatureSplit").asBoolean })
        assertTrue(apks.all { it.get("package").asString == SOURCE_PACKAGE })
        assertTrue(
            apks.all { apk ->
                apk.get("source").asString == "$BUNDLE_ARTIFACT:${apk.get("name").asString}"
            },
        )
        assertEquals(
            EXPECTED_APK_AND_MANIFEST_HASHES,
            apks.associate { apk ->
                apk.get("name").asString to listOf(
                    apk.get("apkSha256").asString,
                    apk.get("binaryManifestSha256").asString,
                )
            },
        )
        val policy = inventory.get("packageRenameArtifactPolicy").asString
        assertTrue(policy.contains("only the decoded base AndroidManifest.xml"))
        assertTrue(policy.contains("standalone APK"))
        assertTrue(policy.contains("must not be installed with the original-package split APKs"))
    }

    private fun readText(path: String): String =
        String(readBytes(path), StandardCharsets.UTF_8)

    private fun readBytes(path: String): ByteArray =
        checkNotNull(javaClass.getResourceAsStream(path)) {
            "Missing fixture $path"
        }.use { it.readBytes() }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(separator = "") { byte -> "%02X".format(byte) }

    private fun normalizeLineEndings(bytes: ByteArray): ByteArray =
        String(bytes, StandardCharsets.UTF_8)
            .replace("\r\n", "\n")
            .toByteArray(StandardCharsets.UTF_8)

    private fun org.w3c.dom.NamedNodeMap.asList(): List<org.w3c.dom.Node> =
        (0 until length).map(::item)

    private fun org.w3c.dom.NodeList.asList(): List<org.w3c.dom.Node> =
        (0 until length).map(::item)

    private companion object {
        const val SOURCE_PACKAGE = "com.google.android.inputmethod.latin"
        const val TARGET_VERSION = "18.4.1.985164140-release-arm64-v8a"
        const val TARGET_APK_SHA256 =
            "797652F9BF89BB0BF2015A270E715E7BF5F89150891FC74F7AD40C163D3DAC75"
        const val DECODED_MANIFEST_SHA256 =
            "1E93467AC1C0A4F8648CEC74342D07F63E48D7B5AF2C32E044FF5A18BFFFDA7B"
        const val MANIFEST_FIXTURE_SHA256 =
            "1E93467AC1C0A4F8648CEC74342D07F63E48D7B5AF2C32E044FF5A18BFFFDA7B"
        const val BUNDLE_VERSION = "18.4.1.985164140-release-x86_64"
        const val BUNDLE_VERSION_CODE = 176027080
        const val BUNDLE_ARTIFACT = "gboard-18.4.1-x86_64.apkm"
        const val PROVENANCE_FIXTURE =
            "/gboard/18.4.1/package-rename/provenance.json"
        const val MANIFEST_FIXTURE =
            "/gboard/18.4.1/package-rename/AndroidManifest.normalized.xml"
        const val RETAINED_LITERALS_FIXTURE =
            "/gboard/18.4.1/package-rename/retained-resource-literals.xml"
        const val SETTINGS_FIXTURE =
            "/gboard/18.4.1/package-rename/settings.xml"
        const val SETTINGS_LEGACY_FIXTURE =
            "/gboard/18.4.1/package-rename/settings_legacy.xml"
        const val SPLIT_MANIFEST_INVENTORY =
            "/gboard/18.4.1/package-rename/split-manifest-inventory.json"
        val EXPECTED_APK_AND_MANIFEST_HASHES = mapOf(
            "base.apk" to listOf(
                "738EC545D5311B0BCC4332237F8F128816F0DF1781F60BA0C642867FC3BF1B24",
                "65A28EA2A2EE2FA8B519AB905F5A1E11C4706E618F5CDCB02AD0D951CA48D0F7",
            ),
            "split_config.xxhdpi.apk" to listOf(
                "C363A196A0094DDAB91F2119F5C3831CB0F340B58601F9BC29E218448F1D8078",
                "7758EA3153EDC6FA63538978A2935C55BF2F24AD861BF7532E025BC24A0B1D49",
            ),
            "split_brella_feature_split.apk" to listOf(
                "167A979925CEA3ACC92A1F32E84C30F12B9387E133056090A5BDE1DE03D499F8",
                "5C264D94D1ACF097455C021010D84C9EFC2E66937D8A3DCA1D2B2FDAF7412FD2",
            ),
            "split_tenoranimation_feature_split.apk" to listOf(
                "32383E568A6470F6E9B5EEC7881EC42EDE5FAF694D3E7B98CC4302CF6558F8EA",
                "D1FC42502F4E941F80E8FC422A414E7B06F64DA79606964DA43987E084C4BF46",
            ),
        )
    }
}
