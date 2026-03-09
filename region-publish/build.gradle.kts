plugins {
    alias(libs.plugins.publish)
    signing
}

publishing {
    repositories {
        mavenLocal()

        maven {
            name = "server"
            url = rootProject.uri(".server/libraries")
        }
    }

    publications {
        fun MavenPublication.setup(target: Project) {
            artifactId = target.name
            from(target.components["java"])
            artifact(target.tasks["sourcesJar"])
            artifact(target.tasks["dokkaJar"])
        }

        create<MavenPublication>("api") {
            setup(projectApi)
        }

        create<MavenPublication>("core") {
            setup(projectCore)
            artifact(coreReobfJar)
        }
    }
}

mavenPublishing {
    pom {
        name.set("Minecraft Stat")
        description.set("Team, Palimp.")
        url.set("https://github.com/jeongtaehanim/${rootProject.name}")

        licenses {
            license {
                name.set("GNU General Public License version 3")
                url.set("https://opensource.org/licenses/GPL-3.0")
            }
        }

        developers {
            developer {
                id.set("jeongtaehanim")
                name.set("jeongtaehan")
                email.set("jeongtaehanim@gmail.com")
                url.set("https://github.com/jeongtaehanim")
                roles.addAll("developer")
                timezone.set("Asia/Seoul")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/jeongtaehanim/${rootProject.name}.git")
            developerConnection.set("scm:git:ssh://github.com:jeongtaehanim/${rootProject.name}.git")
            url.set("https://github.com/jeongtaehanim/${rootProject.name}")
        }
    }

    publishToMavenCentral()
}

signing {
    isRequired = true
    useInMemoryPgpKeys(
        providers.gradleProperty("signingKey").orNull,
        providers.gradleProperty("signingPassword").orNull
    )
    sign(publishing.publications)
}

projectPlugin.tasks.named("clipJar") {
    dependsOn(tasks.named("publishApiPublicationToServerRepository"))
    dependsOn(tasks.named("publishCorePublicationToServerRepository"))
}