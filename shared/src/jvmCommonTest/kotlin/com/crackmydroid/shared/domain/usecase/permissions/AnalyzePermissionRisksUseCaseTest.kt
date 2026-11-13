package com.crackmydroid.shared.domain.usecase.permissions

import com.crackmydroid.shared.domain.model.PermissionFindingKind
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.app.AppPermissionEntry
import com.crackmydroid.shared.domain.usecase.UseCaseTestDispatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AnalyzePermissionRisksUseCaseTest {

    private val dispatchers = UseCaseTestDispatchers()
    private val useCase = AnalyzePermissionRisksUseCase(dispatchers)

    @Test
    fun returnsNullWhenPermissionsAreEmpty() = runTest(dispatchers.default) {
        val app = AppPermissionEntry("App", "pkg.app", emptyList())
        val result = useCase(app).getOrThrow()
        assertNull(result)
    }

    @Test
    fun detectsCriticalPattern_overlayAndAccessibility() = runTest(dispatchers.default) {
        val app = AppPermissionEntry(
            appLabel = "Risky",
            packageName = "pkg.risky",
            permissions = listOf(
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.BIND_ACCESSIBILITY_SERVICE",
            ),
        )

        val result = useCase(app).getOrThrow()
        assertNotNull(result)
        assertTrue(
            result.findings.any {
                it.key == "pattern_overlay_accessibility" &&
                    it.kind == PermissionFindingKind.OVERLAY_ACCESSIBILITY_PATTERN &&
                    it.level == PermissionRiskLevel.Critical
            },
        )
        assertTrue(result.criticalCount >= 1)
    }

    @Test
    fun deduplicatesAndSortsFindingsBySeverity() = runTest(dispatchers.default) {
        val app = AppPermissionEntry(
            appLabel = "Mixed",
            packageName = "pkg.mixed",
            permissions = listOf(
                "android.permission.CAMERA",
                "android.permission.CAMERA",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.SEND_SMS",
            ),
        )

        val result = useCase(app).getOrThrow()
        assertNotNull(result)
        assertEquals(1, result.findings.count { it.key.contains("SEND_SMS") })
        assertEquals(PermissionRiskLevel.Critical, result.findings.first().level)
        assertTrue(result.score > 0)
    }

    @Test
    fun `blank unknown and harmless permissions produce no report`() = runTest(dispatchers.default) {
        for (permissions in listOf(listOf(" ", "\t"), listOf("android.permission.INTERNET", "custom.UNKNOWN"))) {
            assertNull(useCase(AppPermissionEntry("App", "pkg", permissions)).getOrThrow())
        }
    }

    @Test
    fun `permission families receive their expected severity weight and counts`() = runTest(dispatchers.default) {
        val families = mapOf(
            PermissionRiskLevel.Critical to listOf(
                "BIND_ACCESSIBILITY_SERVICE",
                "SYSTEM_ALERT_WINDOW",
                "SEND_SMS",
                "READ_SMS",
                "RECEIVE_SMS",
                "RECEIVE_MMS",
                "RECEIVE_WAP_PUSH",
            ),
            PermissionRiskLevel.High to listOf(
                "REQUEST_INSTALL_PACKAGES", "MANAGE_EXTERNAL_STORAGE", "QUERY_ALL_PACKAGES",
                "READ_CALL_LOG", "WRITE_CALL_LOG", "PROCESS_OUTGOING_CALLS", "READ_CONTACTS",
                "WRITE_CONTACTS", "READ_PHONE_STATE", "CALL_PHONE", "ANSWER_PHONE_CALLS",
                "ACCESS_BACKGROUND_LOCATION", "ACCESS_FINE_LOCATION", "RECORD_AUDIO", "CAMERA",
            ),
            PermissionRiskLevel.Medium to listOf(
                "READ_EXTERNAL_STORAGE",
                "WRITE_EXTERNAL_STORAGE",
                "READ_MEDIA_IMAGES",
                "READ_CALENDAR",
                "WRITE_CALENDAR",
                "BODY_SENSORS",
                "USE_BIOMETRIC",
                "USE_FINGERPRINT",
            ),
        )
        for ((level, tokens) in families) {
            for (token in tokens) {
                val permission = "android.permission.$token"
                val report = assertNotNull(useCase(AppPermissionEntry("Demo", "pkg", listOf(permission))).getOrThrow())
                assertEquals(expectedKind(token), report.findings.single().kind, token)
                assertEquals(level, report.findings.single().level, token)
                assertEquals(if (level == PermissionRiskLevel.Critical) 1 else 0, report.criticalCount, token)
                assertEquals(if (level == PermissionRiskLevel.High) 1 else 0, report.highCount, token)
                assertEquals(if (level == PermissionRiskLevel.Medium) 1 else 0, report.mediumCount, token)
                assertEquals(
                    when (level) {
                        PermissionRiskLevel.Critical -> 5
                        PermissionRiskLevel.High -> 3
                        PermissionRiskLevel.Medium -> 1
                    },
                    report.score,
                    token,
                )
                assertEquals("Demo", report.appLabel)
                assertEquals("pkg", report.packageName)
            }
        }
    }

    @Test
    fun `combined patterns require both members and are ordered before individual findings`() =
        runTest(dispatchers.default) {
            val patterns = listOf(
                Triple("SYSTEM_ALERT_WINDOW", "BIND_ACCESSIBILITY_SERVICE", "pattern_overlay_accessibility"),
                Triple("REQUEST_INSTALL_PACKAGES", "QUERY_ALL_PACKAGES", "pattern_install_query"),
                Triple("RECEIVE_BOOT_COMPLETED", "READ_SMS", "pattern_boot_sms"),
                Triple("ACCESS_BACKGROUND_LOCATION", "CAMERA", "pattern_background_capture"),
                Triple("ACCESS_BACKGROUND_LOCATION", "RECORD_AUDIO", "pattern_background_capture"),
            )
            for ((first, second, key) in patterns) {
                val report =
                    assertNotNull(useCase(AppPermissionEntry("App", "pkg", listOf(first, second))).getOrThrow())
                assertEquals(key, report.findings.first().key)
                assertTrue(report.findings.first().isPattern)
                assertEquals(
                    when (key) {
                        "pattern_overlay_accessibility" -> PermissionFindingKind.OVERLAY_ACCESSIBILITY_PATTERN
                        "pattern_install_query" -> PermissionFindingKind.INSTALL_QUERY_PATTERN
                        "pattern_boot_sms" -> PermissionFindingKind.BOOT_SMS_PATTERN
                        else -> PermissionFindingKind.BACKGROUND_LOCATION_CAPTURE_PATTERN
                    },
                    report.findings.first().kind,
                )
                for (single in listOf(first, second)) {
                    val partial = useCase(AppPermissionEntry("App", "pkg", listOf(single))).getOrThrow()
                    assertTrue(partial?.findings.orEmpty().none { it.key == key })
                }
            }
        }

    @Test
    fun `whitespace duplicates and lowercase permissions retain one normalized finding`() =
        runTest(dispatchers.default) {
            val report = assertNotNull(
                useCase(AppPermissionEntry("App", "pkg", listOf(" camera ", "camera", "", "  "))).getOrThrow(),
            )
            assertEquals("camera", report.findings.single().key)
            assertEquals(PermissionFindingKind.MICROPHONE_CAMERA_ACCESS, report.findings.single().kind)
            assertEquals(3, report.score)
        }

    @Test
    fun `reports sort by descending score then critical count then case insensitive label`() =
        runTest(dispatchers.default) {
            val base = assertNotNull(useCase(AppPermissionEntry("Base", "pkg", listOf("CAMERA"))).getOrThrow())
            val low = base.copy(appLabel = "Low", score = 1)
            val zulu = base.copy(appLabel = "Zulu", score = 10, criticalCount = 0)
            val alpha = zulu.copy(appLabel = "alpha")
            val critical = zulu.copy(appLabel = "Critical", criticalCount = 1)
            assertEquals(listOf(critical, alpha, zulu, low), useCase.sortReports(listOf(low, zulu, alpha, critical)))
            assertEquals(emptyList(), useCase.sortReports(emptyList()))
        }

    private fun expectedKind(token: String): PermissionFindingKind = when (token) {
        "BIND_ACCESSIBILITY_SERVICE" -> PermissionFindingKind.ACCESSIBILITY_SERVICE

        "SYSTEM_ALERT_WINDOW" -> PermissionFindingKind.OVERLAY_ACCESS

        "SEND_SMS", "READ_SMS", "RECEIVE_SMS", "RECEIVE_MMS", "RECEIVE_WAP_PUSH" ->
            PermissionFindingKind.SMS_MMS_ACCESS

        "REQUEST_INSTALL_PACKAGES" -> PermissionFindingKind.APK_INSTALLATION

        "MANAGE_EXTERNAL_STORAGE" -> PermissionFindingKind.BROAD_STORAGE_ACCESS

        "QUERY_ALL_PACKAGES" -> PermissionFindingKind.INSTALLED_APP_INVENTORY

        "READ_CALL_LOG", "WRITE_CALL_LOG", "PROCESS_OUTGOING_CALLS", "READ_CONTACTS",
        "WRITE_CONTACTS", "READ_PHONE_STATE", "CALL_PHONE", "ANSWER_PHONE_CALLS",
        ->
            PermissionFindingKind.PHONE_AND_CONTACTS_ACCESS

        "ACCESS_BACKGROUND_LOCATION", "ACCESS_FINE_LOCATION" -> PermissionFindingKind.LOCATION_ACCESS

        "RECORD_AUDIO", "CAMERA" -> PermissionFindingKind.MICROPHONE_CAMERA_ACCESS

        else -> PermissionFindingKind.SENSITIVE_DATA_ACCESS
    }
}
