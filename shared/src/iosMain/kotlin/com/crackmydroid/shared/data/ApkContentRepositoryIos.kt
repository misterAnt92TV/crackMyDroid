package com.crackmydroid.shared.data

import com.crackmydroid.shared.domain.model.apkcontent.ApkContentAnalysis
import com.crackmydroid.shared.domain.repository.ApkContentRepository

class ApkContentRepositoryIos : ApkContentRepository {
    override suspend fun analyze(packageName: String): Result<ApkContentAnalysis> =
        Result.failure(UnsupportedOperationException("APK content analysis requires an Android target."))
}
