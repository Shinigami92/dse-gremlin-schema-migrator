plugins {
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.allopen") version "1.6.21"
    id("io.quarkus")
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-picocli")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("com.datastax.oss.quarkus:cassandra-quarkus-client:1.1.2")
    implementation("org.apache.tinkerpop:tinkergraph-gremlin:3.6.0")
    implementation("org.apache.tinkerpop:gremlin-driver:3.6.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.11.0")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.testcontainers:testcontainers:1.17.2")
}

group = "com.github.shinigami92"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

allOpen {
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
    annotation("io.quarkus.test.junit.QuarkusMainIntegrationTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}
