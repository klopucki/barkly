plugins {
    java
    id("org.springframework.boot") version "4.1.0"
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



