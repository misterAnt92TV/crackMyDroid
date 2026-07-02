package com.crackmydroid.shared.data.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.crackmydroid.database.CacheDatabase
import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(CacheDatabase.Schema, context, "cache.db")
}
