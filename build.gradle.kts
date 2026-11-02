plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.mpp) apply false
    alias(libs.plugins.kover)
}

subprojects {
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
        resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
    }
}

allprojects {
    tasks.withType<Test>().configureEach {
        ignoreFailures = true
    }
}

kover {
    reports {
        total {
            html {
                onCheck.set(false)
            }
            xml {
                onCheck.set(false)
            }
        }
    }
}
