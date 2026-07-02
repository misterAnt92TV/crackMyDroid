package com.crackmydroid.shared.util

expect fun saveTextFile(baseName: String, content: String): Result<String>

expect fun shareTextFile(path: String): Result<Unit>
