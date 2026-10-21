plugins {
    java
    id("org.springframework.boot") version "4.0.0"
}

group = "pl.barkly"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

val javaFormatter by configurations.creating

dependencies {
    javaFormatter("com.google.googlejavaformat:google-java-format:1.28.0")
}

val javaFormatterExports = listOf("api", "code", "file", "parser", "tree", "util").map {
    "--add-exports=jdk.compiler/com.sun.tools.javac.$it=ALL-UNNAMED"
}

val javaSourcePaths = fileTree("src") {
    include("**/*.java")
}.files.sortedBy { it.path }.map { it.absolutePath }

val formatJava = tasks.register<JavaExec>("formatJava") {
    group = "formatting"
    description = "Formats Java sources and tests with four-space indentation."
    classpath = javaFormatter
    jvmArgs(javaFormatterExports)
    mainClass.set("com.google.googlejavaformat.java.Main")
    args(listOf("--aosp", "--replace") + javaSourcePaths)
}

tasks.register<JavaExec>("checkJavaFormat") {
    mustRunAfter(formatJava)
    group = "verification"
    description = "Checks Java formatting without changing files."
    classpath = javaFormatter
    jvmArgs(javaFormatterExports)
    mainClass.set("com.google.googlejavaformat.java.Main")
    args(listOf("--aosp", "--dry-run", "--set-exit-if-changed") + javaSourcePaths)
}

tasks.withType<JavaCompile>().configureEach {
    mustRunAfter(formatJava)
}
