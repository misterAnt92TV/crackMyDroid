package com.crackmydroid.shared.i18n

import com.crackmydroid.shared.domain.model.PenTestActivityRisk
import com.crackmydroid.shared.domain.model.PenTestMessage
import com.crackmydroid.shared.domain.model.PenTestSurface
import com.crackmydroid.shared.domain.model.PermissionFindingKind
import com.crackmydroid.shared.domain.model.PermissionRiskFinding
import com.crackmydroid.shared.domain.model.PermissionRiskLevel
import com.crackmydroid.shared.domain.model.ReportLabel
import com.crackmydroid.shared.domain.model.activity.ActivityIntentRiskLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UseCaseTextsTest {
    private val languages = listOf("en", "it", "de", "es", "fr")

    @Test
    fun `catalog covers every permission kind and report label in all supported languages`() {
        languages.forEach { language ->
            val texts = UseCaseTexts(language)
            assertEquals(language, texts.languageCode)
            PermissionFindingKind.values().forEach { kind ->
                val finding = PermissionRiskFinding(
                    key = "android.permission.CAMERA",
                    kind = kind,
                    level = PermissionRiskLevel.High,
                    isPattern = kind.name.endsWith("_PATTERN")
                )
                assertFalse(texts.permissionTitle(finding).isBlank(), "$language $kind title")
                assertFalse(texts.permissionReason(finding).isBlank(), "$language $kind reason")
            }
            ReportLabel.values().forEach { label ->
                assertFalse(texts.label(label).isBlank(), "$language $label")
            }
            PermissionRiskLevel.values().forEach { level ->
                assertFalse(texts.riskLevel(level).isBlank(), "$language $level")
            }
            ActivityIntentRiskLevel.values().forEach { level ->
                assertFalse(texts.activityRiskLevel(level).isBlank(), "$language $level")
            }
            allPenTestMessages().forEach { message ->
                assertFalse(texts.penTest(message).isBlank(), "$language $message")
            }
        }
    }

    @Test
    fun `catalog canonicalizes language codes and falls back to English`() {
        assertEquals("en", UseCaseTexts("EN-us").languageCode)
        assertEquals("it", UseCaseTexts("it-IT").languageCode)
        assertEquals("en", UseCaseTexts("pt-BR").languageCode)
        assertEquals(
            UseCaseTexts("en").permissionReason(finding(PermissionFindingKind.SENSITIVE_DATA_ACCESS)),
            UseCaseTexts("pt").permissionReason(finding(PermissionFindingKind.SENSITIVE_DATA_ACCESS))
        )
    }

    @Test
    fun `translations differ from English for each supported non-English locale`() {
        val english = UseCaseTexts("en").permissionReason(finding(PermissionFindingKind.SENSITIVE_DATA_ACCESS))
        listOf("it", "de", "es", "fr").forEach { language ->
            assertFalse(
                english == UseCaseTexts(language)
                    .permissionReason(finding(PermissionFindingKind.SENSITIVE_DATA_ACCESS)),
                "$language must use a real translation"
            )
        }
    }

    @Test
    fun `technical permission and activity identifiers are preserved`() {
        val texts = UseCaseTexts("fr")
        val permission = "android.permission.SYSTEM_ALERT_WINDOW"
        assertEquals(
            permission,
            texts.permissionTitle(
                PermissionRiskFinding(
                    key = permission,
                    kind = PermissionFindingKind.OVERLAY_ACCESS,
                    level = PermissionRiskLevel.Critical
                )
            )
        )
        val manifest = texts.penTest(
            PenTestMessage.ManifestSurface(
                permissions = listOf(permission),
                activities = listOf(PenTestActivityRisk("CheckoutActivity", ActivityIntentRiskLevel.High))
            )
        )
        assertTrue(manifest.contains(permission))
        assertTrue(manifest.contains("CheckoutActivity"))
        assertTrue(manifest.contains(texts.activityRiskLevel(ActivityIntentRiskLevel.High)))

        assertTrue(
            texts.penTest(PenTestMessage.NetworkSurface(listOf("READ_SMS"))).contains("INTERNET + READ_SMS")
        )
    }

    @Test
    fun `booleans are localized and summaries retain typed count values`() {
        assertEquals("Yes", UseCaseTexts("en").boolean(true))
        assertEquals("Sì", UseCaseTexts("it").boolean(true))
        assertEquals("Nein", UseCaseTexts("de").boolean(false))
        assertEquals("Sí", UseCaseTexts("es").boolean(true))
        assertEquals("Oui", UseCaseTexts("fr").boolean(true))
        languages.forEach { language ->
            val summary = UseCaseTexts(language).penTest(
                PenTestMessage.PermissionRiskSummary(9, 1, 2, 3)
            )
            assertTrue(summary.contains("9"))
            assertTrue(summary.contains("1"))
            assertTrue(summary.contains("2"))
            assertTrue(summary.contains("3"))
        }
    }

    private fun finding(kind: PermissionFindingKind) = PermissionRiskFinding(
        key = "android.permission.CAMERA",
        kind = kind,
        level = PermissionRiskLevel.High
    )

    private fun allPenTestMessages(): List<PenTestMessage> = listOf(
        PenTestMessage.PermissionRiskSummary(7, 1, 1, 0),
        PenTestMessage.MissingPermissionData(PenTestSurface.MANIFEST),
        PenTestMessage.MissingPermissionData(PenTestSurface.NETWORK),
        PenTestMessage.ManifestSurface(
            permissions = listOf("SYSTEM_ALERT_WINDOW"),
            activities = listOf(PenTestActivityRisk("MainActivity", ActivityIntentRiskLevel.Critical))
        ),
        PenTestMessage.RootUnavailable,
        PenTestMessage.FileExposure(listOf("MANAGE_EXTERNAL_STORAGE")),
        PenTestMessage.NetworkSurface(listOf("READ_SMS")),
        PenTestMessage.DependencyRiskSummary(10, 1, 2, 0),
        PenTestMessage.InstallPackagesDependencyReview,
        PenTestMessage.SecurityAnomaly(5, 1, 0, 0),
        PenTestMessage.CriticalActivityEntryPoints(
            listOf(PenTestActivityRisk("MainActivity", ActivityIntentRiskLevel.Critical))
        )
    )
}
