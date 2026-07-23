import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.plugin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.google.firebase.crashlytics)
}

val keystoreProperties = Properties()
val keystorePropertiesFile = rootProject.file("keystore.properties")
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.offerlens"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.offerlens.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            val keystoreFile = rootProject.file("keystore.properties")
            if (keystoreFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
            // Production AdMob ID
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-7195572820017273~2445725500\"")
            // Placeholders - REPLACE WITH REAL IDs
            buildConfigField("String", "ADMOB_BANNER_ID", "\"INSERT_RELEASE_BANNER_ID\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"INSERT_RELEASE_INTERSTITIAL_ID\"")
            
            // Affiliate Keys
            buildConfigField("String", "AFFILIATE_CUELINKS_KEY", "\"INSERT_CUELINKS_KEY_HERE\"")
            buildConfigField("String", "AFFILIATE_VCOMMISSION_KEY", "\"INSERT_VCOMMISSION_KEY_HERE\"")
            buildConfigField("String", "AFFILIATE_AMAZON_TAG", "\"INSERT_AMAZON_TAG_HERE\"")

            manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-7195572820017273~2445725500"
        }
        debug {
            // Test AdMob ID
            buildConfigField("String", "ADMOB_APP_ID", "\"ca-app-pub-3940256099942544~3347511713\"")
            // Test Unit IDs
            buildConfigField("String", "ADMOB_BANNER_ID", "\"ca-app-pub-3940256099942544/6300978111\"")
            buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"ca-app-pub-3940256099942544/1033173712\"")

            // Affiliate Keys (Test or Dev)
            buildConfigField("String", "AFFILIATE_CUELINKS_KEY", "\"TEST_CUELINKS_KEY\"")
            buildConfigField("String", "AFFILIATE_VCOMMISSION_KEY", "\"TEST_VCOMMISSION_KEY\"")
            buildConfigField("String", "AFFILIATE_AMAZON_TAG", "\"TEST_AMAZON_TAG\"")

            manifestPlaceholders["ADMOB_APP_ID"] = "ca-app-pub-3940256099942544~3347511713"
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
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended) // Required for Diamond, Wallet, etc.
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    // implementation(libs.play.services.auth) // Causing Hilt build errors

    // Freemium (Billing & Ads)
    implementation(libs.billing.ktx)
    implementation(libs.play.services.ads)
    implementation(libs.datastore.preferences)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    // Timber for better logging
    implementation(libs.timber)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Image Loading
    implementation(libs.coil.compose)
    
    // SQLCipher for database encryption
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite.ktx)
}
