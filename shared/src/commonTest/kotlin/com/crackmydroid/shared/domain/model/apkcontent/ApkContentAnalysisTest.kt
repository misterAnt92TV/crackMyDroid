package com.crackmydroid.shared.domain.model.apkcontent

import kotlin.test.Test
import kotlin.test.assertEquals

class ApkContentAnalysisTest {

    private val sampleMetadata = ApkArchiveMetadata(
        sha256 = "d2c1e4f201020304",
        archiveBytes = 10_000_000L,
        entryCount = 1_500,
        uncompressedBytes = 25_000_000L,
        dexCount = 2,
        nativeLibraries = listOf("libnative.so"),
        certificateEntries = listOf("META-INF/CERT.RSA"),
    )

    @Test
    fun highestSeverity_returnsInfo_whenFindingsAreEmpty() {
        val analysis = ApkContentAnalysis(
            packageName = "com.test.app",
            metadata = sampleMetadata,
            libraries = emptyList(),
            findings = emptyList(),
            structuralEntries = emptyList(),
        )

        assertEquals(ApkFindingSeverity.Info, analysis.highestSeverity)
    }

    @Test
    fun highestSeverity_returnsInfo_whenOnlyInfoFindingsExist() {
        val analysis = ApkContentAnalysis(
            packageName = "com.test.app",
            metadata = sampleMetadata,
            libraries = emptyList(),
            findings = listOf(
                ApkContentFinding(
                    severity = ApkFindingSeverity.Info,
                    category = "metadata",
                    evidence = "Found manifest",
                    remediation = "None",
                ),
            ),
            structuralEntries = emptyList(),
        )

        assertEquals(ApkFindingSeverity.Info, analysis.highestSeverity)
    }

    @Test
    fun highestSeverity_returnsMedium_whenMediumIsHighest() {
        val analysis = ApkContentAnalysis(
            packageName = "com.test.app",
            metadata = sampleMetadata,
            libraries = emptyList(),
            findings = listOf(
                ApkContentFinding(
                    severity = ApkFindingSeverity.Info,
                    category = "metadata",
                    evidence = "Info",
                    remediation = "None",
                ),
                ApkContentFinding(
                    severity = ApkFindingSeverity.Medium,
                    category = "security",
                    evidence = "Medium risk found",
                    remediation = "Review library",
                ),
            ),
            structuralEntries = emptyList(),
        )

        assertEquals(ApkFindingSeverity.Medium, analysis.highestSeverity)
    }

    @Test
    fun highestSeverity_returnsHigh_whenHighIsHighest() {
        val analysis = ApkContentAnalysis(
            packageName = "com.test.app",
            metadata = sampleMetadata,
            libraries = emptyList(),
            findings = listOf(
                ApkContentFinding(
                    severity = ApkFindingSeverity.Medium,
                    category = "security",
                    evidence = "Medium risk",
                    remediation = "Review",
                ),
                ApkContentFinding(
                    severity = ApkFindingSeverity.High,
                    category = "security",
                    evidence = "Sensitive native library included",
                    remediation = "Remove debug tools",
                ),
            ),
            structuralEntries = emptyList(),
        )

        assertEquals(ApkFindingSeverity.High, analysis.highestSeverity)
    }

    @Test
    fun highestSeverity_returnsCritical_whenCriticalPresent() {
        val analysis = ApkContentAnalysis(
            packageName = "com.test.app",
            metadata = sampleMetadata,
            libraries = emptyList(),
            findings = listOf(
                ApkContentFinding(
                    severity = ApkFindingSeverity.High,
                    category = "security",
                    evidence = "High risk",
                    remediation = "Fix",
                ),
                ApkContentFinding(
                    severity = ApkFindingSeverity.Critical,
                    category = "integrity",
                    evidence = "AndroidManifest.xml missing from APK",
                    remediation = "Corrupt or malformed package",
                ),
            ),
            structuralEntries = emptyList(),
        )

        assertEquals(ApkFindingSeverity.Critical, analysis.highestSeverity)
    }

    @Test
    fun plaintextContent_defaultsToEmptyAndFiltersCategoriesCorrectly() {
        val analysis = ApkContentAnalysis(
            packageName = "com.test.app",
            metadata = sampleMetadata,
            libraries = emptyList(),
            findings = emptyList(),
            structuralEntries = emptyList(),
            plaintextContent = ApkPlaintextContent(
                listOf(
                    ApkPlainTextItem(ApkPlainTextCategory.Endpoint, "https://api.test.com", "classes.dex"),
                    ApkPlainTextItem(ApkPlainTextCategory.ConfigurationKey, "KEY = 123", "assets/app.json"),
                    ApkPlainTextItem(ApkPlainTextCategory.TextString, "Welcome", "assets/strings.json"),
                ),
            ),
        )

        assertEquals(3, analysis.plaintextContent.totalCount)
        assertEquals(1, analysis.plaintextContent.endpoints.size)
        assertEquals(1, analysis.plaintextContent.configKeys.size)
        assertEquals(1, analysis.plaintextContent.textStrings.size)
    }
}
