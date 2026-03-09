import org.gradle.api.Project
import org.gradle.jvm.tasks.Jar

private fun Project.getSubprojectByName(name: String) = project(":${rootProject.name}-$name")

val Project.projectApi
    get() = getSubprojectByName("api")

val Project.projectCore
    get() = getSubprojectByName("core")

val Project.projectPlugin
    get() = getSubprojectByName("plugin")

val Project.projectDongle
    get() = getSubprojectByName("dongle")

private fun Project.coreTask(name: String) = projectCore.tasks.named(name, Jar::class.java)

val Project.coreDevJar
    get() = coreTask("coreDevJar")

val Project.coreReobfJar
    get() = coreTask("coreReobfJar")

val Project.coreSourcesJar
    get() = coreTask("sourcesJar")