import io.gitlab.arturbosch.detekt.Detekt

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kover)
    alias(libs.plugins.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.parcelize)
}

android {
    namespace = "pl.fmizielinski.reports"
    compileSdk = 34

    defaultConfig {
        applicationId = "pl.fmizielinski.reports"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            type = "String",
            name = "HOST",
            value = "${project.properties["hostDev"]}",
        )
        buildConfigField(
            type = "int",
            name = "REPORT_DESCRIPTION_LENGTH",
            value = "${project.properties["reportDescriptionLength"]}",
        )
        buildConfigField(
            type = "int",
            name = "REPORT_TITLE_LENGTH",
            value = "${project.properties["reportTitleLength"]}",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
}

detekt {
    source.setFrom("src/main/java")
    config.setFrom("../config/detekt/detekt.yml")
}

tasks.withType<Detekt>().configureEach {
    reports {
        md.required.set(false)
        txt.required.set(false)
    }
}

dependencies {
    implementation(libs.accompanist)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.serialization)
    implementation(libs.androidx.splashScreen)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.activity)
    implementation(libs.bundles.compose)
    debugImplementation(libs.androidx.ui.tooling)
    ksp(libs.compose.destinations.ksp)

    // Koin
    implementation(libs.bundles.koin)
    ksp(libs.koin.compiler)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // Coroutines
    implementation(libs.coroutines)

    // Navigation
    implementation(libs.bundles.navigation)

    // Timber
    implementation(libs.timber)

    // Retrofit
    implementation(libs.bundles.retrofit)
    implementation(libs.okHttp)

    // Glide
    implementation(libs.bundles.glide)

    testImplementation(libs.bundles.test.strikt)
    testImplementation(libs.test.arch.core)
    testImplementation(libs.test.coroutines)
    testImplementation(libs.bundles.test.junit)
    testImplementation(libs.test.mockk)
    testImplementation(libs.test.turbine)
    testRuntimeOnly(libs.test.junit.jupiter.engine)
}

ksp {
    // region Compose Destinations
    arg("compose-destinations.mermaidGraph", "$rootDir/docs")
    arg("compose-destinations.htmlMermaidGraph", "$rootDir/docs")
    arg("compose-destinations.codeGenPackageName", "pl.fmizielinski.reports.ui.destinations")
    // endregion Compose Destinations
}

koverReport {
    filters {
        excludes {
            classes(
                "*Activity*",
                "*BuildConfig*",
                "*Callbacks*",
                "*ComposableSingletons*",
                "*ErrorMappersKt*",
                "*Ext*",
                "*Interceptor*",
                "*MainViewModelKt*",
                "*NumberKt*",
                "*ReportsApplication*",
                "*ReportsDatabase*",
                "*Screen*",
                "*Serializer*",
            )
            packages(
                "*.model",
                "org.koin.ksp.generated",
                "pl.fmizielinski.reports.data.db.dao",
                "pl.fmizielinski.reports.data.network.utils",
                "pl.fmizielinski.reports.di",
                "pl.fmizielinski.reports.domain.base",
                "pl.fmizielinski.reports.domain.error",
                "pl.fmizielinski.reports.ui.base",
                "pl.fmizielinski.reports.ui.common.composable",
                "pl.fmizielinski.reports.ui.destinations",
                "pl.fmizielinski.reports.ui.theme",
                "pl.fmizielinski.reports.ui.utils",
            )
            annotatedBy(
                "*Generated*",
                "*Composable*",
            )
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
