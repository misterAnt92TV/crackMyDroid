package com.crackmydroid.shared.presentation

import com.crackmydroid.shared.domain.model.InstalledAppEntry
import com.crackmydroid.shared.domain.model.apkcontent.ApkArchiveMetadata
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.ui.apkcontent.AnalyzeApkContentState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AnalyzeApkContentStateTest {

    private val appA = InstalledAppEntry("Bank App", "com.bank.app", "/data/app/bank.apk")
    private val appB = InstalledAppEntry("Social Network", "com.social.network", "/data/app/social.apk")
    private val appC = InstalledAppEntry("Secure Vault", "org.secure.vault", "/data/app/vault.apk")
    private val sampleApps = listOf(appA, appB, appC)

    private val sampleAnalysis = ApkContentAnalysis(
        packageName = "com.bank.app",
        metadata = ApkArchiveMetadata(
            sha256 = "12345678",
            archiveBytes = 5_000_000L,
            entryCount = 800,
            uncompressedBytes = 12_000_000L,
            dexCount = 1,
            nativeLibraries = emptyList(),
            certificateEntries = emptyList(),
        ),
        libraries = emptyList(),
        findings = emptyList(),
        structuralEntries = emptyList(),
    )

    @Test
    fun step_isOne_whenNoPackageSelectedAndNoAnalysis() {
        val state = AnalyzeApkContentState(apps = sampleApps, selectedPackage = null, analysis = null)
        assertEquals(1, state.step)
    }

    @Test
    fun step_isTwo_whenPackageIsSelectedButAnalysisIsNull() {
        val state = AnalyzeApkContentState(apps = sampleApps, selectedPackage = "com.bank.app", analysis = null)
        assertEquals(2, state.step)
    }

    @Test
    fun step_isThree_whenAnalysisIsPresent() {
        val state = AnalyzeApkContentState(
            apps = sampleApps,
            selectedPackage = "com.bank.app",
            analysis = sampleAnalysis,
        )
        assertEquals(3, state.step)
    }

    @Test
    fun filteredApps_returnsAllApps_whenQueryIsBlank() {
        val state = AnalyzeApkContentState(apps = sampleApps, query = "   ")
        assertEquals(3, state.filteredApps.size)
    }

    @Test
    fun filteredApps_filtersByLabelCaseInsensitive() {
        val state = AnalyzeApkContentState(apps = sampleApps, query = "bank")
        assertEquals(listOf(appA), state.filteredApps)

        val stateUpper = AnalyzeApkContentState(apps = sampleApps, query = "VAULT")
        assertEquals(listOf(appC), stateUpper.filteredApps)
    }

    @Test
    fun filteredApps_filtersByPackageNameCaseInsensitive() {
        val state = AnalyzeApkContentState(apps = sampleApps, query = "social")
        assertEquals(listOf(appB), state.filteredApps)

        val stateDomain = AnalyzeApkContentState(apps = sampleApps, query = "org.secure")
        assertEquals(listOf(appC), stateDomain.filteredApps)
    }

    @Test
    fun filteredApps_returnsEmpty_whenNoMatch() {
        val state = AnalyzeApkContentState(apps = sampleApps, query = "nonexistent")
        assertEquals(emptyList(), state.filteredApps)
    }

    @Test
    fun selectedApp_returnsMatchingApp_whenSelectedPackageMatches() {
        val state = AnalyzeApkContentState(apps = sampleApps, selectedPackage = "com.social.network")
        assertEquals(appB, state.selectedApp)
    }

    @Test
    fun selectedApp_returnsNull_whenSelectedPackageIsNull() {
        val state = AnalyzeApkContentState(apps = sampleApps, selectedPackage = null)
        assertNull(state.selectedApp)
    }

    @Test
    fun selectedApp_returnsNull_whenSelectedPackageNotFound() {
        val state = AnalyzeApkContentState(apps = sampleApps, selectedPackage = "com.unknown.package")
        assertNull(state.selectedApp)
    }
}
