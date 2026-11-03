plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.mpp) apply false
    alias(libs.plugins.kover)
}

dependencies {
    kover(project(":shared"))
    kover(project(":androidApp"))
}

kover {
    currentProject {
        createVariant("coverage") {
        }
    }
    reports {
        variant("coverage") {
            html {
                htmlDir = layout.buildDirectory.dir("reports/kover/html").get()
            }
            xml {
                xmlFile = layout.buildDirectory.file("reports/kover/report.xml").get()
            }
        }
    }
}

subprojects {
    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
        resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
    }
}
