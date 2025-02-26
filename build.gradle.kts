plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "2.1.0" // Adds runServer and runMojangMappedServer tasks for testing
    id("maven-publish")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "de.oliver"
version = findProperty("version")!!
description = findProperty("description").toString()

allprojects {

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.alessiodp.com/releases/")
    }

    dependencies {
        compileOnly("net.kyori:adventure-text-minimessage:4.13.1")

        //implementation("net.byteflux:libby-bukkit:1.2.0")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(path = ":fancy-plugin:core-plugin", configuration = "shadow"))
}

tasks {
    runServer{
        minecraftVersion("1.19.4")
    }

    shadowJar{
        archiveClassifier.set("")
        relocate("dev.jorel.commandapi", "de.oliver.fancycoins.commandapi")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()
                from(project.components["java"])
            }
        }
    }

    build {
        dependsOn(shadowJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}