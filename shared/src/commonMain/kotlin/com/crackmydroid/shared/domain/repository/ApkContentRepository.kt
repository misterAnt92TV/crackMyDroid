package com.crackmydroid.shared.domain.repository

import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis

/** Platform boundary: source APK access and archive parsing never leak into common UI/domain code. */
interface ApkContentRepository {
    suspend fun analyze(packageName: String): Result<ApkContentAnalysis>
}
