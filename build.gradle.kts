import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
}

ktlint {
    android.set(false)

    filter {
        exclude { element ->
            element.file.toString().contains("generated")
        }
        exclude("**/build/**")
        exclude("**/target/**")
    }

    reporters {
        reporter(PLAIN)
        reporter(CHECKSTYLE)
    }

    verbose.set(true)
    outputToConsole.set(true)
}
