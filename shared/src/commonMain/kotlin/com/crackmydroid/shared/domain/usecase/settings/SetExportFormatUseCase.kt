package com.crackmydroid.shared.domain.usecase

import com.crackmydroid.shared.domain.model.ExportFormat
import com.crackmydroid.shared.domain.repository.SettingsRepository

class SetExportFormatUseCase(private val repository: SettingsRepository) {
    suspend operator fun invoke(format: ExportFormat) = repository.setExportFormat(format)
}
