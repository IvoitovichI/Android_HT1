plugins { kotlin("jvm") }
repositories { mavenCentral() }
kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
    sourceSets.main {
        kotlin.srcDir("../src/main/kotlin")
        kotlin.include("Character.kt", "Student.kt", "Lesson.kt", "Question.kt", "Quiz.kt", "LessonCatalog.kt", "Exhaustion.kt", "campus/**")
    }
}
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val verification by sourceSets.creating {
    kotlin.srcDir("../verification")
    compileClasspath += sourceSets.main.get().output + configurations.runtimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}
val verifyCampus by tasks.registering(JavaExec::class) {
    dependsOn("verificationClasses")
    mainClass.set("CoreChecksKt")
    classpath = sourceSets["verification"].runtimeClasspath
}
