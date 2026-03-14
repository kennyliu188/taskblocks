plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    // 代码包名仍为 com.example.taskblocks，供 R 等引用使用
    // 应用实际发布包名使用 defaultConfig.applicationId（已改为 com.kennyliu.taskblocks）
    namespace = "com.example.taskblocks"
    compileSdk = 35

    // ========== 只需填写下面两个 AdMob ID ==========
    val admobAppId = "ca-app-pub-2407470402362019~2453066921"  // AdMob 应用 ID（Apps → App settings 里复制）
    val admobHomeBannerId = "ca-app-pub-2407470402362019/2061855119"           // 首页 Banner 广告位 ID
    // =============================================

    defaultConfig {
        applicationId = "com.kennyliu.taskblocks"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["adMobAppId"] = admobAppId
        buildConfigField("String", "ADMOB_HOME_BANNER_ID", "\"$admobHomeBannerId\"")
        // 广告总开关：false 关闭广告，true 开启广告
        buildConfigField("boolean", "SHOW_ADS", "false")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.google.android.gms:play-services-ads:23.6.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}