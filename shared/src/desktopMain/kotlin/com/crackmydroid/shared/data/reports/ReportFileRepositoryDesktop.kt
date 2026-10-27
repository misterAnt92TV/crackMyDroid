package com.crackmydroid.shared.data.reports

import com.crackmydroid.shared.domain.repository.ReportFileRepository
import com.crackmydroid.shared.util.saveTextFile
import com.crackmydroid.shared.util.shareTextFile

class ReportFileRepositoryDesktop : ReportFileRepository {
    override suspend fun save(fileName: String, content: String): Result<String> =
        saveTextFile(fileName, content)

    override suspend fun share(path: String): Result<Unit> = shareTextFile(path)
}
