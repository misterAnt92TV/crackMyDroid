package com.crackmydroid.shared.domain.usecase.reports

import com.crackmydroid.shared.domain.repository.ReportFileRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

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
        coVerifyOrder {
            repository.save("report.txt", "content")
            repository.share("/tmp/report.txt")
        }
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

    @Test
    fun `save preserves failure results and thrown exceptions`() = runTest {
        val repository = mockk<ReportFileRepository>()
        val failure = IllegalStateException("Cannot save")
        val useCase = ExportReportUseCase(repository)
        coEvery { repository.save("empty.txt", "") } returns Result.failure(failure)
        assertSame(failure, useCase.save("empty.txt", "").exceptionOrNull())
        coEvery { repository.save("empty.txt", "") } throws failure
        assertSame(failure, assertFailsWith<IllegalStateException> { useCase.save("empty.txt", "") })
    }

    @Test
    fun `share preserves save or share exceptions without reporting a successful path`() = runTest {
        for (saveThrows in listOf(false, true)) {
            val repository = mockk<ReportFileRepository>()
            val failure = IllegalStateException("Cannot export")
            val useCase = ExportReportUseCase(repository)
            if (saveThrows) {
                coEvery { repository.save("report.txt", "") } throws failure
                assertSame(failure, useCase.share("report.txt", "").exceptionOrNull())
                coVerify(exactly = 0) { repository.share(any()) }
            } else {
                coEvery { repository.save("report.txt", "") } returns Result.success("/generated.txt")
                coEvery { repository.share("/generated.txt") } returns Result.failure(failure)
                assertSame(failure, useCase.share("report.txt", "").exceptionOrNull())
                coEvery { repository.share("/generated.txt") } throws failure
                assertSame(failure, useCase.share("report.txt", "").exceptionOrNull())
                coVerify(exactly = 2) { repository.share("/generated.txt") }
            }
        }
    }
}
