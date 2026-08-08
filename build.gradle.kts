import io.gitlab.arturbosch.detekt.Detekt
import java.util.Properties


val versionProperties = Properties().apply {
    load(rootProject.file("version.properties").inputStream())
}

val versionMajor = versionProperties["VERSION_MAJOR"].toString().toInt()
val versionMinor = versionProperties["VERSION_MINOR"].toString().toInt()
val versionPatch = versionProperties["VERSION_PATCH"].toString().toInt()

val versionName = "$versionMajor.$versionMinor.$versionPatch"

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.sonarqube)
}

subprojects {
    tasks.withType<Detekt>().configureEach {
        jvmTarget = "17"
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}
sonar {
    properties {

        property("sonar.projectKey", "marciacrisrs_presencial-app")
        property("sonar.organization", "marciacrisrs")
        property("sonar.host.url", "https://sonarcloud.io")

        property(
            "sonar.sources",
            rootProject.layout.projectDirectory
                .dir("app/src/main/java")
                .asFile.absolutePath
        )

        property(
            "sonar.tests",
            rootProject.layout.projectDirectory
                .dir("app/src/test")
                .asFile.absolutePath
        )

        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            rootProject.layout.projectDirectory
                .file("app/build/reports/kover/report.xml")
                .asFile.absolutePath
        )

        property(
            "sonar.kotlin.detekt.reportPaths",
            rootProject.layout.projectDirectory
                .file("app/build/reports/detekt/detekt.xml")
                .asFile.absolutePath
        )

        property(
            "sonar.androidLint.reportPaths",
            rootProject.layout.projectDirectory
                .file("app/build/reports/lint-results-debug.xml")
                .asFile.absolutePath
        )

        property("sonar.sourceEncoding", "UTF-8")
    }
}