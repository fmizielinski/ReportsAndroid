import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kover)
    alias(libs.plugins.serialization)
}

android {
    namespace = "pl.fmizielinski.reports"
    compileSdk = 34

    defaultConfig {
        applicationId = "pl.fmizielinski.reports"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "HOST", "${project.properties["hostDev"]}")
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
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ktlint {
    android.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
        reporters {
            reporter(ReporterType.CHECKSTYLE)
            reporter(ReporterType.HTML)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.serialization)

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
}

ksp {
    // .mmd file
    arg("compose-destinations.mermaidGraph", "$rootDir/docs")
    // .html file
    arg("compose-destinations.htmlMermaidGraph", "$rootDir/docs")
}

koverReport {
    filters {
        excludes {
            classes(
                "*ReportsApplication*",
                "*Activity*",
                "*BuildConfig*",
                "*ComposableSingletons*",
                "*Ext*",
                "*Screen*",
            )
            packages(
                "*.model",
                "org.koin.ksp.generated",
                "pl.fmizielinski.reports.di",
                "pl.fmizielinski.reports.ui.base",
                "pl.fmizielinski.reports.ui.theme",
            )
            annotatedBy(
                "*Generated*",
                "*Composable*",
            )
        }
    }
}
