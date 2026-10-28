pluginManagement {
    repositories { gradlePluginPortal(); google(); mavenCentral() }
}
rootProject.name = "go-iv-lab"
include(":nickname-core", ":iv-engine", ":app")
