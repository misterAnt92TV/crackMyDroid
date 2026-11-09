package com.crackmydroid.android

import android.os.Build

internal enum class PackageInventoryAccess {
    Full,
    Unavailable
}

/**
 * QUERY_ALL_PACKAGES is an install-time normal permission. It must not be requested with a
 * runtime permission launcher; an unavailable result means this build cannot scan all packages.
 */
internal fun packageInventoryAccess(
    sdkInt: Int,
    queryAllPackagesPermissionGranted: Boolean,
): PackageInventoryAccess = when {
    sdkInt < Build.VERSION_CODES.R -> PackageInventoryAccess.Full
    queryAllPackagesPermissionGranted -> PackageInventoryAccess.Full
    else -> PackageInventoryAccess.Unavailable
}
