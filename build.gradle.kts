plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "6.18.0"
}

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    kotlinGradle { ktfmt().kotlinlangStyle() }
}

allprojects {
    group = "com.kazakan"

    version = "1.0-SNAPSHOT"

    repositories { mavenCentral() }

    tasks {
        withType<Tar> { duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE }
        withType<Zip> { duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE }
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("com.diffplug.spotless")
    }

    dependencies {
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    }

    tasks.getByName<Test>("test") { useJUnitPlatform() }

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
}
