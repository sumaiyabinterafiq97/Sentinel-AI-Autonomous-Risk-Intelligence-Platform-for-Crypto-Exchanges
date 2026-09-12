plugins {
    `java-library`
}

dependencies {
    testImplementation(libs.junit.jupiter)
}

sourceSets {
    main {
        resources {
            srcDir(rootProject.projectDir.resolve("../contracts/events"))
        }
    }
}
