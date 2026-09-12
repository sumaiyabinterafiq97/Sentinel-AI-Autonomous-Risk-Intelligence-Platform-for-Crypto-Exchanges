pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "sentinel-backend"

include(
    "libs:contracts-openapi",
    "libs:contracts-events",
    "libs:common-security",
    "libs:common-observability",
    "libs:common-outbox",
    "services:platform",
    "services:identity",
    "services:ops",
    "services:dash",
)
