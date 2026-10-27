package com.crackmydroid.shared.data.cache

import com.crackmydroid.shared.domain.model.ManifestSecurityReport
import com.crackmydroid.shared.domain.model.AppComponent

/**
 * Wrapper di caching in-memory per ridurre accessi ripetuti al PackageManager
 * Invalida il cache quando viene richiesto esplicitamente
 */
class CachingRepositoryDecorator {
    private var manifestSecurityCache: List<ManifestSecurityReport>? = null
    private var componentsCache: List<AppComponent>? = null
    private var lastManifestSecurityTime = 0L
    private var lastComponentsTime = 0L
    private val cacheValidityMs = 60_000L

    suspend fun <T> getCachedOrCompute(
        cacheGetter: () -> T?,
        cacheSetter: (T) -> Unit,
        timeGetter: () -> Long,
        timeSetter: (Long) -> Unit,
        compute: suspend () -> T
    ): T {
        val cached = cacheGetter()
        val now = System.currentTimeMillis()
        val lastTime = timeGetter()

        return if (cached != null && (now - lastTime) < cacheValidityMs) {
            cached
        } else {
            val result = compute()
            cacheSetter(result)
            timeSetter(now)
            result
        }
    }

    fun invalidateManifestSecurityCache() {
        manifestSecurityCache = null
        lastManifestSecurityTime = 0L
    }

    fun invalidateComponentsCache() {
        componentsCache = null
        lastComponentsTime = 0L
    }

    fun cacheManifestSecurityReport(data: List<ManifestSecurityReport>) {
        manifestSecurityCache = data
        lastManifestSecurityTime = System.currentTimeMillis()
    }

    fun getManifestSecurityCache(): List<ManifestSecurityReport>? = manifestSecurityCache

    fun cacheComponents(data: List<AppComponent>) {
        componentsCache = data
        lastComponentsTime = System.currentTimeMillis()
    }

    fun getComponentsCache(): List<AppComponent>? = componentsCache
}
