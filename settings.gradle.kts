pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "zypos"
include(":app")
include(":core:ui")
include(":core:data")
include(":core:domain")
include(":core:common")
include(":core:designsystem")
include(":features:menu")
include(":features:order")
include(":features:dashboard")
include(":features:transaction")
include(":features:favorite")
