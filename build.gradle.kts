import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask

plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.compose.mpp) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint)
}

allprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        version.set(rootProject.libs.versions.ktlint.get())
        relative.set(true)
        if (providers.gradleProperty("ktlintFullReport").map(String::toBoolean).getOrElse(false)) {
            baseline.unsetConvention()
        } else {
            baseline.set(layout.projectDirectory.file("ktlint-baseline.xml").asFile)
        }
        reporters {
            reporter(ReporterType.CHECKSTYLE)
            reporter(ReporterType.SARIF)
        }
        filter {
            exclude("**/build/**", "**/generated/**")
            exclude {
                it.file.toPath().startsWith(layout.buildDirectory.get().asFile.toPath()) ||
                    it.file.invariantSeparatorsPath.contains("/generated/")
            }
        }
    }
    tasks.withType<BaseKtLintCheckTask>().configureEach {
        inputs.property(
            "excludedBuildDirectory",
            layout.buildDirectory.map { it.asFile.relativeTo(rootProject.projectDir).invariantSeparatorsPath },
        )
    }
}

val ktlintCli by configurations.creating {
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}
val lintPaths = providers.gradleProperty("ktlintFiles").map { it.split(",") }.orElse(emptyList())

listOf("ktlintFilesCheck", "ktlintFilesFormat").forEach { taskName ->
    tasks.register<JavaExec>(taskName) {
        group = if (taskName == "ktlintFilesFormat") "formatting" else "verification"
        description = "Runs pinned ktlint on the repository-relative paths supplied by -PktlintFiles."
        classpath = ktlintCli
        mainClass.set("com.pinterest.ktlint.Main")
        workingDir = rootDir
        inputs.files(lintPaths.map { paths -> paths.map { file(it) } })
        inputs.file(".editorconfig")
        doFirst {
            val paths = lintPaths.get()
            require(paths.isNotEmpty()) { "Supply Kotlin paths with -PktlintFiles=path/File.kt,path/Other.kt" }
            val sources = paths.map { path ->
                val source = file(path).canonicalFile
                require(
                    source.toPath().startsWith(rootDir.canonicalFile.toPath()) &&
                        source.isFile && (path.endsWith(".kt") || path.endsWith(".kts")),
                ) { "Not a maintained Kotlin file inside the repository: $path" }
                val relativePath = source.relativeTo(rootDir.canonicalFile).invariantSeparatorsPath
                require(relativePath.split("/").none { it == "build" || it == "generated" }) {
                    "Generated build output must not be linted: $path"
                }
                relativePath
            }
            setArgs(
                listOf("--relative") +
                    (if (taskName == "ktlintFilesFormat") listOf("--format") else emptyList()) +
                    listOf("--") + sources,
            )
        }
    }
}

dependencies {
    ktlintCli(libs.ktlint.cli)
    kover(project(":architectureModule"))
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
    configurations.configureEach {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
        resolutionStrategy.cacheDynamicVersionsFor(0, "seconds")
    }
}
