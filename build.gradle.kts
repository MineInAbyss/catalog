plugins {
    `version-catalog`
    `maven-publish`
    alias(libs.plugins.gradle.versions)
    alias(libs.plugins.version.catalog.update)
}

val rootDir = isolated.rootProject.projectDirectory

repositories {
    mavenCentral()
    google()
    maven("https://repo.mineinabyss.com/releases") {
        content {
            includeGroupAndSubgroups("com.mineinabyss")
        }
    }
    maven("https://repo.mineinabyss.com/snapshots") {
        content {
            includeGroupAndSubgroups("com.mineinabyss")
        }
    }
    maven("https://repo.mineinabyss.com/mirror")
    maven("https://repo.papermc.io/repository/maven-public/") {
        content {
            includeGroupAndSubgroups("io.papermc")
        }
    }
    gradlePluginPortal()
}

catalog {
    versionCatalog {
        from(rootDir.files("gradle/libs.versions.toml"))
    }
}


publishing {
    repositories {
        maven {
            name = "mineinabyssMaven"
            val repo = "https://repo.mineinabyss.com/"
            val isSnapshot = System.getenv("IS_SNAPSHOT") == "true"
            val url = if (isSnapshot) repo + "snapshots" else repo + "releases"
            setUrl(url)
            credentials(PasswordCredentials::class)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            from(components["versionCatalog"])
            artifactId = "catalog"
        }
    }
}

tasks {
    updateDaemonJvm {
        languageVersion = JavaLanguageVersion.of(25)
        vendor = JvmVendorSpec.JETBRAINS
    }
    dependencyUpdates {
        rejectVersionIf {
            isNonStable(candidate.version) && !isNonStable(currentVersion)
        }
    }

}
fun isNonStable(version: String): Boolean {
    val unstableKeywords = listOf(
        "-beta",
        "-dev",
        "-rc",
        "-alpha",
    )

    return unstableKeywords.any { version.contains(it, ignoreCase = true) }
}

versionCatalogUpdate {
    keep {
        keepUnusedVersions = true
    }
}
