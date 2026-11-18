plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.6.1"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.23"
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

val targetMinecraft = "26.3"

group = "com.imjustdoom.villagerinabucket"
if (!project.hasProperty("buildWithoutGitHash")) {
    fun getShortCommitHash(): Provider<String> = providers.exec {
        commandLine("git", "rev-parse", "--short", "HEAD")
    }.standardOutput.asText.map { it.trim().ifEmpty { "unknown" } }
    version = "${rootProject.version}-${getShortCommitHash().get()}"
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "jitpack"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "worldguard"
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
    compileOnly("com.github.amnoah.betterreload:api:v1.0.0")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.19")
    compileOnly("com.github.GriefPrevention:GriefPrevention:16.18.7")
    implementation("org.bstats:bstats-bukkit:3.1.0")
    paperweight.paperDevBundle("$targetMinecraft.build.227-stable")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
    withJavadocJar()
}

val resourcePackZip by tasks.registering(Zip::class) {
    archiveFileName.set(rootProject.name + "Pack-" + targetMinecraft + "-zh-cn.zip")
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    from("resourcepack")
    from("LICENSE")
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
        (options as StandardJavadocDocletOptions).apply {
            links("https://jd.papermc.io/paper/$targetMinecraft/")
            title = "${project.name} ${project.version} API"
            addStringOption("Xdoclint:none", "-quiet") // Suppress warnings for missing Javadoc
        }
    }

    shadowJar {
        dependencies {
            include(dependency("org.bstats:bstats-bukkit:3.1.0"))
            include(dependency("org.bstats:bstats-base:3.1.0"))
        }
        relocate("org.bstats", project.group.toString() + ".bstats")
        archiveFileName.set("${rootProject.name}-paper-${project.version}.jar")
    }

    processResources {
        filteringCharset = "UTF-8"
        from("LICENSE") { into("META-INF") }

        val pluginName = rootProject.name
        val pluginVersion = project.version
        val pluginGroup = project.group

        filesMatching("**/plugin.yml") {
            expand(
                "name" to pluginName,
                "version" to pluginVersion,
                "group" to pluginGroup
            )
        }
    }

    runServer {
        minecraftVersion(targetMinecraft)
        downloadPlugins {
            modrinth("luckperms", "v5.5.71-bukkit")
        }
        dependsOn(shadowJar)
    }

    build {
        dependsOn(shadowJar)
        dependsOn(javadoc)
        dependsOn(resourcePackZip)
    }
}

publishing {
    repositories {
        maven {
            name = "imjustdoom"
            url = uri("https://repo.imjustdoom.com/releases")
            credentials {
                username = System.getenv("MAVEN_NAME")
                password = System.getenv("MAVEN_SECRET")
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()
            from(components["java"])
        }
    }
}