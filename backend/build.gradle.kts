plugins {
    java
}

allprojects {
    group = "com.sentinel"
    version = "0.0.1-m0"
}

subprojects {
    repositories {
        mavenCentral()
    }
}

val javaVersion = JavaLanguageVersion.of(21)

subprojects {
    pluginManager.withPlugin("java") {
        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(javaVersion)
            }
        }
        tasks.named<Test>("test") {
            useJUnitPlatform()
        }
    }
}
