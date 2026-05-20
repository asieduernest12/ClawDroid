import java.net.URL
import java.io.InputStream
import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

tasks.register<DefaultTask>("downloadTermuxBootstraps") {
    description = "Download Termux bootstrap ZIPs for all architectures into assets"
    group = "build setup"
    val bootstrapBaseUrl = "https://github.com/termux/termux-packages/releases/latest/download"
    val bootstrapDir = file("src/main/assets/termux/bootstrap")

    outputs.dir(bootstrapDir)

    doLast {
        bootstrapDir.mkdirs()
        for (arch in listOf("aarch64", "arm", "x86_64", "i686")) {
            val zipFile = bootstrapDir.resolve("bootstrap-${arch}.zip")
            if (zipFile.exists()) {
                logger.lifecycle("Termux bootstrap for $arch already cached at ${zipFile}")
                continue
            }
            val url = URL("${bootstrapBaseUrl}/bootstrap-${arch}.zip")
            logger.lifecycle("Downloading Termux bootstrap ($arch) from $url ...")
            val input: InputStream = url.openStream()
            try {
                zipFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } finally {
                input.close()
            }
            logger.lifecycle("Downloaded ${zipFile} (${zipFile.length()} bytes)")
        }
    }
}

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }.configureEach {
    dependsOn("downloadTermuxBootstraps")
}

tasks.matching { it.name.contains("LintVital") }.configureEach {
    dependsOn("downloadTermuxBootstraps")
}

android {
    namespace = "com.example.clawdroid"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.clawdroid"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val envFile = rootProject.file(".env")
    val openRouterKey = if (envFile.exists()) {
        try {
            val props = Properties()
            FileInputStream(envFile).use { props.load(it) }
            props.getProperty("OPENROUTER_API_KEY", "")
        } catch (e: Exception) {
            ""
        }
    } else ""

    buildTypes {
        debug {
            buildConfigField("String", "OPENROUTER_API_KEY", "\"${openRouterKey}\"")
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "OPENROUTER_API_KEY", "\"\"")
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("org.nanohttpd:nanohttpd:2.3.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.json:json:20231013")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("com.tngtech.jgiven:jgiven-junit:2.0.3") {
        exclude(group = "xml-apis")
    }
    androidTestImplementation("com.tngtech.jgiven:jgiven-html5-report:2.0.3") {
        exclude(group = "xml-apis")
    }
}
