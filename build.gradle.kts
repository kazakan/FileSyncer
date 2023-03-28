plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jetty:jetty-server:11.0.14")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.14")
    implementation("org.eclipse.jetty:jetty-servlets:11.0.14")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
}
