import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.0"
    antlr
    application
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.13.1")
    testImplementation(kotlin("test"))
    implementation(files("libs/rsyntaxtextarea-3.3.6-SNAPSHOT.jar"))
}

tasks.generateGrammarSource {
    arguments = arguments + listOf("-visitor")
}


tasks.withType<KotlinCompile>().configureEach {
    dependsOn(tasks.withType<AntlrTask>())
}

tasks.withType<Jar>().configureEach {
    dependsOn(tasks.withType<AntlrTask>())
}

tasks.test {
    useJUnitPlatform()
}


kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("gui.GuiMainKt")
}

tasks.register<JavaExec>("runRepl") {
    mainClass = "ReplMainKt"
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}