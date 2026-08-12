import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val versionProperties = Properties().apply {
    val file = rootProject.file("version.properties")
    if (file.exists()) {
        load(file.inputStream())
    }
}

val versionMajor = (versionProperties.getProperty("VERSION_MAJOR") ?: "1").toInt()
val versionMinor = (versionProperties.getProperty("VERSION_MINOR") ?: "0").toInt()
val versionPatch = (versionProperties.getProperty("VERSION_PATCH") ?: "4").toInt()

val compileSdkVer = (versionProperties.getProperty("COMPILE_SDK") ?: "37").toInt()
val targetSdkVer = (versionProperties.getProperty("TARGET_SDK") ?: "37").toInt()
val minSdkVer = (versionProperties.getProperty("MIN_SDK") ?: "26").toInt()

val appVersionName = "$versionMajor.$versionMinor.$versionPatch"
val appVersionCode = versionMajor * 10000 + versionMinor * 100 + versionPatch

fun readVersionProperties(): Properties = Properties().apply {
    rootProject.file("version.properties").inputStream().use { load(it) }
}

fun formatVersionName(props: Properties): String =
    "${props.getProperty("VERSION_MAJOR")}.${props.getProperty("VERSION_MINOR")}.${props.getProperty("VERSION_PATCH")}"

fun writeVersionProperties(props: Properties) {
    val file = rootProject.file("version.properties")
    file.writeText(
        buildString {
            appendLine("VERSION_MAJOR=${props.getProperty("VERSION_MAJOR")}")
            appendLine("VERSION_MINOR=${props.getProperty("VERSION_MINOR")}")
            appendLine("VERSION_PATCH=${props.getProperty("VERSION_PATCH")}")
            appendLine("COMPILE_SDK=${props.getProperty("COMPILE_SDK")}")
            appendLine("TARGET_SDK=${props.getProperty("TARGET_SDK")}")
            appendLine("MIN_SDK=${props.getProperty("MIN_SDK")}")
        }
    )
}

tasks.register("printVersion") {
    group = "versioning"
    description = "Imprime versionName lido de version.properties"
    doLast {
        println(formatVersionName(readVersionProperties()))
    }
}

fun formatVersionCode(props: Properties): Int {
    val major = props.getProperty("VERSION_MAJOR").toInt()
    val minor = props.getProperty("VERSION_MINOR").toInt()
    val patch = props.getProperty("VERSION_PATCH").toInt()
    return major * 10000 + minor * 100 + patch
}

tasks.register("printVersionCode") {
    group = "versioning"
    description = "Imprime versionCode calculado de version.properties"
    doLast {
        println(formatVersionCode(readVersionProperties()))
    }
}

tasks.register("incrementPatch") {
    group = "versioning"
    description = "Incrementa a versão patch no arquivo version.properties"
    doLast {
        val props = readVersionProperties()
        val newPatch = props.getProperty("VERSION_PATCH").toInt() + 1
        props.setProperty("VERSION_PATCH", newPatch.toString())
        writeVersionProperties(props)
        println("VERSION_NAME=${formatVersionName(props)}")
    }
}

tasks.register("syncReleaseVersion") {
    group = "versioning"
    description = "Sincroniza version.properties com um versionCode alvo (CI/Play Store)"
    doLast {
        val targetCode = project.findProperty("targetVersionCode")?.toString()?.toInt()
            ?: System.getenv("TARGET_VERSION_CODE")?.toInt()
            ?: error("Defina targetVersionCode ou TARGET_VERSION_CODE")

        val props = readVersionProperties()
        val major = props.getProperty("VERSION_MAJOR").toInt()
        val minor = props.getProperty("VERSION_MINOR").toInt()
        val patch = targetCode - (major * 10000 + minor * 100)
        require(patch > 0) {
            "versionCode $targetCode invalido para $major.$minor.x"
        }

        props.setProperty("VERSION_PATCH", patch.toString())
        writeVersionProperties(props)
        println("VERSION_NAME=${formatVersionName(props)}")
        println("VERSION_CODE=$targetCode")
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
    compileSdk = compileSdkVer

    defaultConfig {
        applicationId = "com.presencial.app"
        minSdk = minSdkVer
        @Suppress("ExpiredTargetSdkVersion")
        targetSdk = targetSdkVer

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

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
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
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
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
    implementation(libs.androidx.security.crypto)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Location & Permissions
    implementation(libs.play.services.location)
    implementation(libs.accompanist.permissions)

    // Glance (App Widgets)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // UI Libraries & Utils
    implementation(libs.json.library)
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
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.junit)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Android Instrumentation Tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.androidx.test.core)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "**.BuildConfig", "**.Manifest", "**.R", "**.R$*",
                    "**.PresencialApp", "**.MainActivity"
                )
                classes(
                    "dagger.hilt.**", "hilt_aggregated_deps.**", "**.di.**",
                    "**.*Hilt_*", "**.*_HiltModules*", "**.*_Factory",
                    "**.*_MembersInjector"
                )
                classes(
                    "**.data.local.dao.**", "**.data.local.entity.**",
                    "**.data.local.mapper.**", "**.data.local.converter.**",
                    "**.data.preferences.**", "**.domain.model.**",
                    "**.*Database*", "**.*SettingsDataStore*"
                )
                classes(
                    "**.ui.**", "**.presentation.**", "**.*ComposableSingletons*",
                    "**.*Preview*", "**.*Screen*", "**.*Activity*", "**.*DialogState*"
                )
                classes(
                    "**.notification.**", "**.*WidgetReceiver*",
                    "**.*BasePresencialWidget*", "**.*WidgetSmall*",
                    "**.*WidgetMedium*", "**.*WidgetLarge*", "**.widget.**",
                    "**.worker.**", "**.data.export.**", "**.domain.location.**",
                    "**.data.location.**", "**.data.local.migration.**",
                    "**.*_Impl*", "**.*WidgetColors*"
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