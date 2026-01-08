import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1" apply false
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    configure<KtlintExtension> {
        android.set(true)

        filter {
            exclude("**/build/**")
            exclude("**/generated/**")
        }
    }
}
