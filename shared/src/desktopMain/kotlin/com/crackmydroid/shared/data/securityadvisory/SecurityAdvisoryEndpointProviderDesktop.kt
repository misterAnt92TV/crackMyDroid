package com.crackmydroid.shared.data.securityadvisory

class SecurityAdvisoryEndpointProviderDesktop : SecurityAdvisoryEndpointProvider {
    override fun endpoint(): String? = System.getProperty(ENDPOINT_PROPERTY)?.asHttpsEndpointOrNull()

    private companion object {
        const val ENDPOINT_PROPERTY = "crackmydroid.securityAdvisoryEndpoint"
    }
}
