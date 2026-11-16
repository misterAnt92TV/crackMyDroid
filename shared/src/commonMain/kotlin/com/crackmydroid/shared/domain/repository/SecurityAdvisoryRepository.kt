package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.apkcontent.ApkLibraryVulnerabilityReport
import com.crackmydroid.shared.domain.model.apkcontent.VerifiedVulnerabilityLibrary

interface SecurityAdvisoryRepository {
    suspend fun lookup(libraries: List<VerifiedVulnerabilityLibrary>): ApkLibraryVulnerabilityReport
}
