plugins {
    kotlin("jvm")
}

repositories { mavenCentral() }

dependencies { testImplementation(kotlin("test-junit")); testImplementation("junit:junit:4.13.2") }

tasks.test { useJUnit() }
