package com.crackmydroid.shared.domain.usecase.permissions

import com.crackmydroid.shared.domain.model.AppPermissionEntry
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AnalyzePermissionRisksUseCaseTest {

    private val useCase = AnalyzePermissionRisksUseCase()

    @Test
    fun returnsNullWhenPermissionsAreEmpty() {
        val app = AppPermissionEntry("App", "pkg.app", emptyList())
        val result = useCase(app)
        assertNull(result)
    }

    @Test
    fun detectsCriticalPattern_overlayAndAccessibility() {
        val app = AppPermissionEntry(
            appLabel = "Risky",
            packageName = "pkg.risky",
            permissions = listOf(
                "android.permission.SYSTEM_ALERT_WINDOW",
                "android.permission.BIND_ACCESSIBILITY_SERVICE"
            )
        )

        val result = useCase(app)
        assertNotNull(result)
        assertTrue(result.findings.any { it.key == "pattern_overlay_accessibility" && it.level == PermissionRiskLevel.Critical })
        assertTrue(result.criticalCount >= 1)
    }

    @Test
    fun deduplicatesAndSortsFindingsBySeverity() {
        val app = AppPermissionEntry(
            appLabel = "Mixed",
            packageName = "pkg.mixed",
            permissions = listOf(
                "android.permission.CAMERA",
                "android.permission.CAMERA",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.SEND_SMS"
            )
        )

        val result = useCase(app)
        assertNotNull(result)
        assertEquals(1, result.findings.count { it.key.contains("SEND_SMS") })
        assertEquals(PermissionRiskLevel.Critical, result.findings.first().level)
        assertTrue(result.score > 0)
    }
}
