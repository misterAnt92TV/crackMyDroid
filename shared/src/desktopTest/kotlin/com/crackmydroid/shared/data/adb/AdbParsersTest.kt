package com.crackmydroid.shared.data.adb

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdbParsersTest {
    @Test
    fun parsesDeviceList() {
        val output = """
            List of devices attached
            RZCX20J738K device usb:2-1 product:a54xnaeea model:SM_A546B device:a54x transport_id:1
            emulator-5554 offline transport_id:7
            ABC123 unauthorized usb:3-1 product:pixel model:Pixel_8 device:husky transport_id:2
        """.trimIndent()

        val devices = AdbParsers.parseAdbDevices(output)

        assertEquals(3, devices.size)
        assertEquals("RZCX20J738K", devices[0].serial)
        assertEquals("SM_A546B", devices[0].model)
        assertEquals("offline", devices[1].state)
        assertEquals("unauthorized", devices[2].state)
    }

    @Test
    fun parsesInstalledAppsAndPmPath() {
        val output = """
            package:/system/app/Calculator/Calculator.apk=com.android.calculator2
            package:/data/app/~~hash==/base.apk=com.example.demo
        """.trimIndent()

        val apps = AdbParsers.parseInstalledApps(output)
        val paths = AdbParsers.parsePmPath(output)

        assertEquals(2, apps.size)
        assertEquals("com.example.demo", apps[1].packageName)
        assertTrue(paths.first().endsWith("Calculator.apk"))
    }

    @Test
    fun parsesPermissionsAndActivitiesFromDump() {
        val dump = """
            requested permissions:
              android.permission.INTERNET
              android.permission.CAMERA
            install permissions:
              android.permission.INTERNET: granted=true

            Activity Resolver Table:
              Schemes:
                demo:
                  123abc com.example.demo/.MainActivity filter 456def
                  Action: "android.intent.action.VIEW"
                  789ghi com.example.demo/com.example.demo.Settings${'$'}DeepLinkActivity filter 012jkl
                  Action: "android.intent.action.VIEW"
                  Scheme: "demo"
                  Authority: "host.example"
                  Path: "/details"
                  StaticType: "image/*"

            Activities:
              Activity{abc123 com.example.demo/.HiddenActivity}
              Activity{def456 com.example.demo/.MainActivity}

            Receiver Resolver Table:
              Schemes:
                demo:
                  999xyz com.example.demo/.DemoReceiver filter 111aaa
        """.trimIndent()

        val permissions = AdbParsers.parseRequestedPermissions(dump)
        val activities = AdbParsers.parseActivities("com.example.demo", "Demo", dump)

        assertEquals(listOf("android.permission.CAMERA", "android.permission.INTERNET"), permissions)
        assertEquals(3, activities.size)
        assertEquals("com.example.demo.MainActivity", activities[0].activityName)
        assertEquals(true, activities[0].launchableViaShell)
        assertEquals("action android.intent.action.VIEW", activities[0].launchContextHint)
        assertEquals("android.intent.action.VIEW", activities[0].launchIntentAction)
        assertEquals("com.example.demo.Settings\$DeepLinkActivity", activities[1].activityName)
        assertEquals("DeepLinkActivity", activities[1].label)
        assertEquals(false, activities[1].launchableViaShell)
        assertEquals("action android.intent.action.VIEW • scheme demo • authority host.example • type image/* • path /details", activities[1].launchContextHint)
        assertTrue(activities[1].launchabilityReason?.contains("intent contestualizzato") == true)
        assertEquals("android.intent.action.VIEW", activities[1].launchIntentAction)
        assertEquals("demo://host.example/details", activities[1].launchIntentData)
        assertEquals("image/*", activities[1].launchIntentMimeType)
        assertEquals("com.example.demo.HiddenActivity", activities[2].activityName)
        assertEquals(false, activities[2].launchableViaShell)
    }
}
