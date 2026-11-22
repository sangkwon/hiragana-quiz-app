plugins {
    alias(libs.plugins.android.application)
    id("org.jetbrains.kotlin.plugin.compose")
    jacoco
}

android {
    namespace = "com.example.hiraganapractice"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.hiraganapractice"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

// Robolectric은 자체 클래스로더로 클래스를 다시 로드하므로, 이 설정이 없으면 JaCoCo가 해당 코드를 0%로 집계한다.
tasks.withType<Test>().configureEach {
    extensions.configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// ---- 커버리지 리포트 (JaCoCo) ----
// 단위 테스트: ./gradlew test && ./gradlew jacocoUnitReport
// UI 테스트:   ./gradlew connectedDebugAndroidTest && ./gradlew jacocoUiReport
// 합산:        ./gradlew jacocoAllReport
fun registerCoverageReport(taskName: String, execDirs: List<String>, runUnitTests: Boolean) {
    tasks.register<JacocoReport>(taskName) {
        group = "verification"
        dependsOn("compileDebugKotlin")
        if (runUnitTests) dependsOn("testDebugUnitTest")
        classDirectories.setFrom(
            fileTree(layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")) {
                exclude("**/R.class", "**/R$*.class", "**/BuildConfig.*")
            }
        )
        sourceDirectories.setFrom("src/main/java")
        executionData.setFrom(
            execDirs.map { dir ->
                fileTree(layout.buildDirectory.dir(dir)) { include("**/*.exec", "**/*.ec") }
            }
        )
        reports {
            xml.required.set(true)
            html.required.set(true)
            html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/$taskName/html"))
            xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/$taskName/report.xml"))
        }
    }
}
registerCoverageReport("jacocoUnitReport", listOf("outputs/unit_test_code_coverage"), true)
registerCoverageReport("jacocoUiReport", listOf("outputs/code_coverage"), false)
registerCoverageReport("jacocoAllReport", listOf("outputs/unit_test_code_coverage", "outputs/code_coverage"), true)
