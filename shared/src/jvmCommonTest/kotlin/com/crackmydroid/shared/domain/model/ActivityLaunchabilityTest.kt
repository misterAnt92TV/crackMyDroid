package com.crackmydroid.shared.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ActivityLaunchabilityTest {
    @Test
    fun classifyAndroidActivityLaunchability_exportedWithoutPermission_isLaunchable() {
        val result = classifyAndroidActivityLaunchability(exported = true, permission = null)

        assertTrue(result.launchableViaShell)
        assertNull(result.launchabilityReason)
    }

    @Test
    fun classifyAndroidActivityLaunchability_notExported_isNotLaunchable() {
        val result = classifyAndroidActivityLaunchability(exported = false, permission = null)

        assertFalse(result.launchableViaShell)
        assertEquals(
            "Activity non esportata: non avviabile da questa app",
            result.launchabilityReason
        )
    }

    @Test
    fun classifyAndroidActivityLaunchability_permissionProtected_isNotLaunchable() {
        val result = classifyAndroidActivityLaunchability(
            exported = true,
            permission = "com.example.permission.START"
        )

        assertFalse(result.launchableViaShell)
        assertEquals(
            "Activity protetta dalla permission com.example.permission.START",
            result.launchabilityReason
        )
    }

    @Test
    fun favoriteKey_activityEntry_returnsCanonicalPackageAndActivityKey() {
        val key = ActivityEntry(
            label = "Details",
            appLabel = "Demo",
            packageName = "com.example.demo",
            activityName = "com.example.demo.DetailsActivity"
        ).favoriteKey()

        assertEquals("com.example.demo/com.example.demo.DetailsActivity", key)
    }
}
