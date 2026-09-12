plugins {
    `java-library`
}

dependencies {
    testImplementation(libs.junit.jupiter)
}

sourceSets {
    main {
        resources {
            srcDir(rootProject.projectDir.resolve("../contracts/openapi"))
        }
    }
}

tasks.register<Copy>("syncOpenApi") {
    from(rootProject.projectDir.resolve("../contracts/openapi"))
    into(layout.buildDirectory.dir("resources/main/contracts/openapi"))
}
