plugins {
    `java-library`
    alias(libs.plugins.spring.dependency.management)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.spring.boot.get()}")
    }
}

dependencies {
    api("org.springframework:spring-jdbc")
    api("org.springframework:spring-tx")
    api("org.springframework:spring-context")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("org.postgresql:postgresql")
    implementation("org.slf4j:slf4j-api")
    testImplementation(libs.junit.jupiter)
}
