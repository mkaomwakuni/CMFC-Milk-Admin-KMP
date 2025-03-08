import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.1.21"
}

kotlin {
    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting
        val commonTest by getting
        val desktopMain by getting
        val wasmJsMain by getting

        // Common dependencies across all platforms
        commonMain.dependencies {
            // Compose Multiplatform core
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")
            implementation("org.jetbrains.compose.animation:animation:1.5.4")
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // State management and lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Coroutines for async operations
            implementation(libs.kotlinx.coroutines.core)

            // Serialization for data handling
            implementation(libs.kotlinx.serialization.json)

            // Date and time handling
            implementation(libs.kotlinx.datetime)

            // Navigation - kept in common but removed voyager
            implementation(libs.decompose)
            implementation(libs.decompose.compose)

            // Koin Core
            implementation(libs.koinCore)
            implementation(libs.koinCompose)

            // Networking core
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        // Desktop-specific dependencies
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing) // UI thread handling

            // Ktor Client for JVM
            implementation(libs.ktor.client.cio)

            // Database (Exposed with SQLite)
            implementation(libs.exposed.core)
            implementation(libs.exposed.dao)
            implementation(libs.exposed.jdbc)
            implementation(libs.sqlite.jdbc)

            // Logging
            implementation(libs.kotlin.logging)

            // Voyager Navigation - moved to desktop
            implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
            implementation("cafe.adriel.voyager:voyager-screenmodel:1.0.0")
            implementation("cafe.adriel.voyager:voyager-koin:1.0.0")

            // Charts - moved to desktop
            implementation("io.github.thechance101:chart:Beta-0.0.5")

            // CIO Client - moved to desktop
            implementation("io.ktor:ktor-client-cio:2.3.6")
            implementation("io.ktor:ktor-client-logging:2.3.6")
        }

        // Web-specific dependencies
        wasmJsMain.dependencies {
            // Ktor Client for JS
            implementation(libs.ktor.client.js)
        }

        // Test dependencies
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        // Desktop test dependencies only
        val desktopTest by getting {
            dependencies {
                implementation(libs.mockk)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "cnc.coop.milkcreamies.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "cnc.coop.milkcreamies"
            packageVersion = "1.0.0"
        }
    }
}
