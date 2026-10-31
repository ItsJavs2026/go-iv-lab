plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.goivlab.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.goivlab.app"
        minSdk = 30
        targetSdk = 35
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        versionCode = 4
        versionName = "0.4.0-overlay-experiment"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            // Overlay Lab coexists with the tested v0.2 app, no uninstall needed.
            applicationIdSuffix = ".overlaylab"
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }

    sourceSets {
        getByName("main") {
            assets.srcDir(rootProject.file("data"))
        }
    }
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(project(":iv-engine"))
    implementation(project(":nickname-core"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
