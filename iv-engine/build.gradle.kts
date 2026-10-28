plugins { kotlin("jvm") }

repositories { mavenCentral() }

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.7.3")
    testImplementation(kotlin("test-junit"))
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
    systemProperty("goivlab.repoRoot", rootProject.projectDir.absolutePath)
}
