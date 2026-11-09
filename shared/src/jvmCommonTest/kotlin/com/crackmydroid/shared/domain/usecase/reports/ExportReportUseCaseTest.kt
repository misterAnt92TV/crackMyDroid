package com.crackmydroid.shared.domain.usecase.reports

import com.crackmydroid.shared.domain.model.ExportDocument
import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.domain.usecase.settings.GetExportFormatUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame
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
        coVerify(exactly = 0) { repository.share(any<String>()) }
    }

    @Test
    fun `save preserves failure results and converts thrown exceptions to failures`() = runTest {
        val repository = mockk<ReportFileRepository>()
        val failure = IllegalStateException("Cannot save")
        val useCase = ExportReportUseCase(repository)
        coEvery { repository.save("empty.txt", "") } returns Result.failure(failure)
        assertSame(failure, useCase.save("empty.txt", "").exceptionOrNull())
        coEvery { repository.save("empty.txt", "") } throws failure
        assertSame(failure, useCase.save("empty.txt", "").exceptionOrNull())
    }

    @Test
    fun `share converts save or share exceptions into failures`() = runTest {
        for (saveThrows in listOf(false, true)) {
            val repository = mockk<ReportFileRepository>()
            val failure = IllegalStateException("Cannot export")
            val useCase = ExportReportUseCase(repository)
            if (saveThrows) {
                coEvery { repository.save("report.txt", "") } throws failure
                assertSame(failure, useCase.share("report.txt", "").exceptionOrNull())
                coVerify(exactly = 0) { repository.share(any<String>()) }
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

    @Test
    fun `share propagates cancellation instead of returning it as a failure`() = runTest {
        val repository = mockk<ReportFileRepository>()
        val cancellation = kotlinx.coroutines.CancellationException("cancelled")
        coEvery { repository.save("report.txt", "") } returns Result.failure(cancellation)

        val failure = assertFailsWith<kotlinx.coroutines.CancellationException> {
            ExportReportUseCase(repository).share("report.txt", "")
        }

        assertSame(cancellation, failure)
        coVerify(exactly = 0) { repository.share(any<String>()) }
    }

    @Test
    fun `share propagates a cancellation returned after saving`() = runTest {
        val repository = mockk<ReportFileRepository>()
        val cancellation = kotlinx.coroutines.CancellationException("cancelled")
        coEvery { repository.save("report.txt", "") } returns Result.success("/report.txt")
        coEvery { repository.share("/report.txt") } returns Result.failure(cancellation)
        assertSame(
            cancellation,
            assertFailsWith<kotlinx.coroutines.CancellationException> {
                ExportReportUseCase(repository).share("report.txt", "")
            },
        )
    }

    @Test
    fun `document export preserves selected format and payload for save and share`() = runTest {
        val data = buildJsonObject { put("value", "report") }
        for (format in ExportFormat.entries) {
            val repository = mockk<ReportFileRepository>()
            val getFormat = mockk<GetExportFormatUseCase>()
            val saved = slot<ExportDocument>()
            val shared = slot<ExportDocument>()
            val path = "/report.${format.extension}"
            coEvery { getFormat() } returns Result.success(format)
            coEvery { repository.save(capture(saved)) } returns Result.success(path)
            coEvery { repository.share(capture(shared)) } returns Result.success(path)
            val useCase = ExportReportUseCase(repository, getFormat)

            assertEquals(path, useCase.save("report", "activities", "text", data).getOrThrow())
            assertEquals(path, useCase.share("report", "activities", "text", data).getOrThrow())
            val expected = ExportDocument("report", "activities", "text", data, format)
            assertEquals(expected, saved.captured)
            assertEquals(expected, shared.captured)
            assertEquals("report.${format.extension}", saved.captured.fileName)
        }
    }

    @Test
    fun `text reports use the selected format for save and share`() = runTest {
        for (format in ExportFormat.entries) {
            val repository = mockk<ReportFileRepository>()
            val getFormat = mockk<GetExportFormatUseCase>()
            val saved = slot<ExportDocument>()
            val shared = slot<ExportDocument>()
            val path = "/permissions.${format.extension}"
            coEvery { getFormat() } returns Result.success(format)
            coEvery { repository.save(capture(saved)) } returns Result.success(path)
            coEvery { repository.share(capture(shared)) } returns Result.success(path)
            val useCase = ExportReportUseCase(repository, getFormat)

            assertEquals(path, useCase.saveTextReport("permissions", "permissions", "report").getOrThrow())
            assertEquals(path, useCase.shareTextReport("permissions", "permissions", "report").getOrThrow())
            val expected = ExportDocument.textOnly("permissions", "permissions", "report", format)
            assertEquals(expected, saved.captured)
            assertEquals(expected, shared.captured)
            assertEquals("permissions.${format.extension}", saved.captured.fileName)
        }
    }

    @Test
    fun `text export propagates format lookup failures and missing configuration`() = runTest {
        val repository = mockk<ReportFileRepository>()
        val getFormat = mockk<GetExportFormatUseCase>()
        val failure = IllegalStateException("Format unavailable")
        coEvery { getFormat() } returns Result.failure(failure)

        assertSame(
            failure,
            ExportReportUseCase(repository, getFormat)
                .saveTextReport("report", "info", "content")
                .exceptionOrNull(),
        )
        assertTrue(
            ExportReportUseCase(repository)
                .saveTextReport("report", "info", "content")
                .isFailure,
        )
        coVerify(exactly = 0) { repository.save(any<ExportDocument>()) }
    }

    @Test
    fun `document save and share propagate repository failures`() = runTest {
        val repository = mockk<ReportFileRepository>()
        val failure = IllegalStateException("Cannot export document")
        val document = ExportDocument.textOnly("report", "activities", "text", ExportFormat.PDF)
        coEvery { repository.save(document) } returns Result.failure(failure)
        coEvery { repository.share(document) } returns Result.failure(failure)
        val useCase = ExportReportUseCase(repository)

        assertSame(failure, useCase.save(document).exceptionOrNull())
        assertSame(failure, useCase.share(document).exceptionOrNull())
    }
}
