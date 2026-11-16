package com.crackmydroid.shared.data.securityadvisory

interface SecurityAdvisoryEndpointProvider {
    fun endpoint(): String?
}

internal fun String.asHttpsEndpointOrNull(): String? {
    val candidate = trim()
    if (!candidate.startsWith("https://")) return null
    val authority = candidate.removePrefix("https://").substringBefore('/', candidate.removePrefix("https://"))
    if (authority.isBlank() || authority.contains('@') || candidate.contains('#')) return null
    return candidate
}
