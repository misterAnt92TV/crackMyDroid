package com.crackmydroid.shared.domain.model

object DeviceScanSnapshotContract {
    const val SNAPSHOT_DIRECTORY: String = "desktop_snapshot"
    const val SNAPSHOT_FILE_NAME: String = "device_scan_snapshot_v1.json"
    const val ANDROID_APP_PACKAGE: String = "com.crackmydroid.android"

    fun remotePath(appPackage: String = ANDROID_APP_PACKAGE): String =
        "/sdcard/Android/data/$appPackage/files/$SNAPSHOT_DIRECTORY/$SNAPSHOT_FILE_NAME"
}
