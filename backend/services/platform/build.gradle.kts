plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    java
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.actuator)
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.postgresql:postgresql")
    implementation(project(":libs:common-security"))
    implementation(project(":libs:common-outbox"))
    implementation(project(":libs:common-observability"))
    implementation(project(":libs:contracts-openapi"))
    implementation(project(":libs:contracts-events"))
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("io.zonky.test:embedded-postgres:2.1.0")
}
