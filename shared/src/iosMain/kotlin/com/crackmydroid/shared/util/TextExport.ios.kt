package com.crackmydroid.shared.util

actual fun saveTextFile(baseName: String, content: String): Result<String> =
    Result.failure(UnsupportedOperationException("Export non disponibile su iOS"))

actual fun shareTextFile(path: String): Result<Unit> =
    Result.failure(UnsupportedOperationException("Share non disponibile su iOS"))
