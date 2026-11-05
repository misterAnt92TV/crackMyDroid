package com.crackmydroid.shared.domain.usecase.settings

import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository

class GetExportFormatUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(): ExportFormat = repository.getExportFormat()
}
