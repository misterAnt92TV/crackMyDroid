package com.crackmydroid.shared.data.securityadvisory

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SecurityAdvisoryEndpointProviderTest {
    @Test
    fun `accepts only trimmed https endpoint`() {
        assertEquals("https://security.example.test/v1/advisories", " https://security.example.test/v1/advisories ".asHttpsEndpointOrNull())
        assertNull("http://security.example.test/v1/advisories".asHttpsEndpointOrNull())
        assertNull("file:///tmp/advisories".asHttpsEndpointOrNull())
        assertNull("https://user:password@security.example.test/advisories".asHttpsEndpointOrNull())
        assertNull("https:///advisories".asHttpsEndpointOrNull())
        assertNull("https://security.example.test/advisories#fragment".asHttpsEndpointOrNull())
    }

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
