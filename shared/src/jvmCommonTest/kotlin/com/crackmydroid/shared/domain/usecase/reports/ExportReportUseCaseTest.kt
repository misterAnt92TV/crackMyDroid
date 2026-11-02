package com.crackmydroid.shared.domain.usecase.reports

import com.crackmydroid.shared.domain.repository.ReportFileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportReportUseCaseTest {

    @Test
    fun save_returnsRepositoryResult() = runTest {
        val repository = mockk<ReportFileRepository>()
        val expected = Result.success("/tmp/report.txt")
        coEvery { repository.save("report.txt", "content") } returns expected

        val useCase = ExportReportUseCase(repository)
        val result = useCase.save("report.txt", "content")

        assertEquals(expected.getOrNull(), result.getOrNull())
        coVerify(exactly = 1) { repository.save("report.txt", "content") }
    }

    @Test
    fun share_savesThenSharesAndReturnsPath() = runTest {
        val repository = mockk<ReportFileRepository>()
        coEvery { repository.save("report.txt", "content") } returns Result.success("/tmp/report.txt")
        coEvery { repository.share("/tmp/report.txt") } returns Result.success(Unit)

        val useCase = ExportReportUseCase(repository)
        val result = useCase.share("report.txt", "content")

        assertEquals("/tmp/report.txt", result.getOrNull())
        coVerify(exactly = 1) { repository.save("report.txt", "content") }
        coVerify(exactly = 1) { repository.share("/tmp/report.txt") }
    }

    @Test
    fun share_returnsFailureWhenSaveFails() = runTest {
        val repository = mockk<ReportFileRepository>()
        coEvery { repository.save("report.txt", "content") } returns Result.failure(IllegalStateException("save fail"))

        val useCase = ExportReportUseCase(repository)
        val result = useCase.share("report.txt", "content")

        assertTrue(result.isFailure)
        coVerify(exactly = 1) { repository.save("report.txt", "content") }
        coVerify(exactly = 0) { repository.share(any()) }
    }
}
