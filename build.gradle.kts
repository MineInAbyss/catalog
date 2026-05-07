plugins {
    `version-catalog`
    alias(libs.plugins.mia.publication)
    alias(libs.plugins.dependencyversions)
    alias(libs.plugins.version.catalog.update)
}

val rootDir = isolated.rootProject.projectDirectory

catalog {
    versionCatalog {
        from(rootDir.files("gradle/libs.versions.toml"))
    }
}

publishing {
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
            isNonStable(candidate.version)
        }
    }

}
fun isNonStable(version: String): Boolean {
    val unstableKeywords = listOf(
        "-beta",
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
