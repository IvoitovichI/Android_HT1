plugins {
    kotlin("jvm") version "2.2.20"
    application
    id("com.android.application") version "9.4.1" apply false
    kotlin("android") version "2.2.20" apply false
    kotlin("plugin.compose") version "2.2.20" apply false
}

repositories { mavenCentral() }

dependencies {
    implementation(project(":game-core"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

kotlin { jvmToolchain(21) }
kotlin.sourceSets.main {
    kotlin.exclude("Character.kt", "Student.kt", "Lesson.kt", "Question.kt", "Quiz.kt", "LessonCatalog.kt", "Exhaustion.kt")
}
application {
    mainClass = "MainKt"
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8", "-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
