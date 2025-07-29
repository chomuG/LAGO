plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tradingview.lightweightcharts"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
        targetSdk = 35

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
}

// npm 빌드 태스크 (선택적)
val npmInstall by tasks.registering {
    doLast {
        try {
            exec {
                workingDir(projectDir)
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    commandLine("cmd", "/c", "npm", "install")
                } else {
                    commandLine("npm", "install")
                }
            }
        } catch (e: Exception) {
            println("npm install failed: ${e.message}")
        }
    }
}

val npmBuild by tasks.registering {
    dependsOn(npmInstall)
    doLast {
        try {
            val commands = listOf("compile", "compile-price-formatter", "compile-time-formatter", "compile-eval-plugin")
            commands.forEach { command ->
                exec {
                    workingDir(projectDir)
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        commandLine("cmd", "/c", "npm", "run", command)
                    } else {
                        commandLine("npm", "run", command)
                    }
                }
            }
        } catch (e: Exception) {
            println("npm build failed: ${e.message}")
        }
    }
}

// 빌드 시 npm이 실행되지 않도록 주석 처리 (필요시 활성화)
// tasks.named("preBuild") {
//     dependsOn(npmBuild)
// }

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.21")
    implementation("androidx.webkit:webkit:1.11.0")
    implementation("com.google.code.gson:gson:2.11.0")
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}