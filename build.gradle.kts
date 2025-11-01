plugins {
    id("java")
}

group = "dev.emortal"
version = "1.0-SNAPSHOT"

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