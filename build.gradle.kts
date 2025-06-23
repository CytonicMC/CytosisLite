import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java")
    id("com.gradleup.shadow") version "9.0.0-beta17"
    id("com.github.harbby.gradle.serviceloader") version ("1.1.9")
    id("io.freefair.lombok") version "8.14"
}

group = "net.cytonic"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://repo.foxikle.dev/cytonic")
}

dependencies {
    implementation(libs.minestom)
    implementation(libs.gson)
    implementation(libs.polar)
    implementation(libs.minestompvp) {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }
    implementation(libs.stomui) {
        exclude(group = "net.minestom", module = "minestom-snapshots")
    }
    implementation(libs.configurate)
    implementation(libs.classgraph)
    implementation(libs.bundles.log4j)
}

tasks.withType<Javadoc> {
    val javadocOptions = options as CoreJavadocOptions
    javadocOptions.addStringOption("source", "21")
    javadocOptions.encoding = "UTF-8"
}

tasks.register("fatJar") {
    group = "Build"
    description = "Builds Cytosis ready to ship with all dependencies included in the final jar."
    dependsOn(fatShadow)
    finalizedBy("copyShadowJarToPrimary")
}

val fatShadow = tasks.register<ShadowJar>("fatShadow") {
    mergeServiceFiles()
    archiveFileName.set("cytosis.jar")
    archiveClassifier.set("")
    destinationDirectory.set(layout.buildDirectory.dir("libs"))

    exclude("META-INF/*.SF")
    exclude("META-INF/*.DSA")
    exclude("META-INF/*.RSA")


    configurations = listOf(
        project.configurations.runtimeClasspath.get()
    )
    from(sourceSets.main.get().output)

    manifest {
        attributes["Main-Class"] = "net.cytonic.cytosis.Cytosis"
    }
}

tasks.register<Copy>("copyShadowJarToPrimary") {
    dependsOn(fatShadow)

    if (providers.gradleProperty("server_dir").isPresent) {
        from(fatShadow.get().archiveFile)
        into(providers.gradleProperty("server_dir"))
    }
}

tasks.register<Copy>("copyJarForDocker") {
    into(layout.buildDirectory.dir("libs"))
}