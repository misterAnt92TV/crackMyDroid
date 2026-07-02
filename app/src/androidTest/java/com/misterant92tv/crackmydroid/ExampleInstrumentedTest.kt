package com.misterant92tv.crackmydroid

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test that runs on an Android device or emulator.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.misterant92tv.crackmydroid", appContext.packageName)
    }

    @Test
    fun appSecurityCheckerReturnsReportForSelf() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val checker = AppSecurityChecker(context.packageManager)
        val report = checker.analyse(context.packageName)
        assertEquals(context.packageName, report.packageName)
        assertNotNull(report.appName)
        assertTrue(report.riskScore in 0..100)
    }
}
