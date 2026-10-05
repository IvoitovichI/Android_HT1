# StudentRPG

A small console RPG in Kotlin/JVM. Play as a student, pass three classes, and manage health, and mental state.

## Run

Install JDK 21 and open this repository folder in Android Studio as a Gradle project. Wait for Gradle Sync to finish. If prompted for a Gradle JDK, select JDK 21. Open `src/main/kotlin/Main.kt` and run `main()` using the green gutter icon. Enter action numbers in the **Run** console. This is a JVM console program; Jetpack Compose, Activity, Fragment, other Android components, and an emulator are not required.

From a Windows terminal in the repository folder, you can also run `./gradlew.bat build`. For an automatic playthrough, run `./gradlew.bat run --args="--demo"`. The first run downloads Gradle and dependencies.

## Homework requirements

All examples are in `src/main/kotlin/Main.kt`:

- Collections and higher-order functions: `lessons`, `actions`, `choose`, and `showStatus` use lists, a set, a map, lambdas, `filter`, `map`, and `reduce`.
- Object-oriented programming: `Student` inherits from `Character`; `CampusAction` is implemented by the lesson, rest, and coffee actions.
- Kotlin types: `Lesson` and the `ActionResult` variants are data classes; `ActionResult` is a sealed class.
- Coroutines: `main` uses `runBlocking`, and `AttendLesson.perform` is a `suspend` function that calls `delay`.
