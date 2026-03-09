plugins {
    idea
    alias(libs.plugins.kotlin)
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.publish) apply false
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

allprojects {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

subprojects {
    apply(
        plugin =
            rootProject.libs.plugins.kotlin
                .get()
                .pluginId,
    )
    apply(
        plugin =
            rootProject.libs.plugins.serialization
                .get()
                .pluginId,
    )

    repositories {
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    dependencies {
        compileOnly(rootProject.libs.paper)

        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        implementation(rootProject.libs.serializationJson)
        implementation(rootProject.libs.serializationProtobuf)
    }
}

listOf(projectApi, projectCore).forEach { module ->
    with(module) {
        apply(
            plugin =
                rootProject.libs.plugins.dokka
                    .get()
                    .pluginId,
        )

        tasks {
            this.register<Jar>("sourcesJar") {
                archiveClassifier.set("sources")
                from(sourceSets["main"].allSource)
            }

            this.register<Jar>("dokkaJar") {
                archiveClassifier.set("javadoc")
                dependsOn("dokkaHtml")

                from(layout.buildDirectory.dir("dokka/html")) {
                    include("**")
                }
            }
        }
    }
}

idea {
    module {
        excludeDirs.add(file(".server"))
        excludeDirs.addAll(
            allprojects.map {
                it.layout.buildDirectory
                    .get()
                    .asFile
            },
        )
        excludeDirs.addAll(allprojects.map { it.file(".gradle") })
    }
}
