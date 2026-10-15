plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
android {
    namespace = "com.example.ragdolllauncher"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.example.ragdolllauncher"
        minSdk = 26
        targetSdk = 29   // keeps legacy storage so Winlator/GameHub can read the extracted folder (P30 = Android 9/10)
        versionCode = 1; versionName = "1.0"
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    lint { abortOnError = false }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}
dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.github.junrar:junrar:7.5.5")   // pure-Java RAR reader (RAR4 only)
    implementation("org.slf4j:slf4j-nop:1.7.36")
}
