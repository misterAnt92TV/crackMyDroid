package com.crackmydroid.shared.data.db

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.crackmydroid.database.CacheDatabase
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CacheDatabaseMigrationTest {
    @Test
    fun legacyV1AndV2_reproduceMissingColumn_thenUpgradeAndRescan() {
        for (oldVersion in listOf(1L, 2L)) {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
                legacySchema(driver, manifest = false, activityFlags = false, version = oldVersion)
                assertFails { writeInstalled(driver) }
                assertFails { writeActivity(driver) }

                upgrade(driver)

                assertEquals(3L, scalar(driver, "PRAGMA user_version"))
                assertEquals(0L, scalar(driver, "SELECT count(*) FROM installed_apps_cache"))
                assertEquals(0L, scalar(driver, "SELECT count(*) FROM activities_cache"))
                assertOtherCachesPreserved(driver)
                assertCurrentQueries(driver)
            }
        }
    }

    @Test
    fun completeV1AndV2_preserveExistingRowsAndManifestValues() {
        for (oldVersion in listOf(1L, 2L)) {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
                legacySchema(driver, manifest = true, activityFlags = true, version = oldVersion)
                driver.execute(null, "UPDATE installed_apps_cache SET debuggable=1, targetSdkVersion=35", 0)
                driver.execute(null, "UPDATE activities_cache SET exported=1, permission='test.permission'", 0)
                if (oldVersion == 2L) {
                    driver.execute(
                        null,
                        "UPDATE installed_apps_cache SET installingPackageName='existing.installer'",
                        0,
                    )
                }

                upgrade(driver)

                val queries = CacheDatabase(driver).cacheDatabaseQueries
                assertEquals(1L, queries.selectAllInstalled().executeAsOne().debuggable)
                assertEquals(35L, queries.selectAllInstalled().executeAsOne().targetSdkVersion)
                assertEquals(1L, queries.selectAllActivities().executeAsOne().exported)
                assertEquals("test.permission", queries.selectAllActivities().executeAsOne().permission)
                if (oldVersion == 2L) {
                    assertEquals(
                        "existing.installer",
                        queries.selectAllInstalled().executeAsOne().installingPackageName,
                    )
                }
                assertOtherCachesPreserved(driver)
                assertCurrentQueries(driver)
            }
        }
    }

    @Test
    fun partialSchemas_invalidateOnlyTheTableWithMissingColumns() {
        for (manifest in listOf(false, true)) {
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
                legacySchema(driver, manifest = manifest, activityFlags = !manifest, version = 2)

                upgrade(driver)

                assertEquals(if (manifest) 1L else 0L, scalar(driver, "SELECT count(*) FROM installed_apps_cache"))
                assertEquals(if (manifest) 0L else 1L, scalar(driver, "SELECT count(*) FROM activities_cache"))
                assertOtherCachesPreserved(driver)
            }
        }
    }

    @Test
    fun missingSingleColumn_isAddedWithoutDuplicateColumnErrors() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            legacySchema(driver, manifest = false, activityFlags = true, version = 2)
            driver.execute(null, "ALTER TABLE installed_apps_cache ADD COLUMN debuggable INTEGER NOT NULL DEFAULT 0", 0)
            driver.execute(
                null,
                "ALTER TABLE installed_apps_cache ADD COLUMN allowBackup INTEGER NOT NULL DEFAULT 0",
                0,
            )
            driver.execute(null, "ALTER TABLE installed_apps_cache ADD COLUMN testOnly INTEGER NOT NULL DEFAULT 0", 0)
            driver.execute(null, "ALTER TABLE installed_apps_cache ADD COLUMN targetSdkVersion INTEGER", 0)

            upgrade(driver)

            assertEquals(0L, scalar(driver, "SELECT count(*) FROM installed_apps_cache"))
            assertEquals(1L, scalar(driver, "SELECT count(*) FROM activities_cache"))
            assertCurrentQueries(driver)
        }
    }

    @Test
    fun freshAndMigratedSchemas_haveMatchingColumnsDefaultsAndNullability() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { fresh ->
            CacheDatabaseSchema.create(fresh)
            assertEquals(3L, CacheDatabaseSchema.version)
            assertCurrentQueries(fresh)
            JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { migrated ->
                legacySchema(migrated, manifest = false, activityFlags = false, version = 1)
                upgrade(migrated)
                for (table in tables) {
                    assertEquals(columns(fresh, table), columns(migrated, table), table)
                }
            }
        }
    }

    @Test
    fun callbacks_observeRepairedSchema_atVersionBoundary() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            legacySchema(driver, manifest = false, activityFlags = false, version = 1)
            val callbacks = mutableListOf<Long>()
            CacheDatabase(driver).transaction {
                CacheDatabaseSchema.migrate(
                    driver,
                    1,
                    3,
                    AfterVersion(1) {
                        assertTrue("installSourceClassification" in columns(it, "installed_apps_cache"))
                        callbacks += 1
                    },
                    AfterVersion(2) {
                        assertTrue("debuggable" in columns(it, "installed_apps_cache"))
                        assertTrue("exported" in columns(it, "activities_cache"))
                        callbacks += 2
                    },
                )
            }
            assertEquals(listOf(1L, 2L), callbacks)
        }
    }

    @Test
    fun failedUpgrade_rollsBackSchemaAndCacheChanges() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            legacySchema(driver, manifest = false, activityFlags = false, version = 2)
            assertFails {
                CacheDatabase(driver).transaction {
                    CacheDatabaseSchema.migrate(driver, 2, 3, AfterVersion(2) { error("abort upgrade") })
                }
            }
            assertFalse("debuggable" in columns(driver, "installed_apps_cache"))
            assertEquals(1L, scalar(driver, "SELECT count(*) FROM installed_apps_cache"))
            assertEquals(2L, scalar(driver, "PRAGMA user_version"))
        }
    }

    @Test
    fun reopenedCurrentDatabase_preservesNewlyScannedRows() {
        val file = Files.createTempFile("cache-migration-", ".db")
        try {
            val url = "jdbc:sqlite:${file.toAbsolutePath()}"
            JdbcSqliteDriver(url).use { driver ->
                legacySchema(driver, manifest = false, activityFlags = false, version = 2)
                upgrade(driver)
                assertCurrentQueries(driver)
            }
            JdbcSqliteDriver(url).use { driver ->
                upgrade(driver)
                assertEquals(3L, scalar(driver, "PRAGMA user_version"))
                assertEquals(
                    1L,
                    CacheDatabase(driver).cacheDatabaseQueries.selectAllInstalled().executeAsOne().debuggable,
                )
            }
        } finally {
            Files.deleteIfExists(file)
        }
    }

    @Test
    fun clearDeviceInfo_removesFieldsAndLegacyJsonWithoutClearingOtherCaches() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            CacheDatabaseSchema.create(driver)
            val queries = CacheDatabase(driver).cacheDatabaseQueries
            queries.setInfoField("manufacturer", "Test")
            queries.setInfoField("serial", "synthetic")
            queries.setDeviceInfo("{}")
            queries.insertPermission("test.app", "Test", "test.permission")

            queries.clearDeviceInfo()

            assertTrue(queries.getAllInfo().executeAsList().isEmpty())
            assertEquals(1, queries.selectAllPermissions().executeAsList().size)
        }
    }

    @Test
    fun packageAndKeyLookups_usePrimaryKeyIndexes() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            CacheDatabaseSchema.create(driver)
            for (query in listOf(
                "SELECT * FROM activities_cache WHERE packageName='test.app'",
                "SELECT * FROM permissions_cache WHERE packageName='test.app'",
                "SELECT value FROM device_info_cache WHERE key='device_info'",
            )) {
                val plan = driver.executeQuery(
                    null,
                    "EXPLAIN QUERY PLAN $query",
                    { cursor ->
                        val details = mutableListOf<String>()
                        while (cursor.next().value) details += checkNotNull(cursor.getString(3))
                        QueryResult.Value(details)
                    },
                    0,
                ).value.joinToString()
                assertTrue(plan.contains("SEARCH", ignoreCase = true), plan)
                assertTrue(plan.contains("INDEX", ignoreCase = true), plan)
            }
        }
    }

    private fun legacySchema(driver: SqlDriver, manifest: Boolean, activityFlags: Boolean, version: Long) {
        val manifestColumns = if (manifest) {
            """
            , debuggable INTEGER NOT NULL DEFAULT 0, allowBackup INTEGER NOT NULL DEFAULT 0,
            testOnly INTEGER NOT NULL DEFAULT 0, targetSdkVersion INTEGER, minSdkVersion INTEGER
        """
        } else {
            ""
        }
        val activityColumns = if (activityFlags) ", exported INTEGER NOT NULL DEFAULT 0, permission TEXT" else ""
        driver.execute(
            null,
            """
            CREATE TABLE installed_apps_cache (
                packageName TEXT NOT NULL PRIMARY KEY, appLabel TEXT NOT NULL, sourcePath TEXT NOT NULL
                $manifestColumns
            )
            """.trimIndent(),
            0,
        )
        driver.execute(
            null,
            """
            CREATE TABLE activities_cache (
                packageName TEXT NOT NULL, appLabel TEXT NOT NULL, activityName TEXT NOT NULL,
                label TEXT NOT NULL $activityColumns, PRIMARY KEY(packageName, activityName)
            )
            """.trimIndent(),
            0,
        )
        driver.execute(
            null,
            """
            CREATE TABLE permissions_cache (
                packageName TEXT NOT NULL, appLabel TEXT NOT NULL, permission TEXT NOT NULL,
                PRIMARY KEY(packageName, permission)
            )
            """.trimIndent(),
            0,
        )
        driver.execute(null, "CREATE TABLE device_info_cache (key TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)", 0)
        driver.execute(
            null,
            "INSERT INTO installed_apps_cache(packageName,appLabel,sourcePath) VALUES('test.app','Test','/test.apk')",
            0,
        )
        driver.execute(
            null,
            "INSERT INTO activities_cache(packageName,appLabel,activityName,label) VALUES('test.app','Test','Main','Main')",
            0,
        )
        driver.execute(null, "INSERT INTO permissions_cache VALUES('test.app','Test','test.permission')", 0)
        driver.execute(null, "INSERT INTO device_info_cache VALUES('manufacturer','Test')", 0)
        if (version == 2L) CacheDatabase.Schema.migrate(driver, 1, 2)
        driver.execute(null, "PRAGMA user_version=$version", 0)
    }

    private fun upgrade(driver: SqlDriver) {
        val old = scalar(driver, "PRAGMA user_version")
        if (old < CacheDatabaseSchema.version) {
            CacheDatabase(driver).transaction {
                CacheDatabaseSchema.migrate(driver, old, CacheDatabaseSchema.version)
                driver.execute(null, "PRAGMA user_version=${CacheDatabaseSchema.version}", 0)
            }
        }
    }

    private fun assertOtherCachesPreserved(driver: SqlDriver) {
        val queries = CacheDatabase(driver).cacheDatabaseQueries
        assertEquals("Test", queries.getInfoField("manufacturer").executeAsOne())
        assertEquals("test.permission", queries.selectPermissionsByPackage("test.app").executeAsOne().permission)
    }

    private fun assertCurrentQueries(driver: SqlDriver) {
        writeInstalled(driver)
        writeActivity(driver)
        val queries = CacheDatabase(driver).cacheDatabaseQueries
        val app = queries.selectAllInstalled().executeAsOne()
        assertEquals(1L, app.debuggable)
        assertEquals(1L, app.allowBackup)
        assertEquals(0L, app.testOnly)
        assertEquals(36L, app.targetSdkVersion)
        assertEquals(24L, app.minSdkVersion)
        assertEquals("OTHER", app.installSourceClassification)
        assertEquals("test.installer", app.installingPackageName)
        assertEquals(123L, app.installSourceObservedAtEpochMs)
        val activity = queries.selectActivitiesByPackage("test.app").executeAsOne()
        assertEquals(1L, activity.exported)
        assertEquals("test.permission", activity.permission)
    }

    private fun writeInstalled(driver: SqlDriver) {
        CacheDatabase(driver).cacheDatabaseQueries.insertInstalled(
            "test.app", "Test", "/test.apk", 1, 1, 0, 36, 24,
            "OTHER", "test.installer", null, null, 123, null,
        )
    }

    private fun writeActivity(driver: SqlDriver) {
        CacheDatabase(driver).cacheDatabaseQueries.insertActivity(
            "test.app",
            "Test",
            "Main",
            "Main",
            1,
            "test.permission",
        )
    }

    private fun scalar(driver: SqlDriver, sql: String): Long = driver.executeQuery(
        null,
        sql,
        { cursor ->
            check(cursor.next().value)
            QueryResult.Value(checkNotNull(cursor.getLong(0)))
        },
        0,
    ).value

    private fun columns(driver: SqlDriver, table: String): Map<String, List<String?>> = driver.executeQuery(
        null,
        "PRAGMA table_info($table)",
        { cursor ->
            val result = mutableMapOf<String, List<String?>>()
            while (cursor.next().value) {
                result[checkNotNull(cursor.getString(1))] = listOf(
                    cursor.getString(2),
                    cursor.getLong(3).toString(),
                    cursor.getString(4),
                    cursor.getLong(5).toString(),
                )
            }
            QueryResult.Value(result)
        },
        0,
    ).value

    private val tables = listOf("installed_apps_cache", "activities_cache", "permissions_cache", "device_info_cache")
}
