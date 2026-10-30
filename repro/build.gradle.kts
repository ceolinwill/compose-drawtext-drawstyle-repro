plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Run against another Compose release with -Pcompose=<version>, for example -Pcompose=1.13.0-beta02.
val compose = providers.gradleProperty("compose").getOrElse("1.12.1")

android {
    namespace = "repro"
    compileSdk = 37
    // Compose 1.13 needs API 37.1 or later.
    compileSdkMinor = 2

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { test ->
            test.testLogging.showStandardStreams = true
            // Robolectric reaches into the JDK's internals.
            test.jvmArgs(
                "--add-opens=java.base/java.io=ALL-UNNAMED",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.util=ALL-UNNAMED",
                "--add-opens=java.base/jdk.internal.access=ALL-UNNAMED",
                "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
            )
        }
    }
}

dependencies {
    implementation("androidx.compose.foundation:foundation:$compose")
    implementation("androidx.compose.ui:ui:$compose")
    testImplementation("androidx.compose.ui:ui-test-junit4:$compose")
    testImplementation("androidx.compose.ui:ui-test-manifest:$compose")
    testImplementation("androidx.test.espresso:espresso-core:3.7.0")
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
}
