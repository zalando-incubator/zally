// Version set to empty to make artifact name in line with the name defined in Dockerfile
version = ""

buildscript {
    extra.apply {
        // sets the jackson version that spring uses
        set("jackson.version", "2.10.1")
    }
}

plugins {
    val kotlinVersion = "1.3.21"

    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.noarg") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion

    id("org.springframework.boot") version "2.0.4.RELEASE"
    id("io.spring.dependency-management") version "1.0.7.RELEASE"
}

dependencies {
    val springBootVersion = "2.0.4.RELEASE"
    val jadlerVersion = "1.3.0"

    compile(project(":zally-core"))
    compile(project(":zally-ruleset-zalando"))
    compile(project(":zally-ruleset-zally"))
    compile("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    compile("org.springframework.boot:spring-boot-starter-undertow:$springBootVersion")
    compile("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    compile("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion") {
        exclude("org.hibernate", "hibernate-entitymanager")
    }
    compile("org.flywaydb:flyway-core:5.1.4")
    compile("org.hsqldb:hsqldb:2.4.1")
    compile("org.postgresql:postgresql:42.2.4")
    compile("org.hibernate:hibernate-core:5.3.5.Final")
    compile("org.jadira.usertype:usertype.core:7.0.0.CR1") {
        exclude("org.hibernate", "hibernate-entitymanager")
    }
    compile("org.zalando.stups:stups-spring-oauth2-server:1.0.22")
    compile("org.zalando:problem-spring-web:0.23.0")
    compile("org.zalando:twintip-spring-web:1.1.0")
    compile("io.micrometer:micrometer-registry-prometheus:1.0.6")

    testCompile(project(":zally-test"))
    testCompile("net.jadler:jadler-core:$jadlerVersion")
    testCompile("net.jadler:jadler-jdk:$jadlerVersion")
    testCompile("net.jadler:jadler-junit:$jadlerVersion")
    testCompile("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testCompile("com.jayway.jsonpath:json-path-assert:2.4.0")
    testCompile("org.mockito:mockito-core:2.23.4")
}

dependencyManagement {
    dependencies {
        dependency("org.assertj:assertj-core:3.11.0")
    }
}

tasks.bootRun {
    jvmArgs = listOf("-Dspring.profiles.active=dev")
}

tasks.withType<PublishToMavenRepository>().configureEach {
    onlyIf {
        false
    }
}
