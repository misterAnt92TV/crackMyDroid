package com.crackmydroid.shared.domain.model

internal data class ActivityLaunchability(
    val launchableViaShell: Boolean,
    val launchabilityReason: String?
)

/**
 * The Android app can only start components exposed to other applications.
 * Keep non-launchable entries in the list so they remain available for audit.
 */
internal fun classifyAndroidActivityLaunchability(
    exported: Boolean,
    permission: String?
): ActivityLaunchability = when {
    !exported -> ActivityLaunchability(
        launchableViaShell = false,
        launchabilityReason = "Activity non esportata: non avviabile da questa app"
    )
    !permission.isNullOrBlank() -> ActivityLaunchability(
        launchableViaShell = false,
        launchabilityReason = "Activity protetta dalla permission $permission"
    )
    else -> ActivityLaunchability(launchableViaShell = true, launchabilityReason = null)
}

internal fun ActivityEntry.favoriteKey(): String = "$packageName/$activityName"
