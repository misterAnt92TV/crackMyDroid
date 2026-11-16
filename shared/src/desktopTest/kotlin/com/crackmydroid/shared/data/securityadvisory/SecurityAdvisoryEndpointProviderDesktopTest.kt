package com.crackmydroid.shared.data.securityadvisory

import kotlin.test.Test
import kotlin.test.assertNull

class SecurityAdvisoryEndpointProviderDesktopTest {
    @Test
    fun `desktop provider reports unavailable when endpoint property is missing`() {
        val property = "crackmydroid.securityAdvisoryEndpoint"
        val previous = System.getProperty(property)
        try {
            System.clearProperty(property)

            assertNull(SecurityAdvisoryEndpointProviderDesktop().endpoint())
        } finally {
            if (previous == null) System.clearProperty(property) else System.setProperty(property, previous)
        }
    }
}
