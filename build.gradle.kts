@file:Suppress("PropertyName")

import kotlin.reflect.KProperty

plugins {
    `java-library`
    `maven-publish`
    idea
    id("net.neoforged.moddev") version("2.0.140")
    kotlin("jvm") version("2.2.20")
}

private operator fun <T> PropertyDelegate.setValue(instance: Any?, property: KProperty<*>, value: T) {
    if (instance !is Project) return
    if (!instance.hasProperty(property.name)) return
    instance.setProperty(property.name, value)
}

val mod_id: String by project
val mod_name: String by project
var mod_version: String by project
val mod_license: String by project
val mod_group_id: String by project
val mod_authors: String by project
val mod_description: String by project
val parchment_mappings_version: String by project
val parchment_minecraft_version: String by project
val minecraft_version: String by project
val minecraft_version_range: String by project
val neo_version: String by project
val loader_version_range: String by project

val buildNumber: String? = System.getenv("GITHUB_RUN_NUMBER")
if (buildNumber != null && System.getenv("BUILD_TYPE") == "snapshot")
    mod_version = "$mod_version+build.$buildNumber"

apply("$rootDir/dependencies.gradle")

version = mod_version
group = mod_group_id

base {
    archivesName.set(mod_name)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = neo_version

    parchment {
        mappingsVersion.set(parchment_mappings_version)
        minecraftVersion.set(parchment_minecraft_version)
    }

    // accessTransformers.add("src/main/resources/META-INF/accesstransformer.cfg")

    runs {
        configureEach {
            // "SCAN": For mods scan.
            // "REGISTRIES": For firing of registry events.
            // "REGISTRYDUMP": For getting the contents of all registries.
            systemProperty("forge.logging.markers", "REGISTRIES")

            logLevel.set(org.slf4j.event.Level.DEBUG)
        }

        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

//        register("gameTestServer") {
//            type.set("gameTestServer")
//            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
//        }

        register("data") {
            data()

            programArguments.addAll(
                "--mod", mod_id,
                "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }
    }

    mods.register(mod_id) {
        sourceSet(sourceSets.main.get())
    }
}

sourceSets.main {
    resources {
        srcDir("src/generated/resources")
    }
}

tasks.named<Wrapper>("wrapper").configure {
    distributionType = Wrapper.DistributionType.BIN
}

val generateModMetadata by tasks.registering(ProcessResources::class) {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "neo_version" to neo_version,
        "loader_version_range" to loader_version_range,
        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_version" to mod_version,
        "mod_authors" to mod_authors,
        "mod_description" to mod_description
    )

    inputs.properties(replaceProperties)

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(replaceProperties)
    }

    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}

sourceSets.main.configure {
    resources.srcDir(generateModMetadata)
}

neoForge.ideSyncTask(generateModMetadata)

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}