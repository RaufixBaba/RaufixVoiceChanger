plugins {
    id("fabric-loom") version "1.10-SNAPSHOT"
    id("maven-publish")
}

val modVersion = property("mod_version").toString()
val minecraftVersion = property("minecraft_version").toString()

version = "$modVersion+$minecraftVersion"
group = property("maven_group").toString()

base {
    archivesName.set(property("archives_base_name").toString())
}

repositories {
    mavenCentral()
    maven("https://maven.maxhenkel.de/repository/public") {
        name = "MaxHenkel"
    }
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") {
                name = "Modrinth"
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    implementation("de.maxhenkel.voicechat:voicechat-api:${property("voicechat_api_version")}")
    modCompileOnly("maven.modrinth:simple-voice-chat:${property("voicechat_mod_version")}")
    modRuntimeOnly("maven.modrinth:simple-voice-chat:${property("voicechat_mod_version")}")
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        val props = mapOf(
            "version" to modVersion,
            "minecraft_version" to minecraftVersion,
            "loader_version" to project.property("loader_version").toString()
        )
        inputs.properties(props)
        filesMatching("fabric.mod.json") {
            expand(props)
        }
    }

    jar {
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName.get()}" }
        }
    }
}
