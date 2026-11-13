package com.crackmydroid.shared.domain.usecase.apkcontent

import com.crackmydroid.shared.domain.model.apkcontent.ApkArchiveMetadata
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.domain.model.apkcontent.ApkContentFinding
import com.crackmydroid.shared.domain.model.apkcontent.ApkFindingSeverity
import com.crackmydroid.shared.domain.repository.ApkContentRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnalyzeApkContentUseCaseTest {
    @Test fun `returns static analysis supplied by platform repository`() = kotlinx.coroutines.test.runTest {
        val expected = ApkContentAnalysis(
            packageName = "com.example.target",
            metadata = ApkArchiveMetadata("hash", 10, 2, 10, 1, emptyList(), emptyList()),
            libraries = emptyList(),
            findings = listOf(ApkContentFinding(ApkFindingSeverity.High, "Native code", "lib/inject.so", "Review it")),
            structuralEntries = listOf("AndroidManifest.xml"),
        )
        val result = AnalyzeApkContentUseCase(FakeRepository(Result.success(expected)))(expected.packageName)
        assertEquals(expected, result.getOrThrow())
        assertEquals(ApkFindingSeverity.High, result.getOrThrow().highestSeverity)
    }

    @Test fun `propagates archive inspection failure`() = kotlinx.coroutines.test.runTest {
        val result = AnalyzeApkContentUseCase(FakeRepository(Result.failure(IllegalArgumentException("Unsafe archive path"))))("pkg")
        assertTrue(result.isFailure)
    }

    private class FakeRepository(private val response: Result<ApkContentAnalysis>) : ApkContentRepository {
        override suspend fun analyze(packageName: String): Result<ApkContentAnalysis> = response
        override suspend fun prepareAndOpenArchive(packageName: String): Result<Unit> = Result.success(Unit)
    }
}
