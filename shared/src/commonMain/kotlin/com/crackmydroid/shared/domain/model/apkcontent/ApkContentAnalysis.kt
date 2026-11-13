package com.crackmydroid.shared.domain.model.apkcontent

import kotlinx.serialization.Serializable

@Serializable
enum class ApkFindingSeverity { Info, Medium, High, Critical }

@Serializable
data class ApkContentFinding(
    val severity: ApkFindingSeverity,
    val category: String,
    val evidence: String,
    val remediation: String,
)

@Serializable
data class ApkLibrary(val name: String, val evidence: String, val version: String? = null)

@Serializable
data class ApkArchiveMetadata(
    val sha256: String,
    val archiveBytes: Long,
    val entryCount: Int,
    val uncompressedBytes: Long,
    val dexCount: Int,
    val nativeLibraries: List<String>,
    val certificateEntries: List<String>,
)

@Serializable
enum class ApkPlainTextCategory {
    Endpoint,
    ConfigurationKey,
    TextString,
}

@Serializable
data class ApkPlainTextItem(val category: ApkPlainTextCategory, val value: String, val source: String)

@Serializable
data class ApkPlaintextContent(val items: List<ApkPlainTextItem> = emptyList()) {
    val totalCount: Int
        get() = items.size

    val endpoints: List<ApkPlainTextItem>
        get() = items.filter { it.category == ApkPlainTextCategory.Endpoint }

    val configKeys: List<ApkPlainTextItem>
        get() = items.filter { it.category == ApkPlainTextCategory.ConfigurationKey }

    val textStrings: List<ApkPlainTextItem>
        get() = items.filter { it.category == ApkPlainTextCategory.TextString }
}

@Serializable
data class ApkContentAnalysis(
    val packageName: String,
    val metadata: ApkArchiveMetadata,
    val libraries: List<ApkLibrary>,
    val findings: List<ApkContentFinding>,
    val structuralEntries: List<String>,
    val plaintextContent: ApkPlaintextContent = ApkPlaintextContent(),
) {
    val highestSeverity: ApkFindingSeverity
        get() = findings.maxByOrNull { it.severity.ordinal }?.severity ?: ApkFindingSeverity.Info
}
