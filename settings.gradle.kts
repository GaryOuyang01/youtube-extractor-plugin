pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // NewPipeExtractor is only published on JitPack; limit the repository to it.
        maven("https://jitpack.io") {
            content { includeGroup("com.github.TeamNewPipe") }
        }
    }
}

rootProject.name = "youtube-extractor-plugin"

include(":app")
