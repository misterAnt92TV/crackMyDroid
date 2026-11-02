package com.crackmydroid.shared.ui.screens

internal data class AutoScanAppSummary(
    val packageName: String,
    val appLabel: String,
    val totalChecks: Int,
    val successCount: Int,
    val errorCount: Int,
    val queuedCount: Int
)
