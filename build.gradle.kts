plugins {
    id("java")
    `maven-publish`
}

group = "dev.emortal"
version = "1.0-SNAPSHOT"

java.toolchain.languageVersion = JavaLanguageVersion.of(25)

repositories {
    mavenCentral()
    maven("https://maven.draylar.dev/releases") // arcane (molang)
}

dependencies {
    compileOnly("net.minestom:minestom:2025.10.31-1.21.10")
    implementation("com.alibaba.fastjson2:fastjson2:2.0.60")
    implementation("dev.omega:arcane:0.1.4") // molang
    implementation("org.joml:joml:1.10.8")
}

publishing {
    repositories {
        maven {
            name = "development"
            url = uri("https://repo.emortal.dev/snapshots")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_SECRET")
            }
        }
        maven {
            name = "release"
            url = uri("https://repo.emortal.dev/releases")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_SECRET")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = "dev.emortal.minestom"
            artifactId = "game-sdk"

            val commitHash = System.getenv("COMMIT_HASH_SHORT")
            val releaseVersion = System.getenv("RELEASE_VERSION")
            version = commitHash ?: releaseVersion ?: "local"

            from(components["java"])
        }
    }
}