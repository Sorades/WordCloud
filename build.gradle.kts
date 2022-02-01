import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    val kotlinVersion = "1.5.30"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.9.0-M1"
}

group = "org.charly.plugin"
version = "1.0.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies{
    implementation("com.kennycason:kumo-tokenizers:1.28")
    implementation("com.kennycason:kumo-core:1.28")
}
