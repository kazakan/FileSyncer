plugins {
    `kotlin-dsl`
    id("org.jlleitschuh.gradle.ktlint") version "11.3.1"
}

repositories {
    mavenCentral()
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:11.0.14")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.14")
    implementation("org.eclipse.jetty:jetty-servlets:11.0.14")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")

    testImplementation(kotlin("test"))
}
