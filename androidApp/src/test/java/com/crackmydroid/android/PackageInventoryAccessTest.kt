package com.crackmydroid.android

import org.junit.Assert.assertEquals
import org.junit.Test

class PackageInventoryAccessTest {
    @Test
    fun packageInventoryAccess_minimumSdk_doesNotRequireQueryAllPackages() {
        assertEquals(
            PackageInventoryAccess.Full,
            packageInventoryAccess(sdkInt = 24, queryAllPackagesPermissionGranted = false),
        )
    }

    @Test
    fun packageInventoryAccess_preAndroid11_returnsFull() {
        val access = packageInventoryAccess(
            sdkInt = 29,
            queryAllPackagesPermissionGranted = false,
        )

        assertEquals(PackageInventoryAccess.Full, access)
    }

    @Test
    fun packageInventoryAccess_android11PermissionGranted_returnsFull() {
        val access = packageInventoryAccess(
            sdkInt = 30,
            queryAllPackagesPermissionGranted = true,
        )

        assertEquals(PackageInventoryAccess.Full, access)
    }

    @Test
    fun packageInventoryAccess_android11PermissionMissing_returnsUnavailable() {
        val access = packageInventoryAccess(
            sdkInt = 30,
            queryAllPackagesPermissionGranted = false,
        )

        assertEquals(PackageInventoryAccess.Unavailable, access)
    }
}
