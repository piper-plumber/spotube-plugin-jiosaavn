import com.codingfeline.buildkonfig.compiler.FieldSpec
import dev.krtirtho.PluginAbility
import dev.krtirtho.PluginCapability
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.zipline.gradle.plugin)
    alias(libs.plugins.spotubeGradle)
    id("com.codingfeline.buildkonfig") version "0.18.0"
}

spotubePlugin {
    name = "JioSaavn"
    version = "0.1.0"
    apiVersion = "0.0.1"
    description = "JioSaavn plugin for Spotube"
    author = "Piper Plumber"
    capabilities = listOf(
        PluginCapability.NETWORK_REQUESTS,
        PluginCapability.PERSISTENT_STORAGE,
        PluginCapability.WEBVIEW
    )
    abilities = listOf(PluginAbility.METADATA)
    license = "AGPL-3.0-or-later"
    contact = "308775074+piper-plumber@users.noreply.github.com"
    repository = "https://github.com/piper.plumber/spotube-plugin-jiosaavn"
    bugs = "https://github.com/piper.plumber/spotube-plugin-jiosaavn/issues"
}


buildkonfig {
    val localProperties = Properties().apply {
        load(project.file("local.properties").inputStream())
    }
    val cookie: String = localProperties["cookie"] as String?
        ?: throw Exception("cookie not found in local.properties")


    packageName = "io.github.piper_plumber.spotube_plugin_jiosaavn"
    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "COOKIE", cookie)
    }
}

kotlin {
    applyDefaultHierarchyTemplate()

    js {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            api(libs.zipline.core)
            implementation(libs.spotube.plugin.interfaces)
            /**
             * ktor-client-logging causes Zipline compileDevelopmentExecutableKotlinJsZipline to
             * fail with "unconsistent stack size: 2 3" quickjs error. This was a bug that has
             * been fixed but Zipline still uses QuickJS from 2021, so the fix is not available
             * in Zipline yet.
             * https://github.com/bellard/quickjs/issues/119
             *
             * The problem is caused by a few control-flow statements, that causes the above pattern
             * to be generated in the compiled JavaScript code.
             * - https://github.com/ktorio/ktor/blob/2413814d35be26247e5dd20613522098b6f47797/ktor-client/ktor-client-plugins/ktor-client-logging/common/src/io/ktor/client/plugins/logging/Logging.kt#L530
             * - https://github.com/ktorio/ktor/blob/2413814d35be26247e5dd20613522098b6f47797/ktor-client/ktor-client-plugins/ktor-client-logging/common/src/io/ktor/client/plugins/logging/Logging.kt#L588
             */
            // implementation(libs.ktor.client.logging)
            // implementation(libs.ktor.client.encoding)
            implementation(libs.semver)
        }

        jsMain.dependencies {
        }

        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.optimal)
        }

        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.swing)
        }

    }
}

zipline {
    mainFunction.set("io.github.piper_plumber.spotube_plugin_jiosaavn.main")
}

plugins.withType<YarnPlugin> {
    the<YarnRootExtension>().yarnLockAutoReplace = true
}

