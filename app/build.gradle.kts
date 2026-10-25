plugins {
 alias(libs.plugins.android.application)
 alias(libs.plugins.kotlin.android)
 alias(libs.plugins.kotlin.compose)
 alias(libs.plugins.kotlin.serialization)
 alias(libs.plugins.ksp)
 alias(libs.plugins.hilt)
}
android {
 namespace = "vn.chitieu"
 compileSdk = 37
 defaultConfig { applicationId = "vn.chitieu"; minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "1.0.0"; testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" }
 buildFeatures { compose = true; buildConfig = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 buildTypes { release { isMinifyEnabled = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
 testOptions { unitTests.isReturnDefaultValues = true }
 lint { abortOnError = true; checkReleaseBuilds = true }
}
kotlin { jvmToolchain(17) }
ksp { arg("room.schemaLocation", "$projectDir/schemas") }
dependencies {
 implementation(platform(libs.compose.bom))
 implementation(libs.material3); implementation(libs.icons); debugImplementation(libs.ui.tooling)
 implementation(libs.activity); implementation(libs.core); implementation(libs.navigation)
 implementation(libs.lifecycle.runtime); implementation(libs.lifecycle.viewmodel); implementation(libs.lifecycle.process)
 implementation(libs.room.runtime); implementation(libs.room.ktx); ksp(libs.room.compiler)
 implementation(libs.hilt.android); ksp(libs.hilt.compiler); implementation(libs.hilt.navigation)
 implementation(libs.datastore); implementation(libs.work)
 implementation(libs.camera.core); implementation(libs.camera.camera2); implementation(libs.camera.lifecycle); implementation(libs.camera.view)
 implementation(libs.zxing); implementation(libs.biometric); implementation(libs.serialization); implementation(libs.coroutines)
 testImplementation(libs.junit); testImplementation(libs.coroutines.test); testImplementation(libs.mockk); testImplementation(libs.turbine)
 androidTestImplementation(libs.room.testing); androidTestImplementation(libs.testext); androidTestImplementation(libs.coroutines.test)
}

