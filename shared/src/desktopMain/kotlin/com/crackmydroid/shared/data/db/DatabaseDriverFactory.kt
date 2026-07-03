package com.crackmydroid.shared.data.db

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver =
        error("Desktop target does not use a SQLDelight driver in v1")
}
