# StudentRPG / Campus Quest

Campus Quest is a student-life RPG for Android, built with Kotlin, Jetpack Compose, and Material 3. Complete a semester by balancing exams, library study, rest, and part-time work. Each day gives you a limited amount of time, so every activity competes for the same resources.

## Features

- Four screens: Campus, Courses, Library, and Market.
- Configurable semesters lasting 12-60 days, with 360 in-game minutes per day.
- Seven courses, 24 exam stages, and 96 multiple-choice and typed-answer questions.
- Library notes and course knowledge that unlock later exam stages and can reveal answers.
- Three special exam tricks powered by the student's smarts resource.
- Health, concentration, XP, levels, and an activity journal.
- Part-time jobs, coins, food, drinks, and a consumable inventory.
- A Warhammer 40,000 elective with lore questions and a dedicated illustration.
- Game progress, navigation, search filters, and answer input retained across Activity recreation, including screen rotation.

Android saved-state restoration is supported. A permanent save slot for arbitrary cold launches is not implemented.

## Screenshots

[View project screenshots](docs/screenshots/). These images show an earlier version of the interface, before the RPG expansion.

## Requirements

- JDK 21.
- Android SDK Platform 36 and Android SDK Platform-Tools.
- An Android 8.0 (API 26) or newer device or emulator.
- Android Studio for the IDE-based workflow.

The Gradle wrapper is included. The first build requires internet access to download the Gradle distribution, dependencies, and any required toolchain.

## Run in Android Studio

1. Open the repository root in Android Studio.
2. Install Android SDK Platform 36 through SDK Manager if needed.
3. Set the Gradle JDK to JDK 21 and let Gradle sync finish.
4. Start an emulator or connect a device with USB debugging enabled. Accept its debugging authorization prompt if shown.
5. Select the `app` run configuration and click **Run**.

## Build and run from PowerShell

Run these commands from the repository root. Replace the example JDK and SDK paths with your local installation paths.

```powershell
$env:JAVA_HOME='C:\path\to\jdk-21'
```

Create or update the local `local.properties` file with your Android SDK path, using forward slashes:

```properties
sdk.dir=C:/path/to/Android/Sdk
```

Build the debug APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

With an emulator running or an authorized device connected, install and launch the app:

```powershell
.\gradlew.bat :app:installDebug
& 'C:\path\to\Android\Sdk\platform-tools\adb.exe' shell am start -n com.studentrpg.campus/.MainActivity
```

## Verification

Run the game-rule checks, including a complete simulated semester, and Android static analysis:

```powershell
.\gradlew.bat :game-core:verifyCampus
.\gradlew.bat :app:lintDebug
```

Run Android regression tests with an authorized device or emulator connected:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

These tests cover navigation, Activity recreation, answer input, gameplay actions, and saved-state serialization.

## Console version

The original console game is also available:

```powershell
.\gradlew.bat run --console=plain
```
