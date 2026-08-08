import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val versionProperties = Properties().apply {
    load(rootProject.file("version.properties").inputStream())
}

val versionMajor = versionProperties.getProperty("VERSION_MAJOR").toInt()
val versionMinor = versionProperties.getProperty("VERSION_MINOR").toInt()
val versionPatch = versionProperties.getProperty("VERSION_PATCH").toInt()

val appVersionName = "$versionMajor.$versionMinor.$versionPatch"
// Gera um versionCode único baseado na versão (ex: 1.0.4 -> 10004)
val appVersionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch

// Task para incrementar o patch automaticamente
tasks.register("incrementPatch") {
    group = "versioning"
    description = "Incrementa a versão patch no arquivo version.properties"
    doLast {
        val props = Properties()
        val file = rootProject.file("version.properties")
        props.load(file.inputStream())
        val currentPatch = props.getProperty("VERSION_PATCH").toInt()
        props.setProperty("VERSION_PATCH", (currentPatch + 1).toString())
        props.store(file.writer(), "Gerado automaticamente")
        println("Versão atualizada para Patch: ${currentPatch + 1}")
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
}



android {
    namespace = "com.presencial.app"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.presencial.app"

        minSdk = 26
        @Suppress("ExpiredTargetSdkVersion")
        targetSdk = 37

        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Jetpack Compose BOM
    implementation(platform(libs.androidx.compose.bom))

    // AndroidX & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Jetpack Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Dependency Injection (Hilt)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.work.compiler)

    // Database (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Location & Permissions
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)

    // Glance (App Widgets)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // UI Libraries & Utils
    implementation(libs.lottie.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Unit Testing
    testImplementation(libs.json.library)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Android Instrumentation Tests
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.ui.tooling)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kover {
    reports {
        filters {
            excludes {
                // Android e Boilerplate base
                classes(
                    "**.BuildConfig",
                    "**.Manifest",
                    "**.R",
                    "**.R$*",
                    "**.PresencialApp",
                    "**.MainActivity"
                )

                // Injeção de Dependência (Hilt)
                classes(
                    "dagger.hilt.**",
                    "hilt_aggregated_deps.**",
                    "**.di.**",
                    "**.*Hilt_*",
                    "**.*_HiltModules*",
                    "**.*_Factory",
                    "**.*_MembersInjector"
                )

                // Persistência e Modelos (Sem lógica de negócio)
                classes(
                    "**.data.local.dao.**",
                    "**.data.local.entity.**",
                    "**.data.local.mapper.**",
                    "**.data.local.converter.**",
                    "**.data.preferences.**",
                    "**.domain.model.**",
                    "**.*Database*"
                )

                // UI e Navegação
                classes(
                    "**.ui.**",
                    "**.presentation.**.components.**",
                    "**.presentation.navigation.**",
                    "**.*ComposableSingletons*",
                    "**.*Preview*",
                    "**.*Screen*",
                    "**.*Activity*",
                    "**.*DialogState*"
                )

                // Framework Glue (Notificações, Widgets, Workers, Export, Location)
                classes(
                    "**.notification.**",
                    "**.widget.**",
                    "**.worker.**",
                    "**.data.export.**",
                    "**.domain.location.**"
                )
            }
        }

        verify {
            rule {
                minBound(80)
            }
        }
    }
}