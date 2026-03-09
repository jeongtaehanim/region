import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

dependencies {
    implementation(projectApi)

    compileOnly(rootProject.libs.command)
}

extra.apply {
    val pluginName = rootProject.name.split("-").joinToString("") { it.replaceFirstChar { char -> char.uppercase() } }

    set("pluginName", pluginName)
    set("packageName", rootProject.name.replace("-", ""))
    set("kotlinVersion", libs.versions.kotlin.get())
    set(
        "paperVersion",
        libs.versions.paper.get().split(".").take(2).joinToString(separator = ".").replace("-R0", "")
    ) //.replace("-R0", "") << 1.x.x가 아닌 1.x 버전인 경우, R0이 포함될 수 있음.
    set("pluginLibraries", "")

    val pluginLibraries = LinkedHashSet<String>()

    configurations.findByName("implementation")?.allDependencies?.forEach { dependency ->
        val group = dependency.group ?: error("group is null")
        var name = dependency.name
        var version = dependency.version

        if (dependency !is ProjectDependency) {
            if (group == "org.jetbrains.kotlin" && version == null) {
                version = getKotlinPluginVersion()
            } else if (group == "io.github.jeongtaehanim") {
                name = name.replace("-api", "-core")
            }

            requireNotNull(version) { "version is null" }
            require(version != "latest.release") { "version is latest.release" }

            pluginLibraries += "$group:$name:$version"
            set("pluginLibraries", pluginLibraries.joinToString("\n  ") { "- '$it'" })
        }
    }
}

tasks {
    processResources {
        filesMatching("*.yml") {
            expand(project.properties)
            expand(extra.properties)
        }
    }

    fun registerJar(
        classifier: String,
        bundleProject: Project? = null,
        bundleTask: TaskProvider<org.gradle.jvm.tasks.Jar>? = null
    ) = register<Jar>("${classifier}Jar") {
        archiveBaseName.set(rootProject.name)
        archiveVersion.set("")
        archiveClassifier.set(classifier)

        from(sourceSets["main"].output)

        if (bundleProject != null) from(bundleProject.sourceSets["main"].output)

        if (bundleTask != null) {
            bundleTask.let { bundleJar ->
                dependsOn(bundleJar)
                from(zipTree(bundleJar.get().archiveFile))
            }
            exclude("clip-plugin.yml")
            rename("bundle-plugin.yml", "plugin.yml")
        } else {
            exclude("bundle-plugin.yml")
            rename("clip-plugin.yml", "plugin.yml")
        }
    }.also { jar ->
        register<Copy>("test${classifier.replaceFirstChar { it.titlecase() }}Jar") {
            val plugins = rootProject.file(".server/plugins-$classifier")

            from(jar)
            into(plugins)
        }
    }

    registerJar("dev", projectApi, coreDevJar)
    registerJar("reobf", projectApi, coreReobfJar)
    registerJar("clip")
}
