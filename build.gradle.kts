plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.18.0"
}

repositories { mavenCentral() }

tasks.test { useJUnitPlatform() }

dependencies {
    implementation("org.eclipse.jetty:jetty-server:11.0.14")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.14")
    implementation("org.eclipse.jetty:jetty-servlets:11.0.14")
    implementation("org.eclipse.jetty:apache-jsp:11.0.14")

    testImplementation(kotlin("test"))
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlin { ktfmt().kotlinlangStyle() }
    kotlinGradle { ktfmt().kotlinlangStyle() }
    javascript {
        target("src/**/*.js")

        prettier().config(mapOf("tabWidth" to 4))
    }

    format("html") {
        // you have to set the target manually
        target("src/**/*.html")

        prettier().config(mapOf("parser" to "html", "tabWidth" to 4))
    }
}
