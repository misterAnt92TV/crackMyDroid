package com.crackmydroid.shared.data.db

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.crackmydroid.database.CacheDatabase

object CacheDatabaseSchema : SqlSchema<QueryResult.Value<Unit>> by CacheDatabase.Schema {
    override fun migrate(
        driver: SqlDriver,
        oldVersion: Long,
        newVersion: Long,
        vararg callbacks: AfterVersion
    ): QueryResult.Value<Unit> = CacheDatabase.Schema.migrate(
        driver,
        oldVersion,
        newVersion,
        AfterVersion(2) { repairLegacyColumns(it) },
        *callbacks
    )

    private fun repairLegacyColumns(driver: SqlDriver) {
        repairTable(
            driver,
            "installed_apps_cache",
            mapOf(
                "debuggable" to "INTEGER NOT NULL DEFAULT 0",
                "allowBackup" to "INTEGER NOT NULL DEFAULT 0",
                "testOnly" to "INTEGER NOT NULL DEFAULT 0",
                "targetSdkVersion" to "INTEGER",
                "minSdkVersion" to "INTEGER"
            )
        )
        repairTable(
            driver,
            "activities_cache",
            mapOf(
                "exported" to "INTEGER NOT NULL DEFAULT 0",
                "permission" to "TEXT"
            )
        )
    }

    private fun repairTable(driver: SqlDriver, table: String, definitions: Map<String, String>) {
        val columns = driver.executeQuery(
            identifier = null,
            sql = "PRAGMA table_info($table)",
            mapper = { cursor ->
                val names = mutableSetOf<String>()
                while (cursor.next().value) {
                    names += checkNotNull(cursor.getString(1))
                }
                QueryResult.Value(names)
            },
            parameters = 0
        ).value
        check(columns.isNotEmpty()) { "Missing cache table during migration: $table" }
        val missing = definitions.filterKeys { it !in columns }
        missing.forEach { (name, definition) ->
            driver.execute(null, "ALTER TABLE $table ADD COLUMN $name $definition", 0)
        }
        // Newly added defaults are not observed manifest values; rescan only the repaired cache.
        if (missing.isNotEmpty()) {
            driver.execute(null, "DELETE FROM $table", 0)
        }
    }
}
