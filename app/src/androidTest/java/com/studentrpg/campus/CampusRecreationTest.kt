package com.studentrpg.campus

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import java.io.File

class CampusRecreationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun searchAnswerAndProgressSurviveRecreation() {
        compose.onNodeWithText("Search classes").performScrollTo().performTextInput("Kotlin")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Kotlin").assertExists()
        compose.onNodeWithText("Only unfinished classes").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNode(isToggleable()).assertIsOn()
        compose.onNodeWithText("Attend Kotlin Basics").performScrollTo().performClick()
        compose.onNodeWithText("val").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Next question").assertIsEnabled().performClick()
        compose.onNodeWithText("Boolean").performClick()
        compose.onNodeWithText("Next question").performClick()
        compose.onNodeWithText("println").performClick()
        compose.onNodeWithText("Next question").performClick()
        compose.onNodeWithText("An ordered collection").performClick()
        compose.onNodeWithText("Finish quiz").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("1 / 3 classes passed · 20 knowledge").performScrollTo().assertExists()
        compose.onNodeWithText("150 min left").assertExists()
    }

    @Test fun captureInterfaceAndQuiz() {
        compose.waitForIdle()
        screenshot("01-semester.png")
        compose.onNodeWithText("Attend Kotlin Basics").performScrollTo().performClick()
        compose.onNodeWithText("val").performClick()
        compose.waitForIdle()
        screenshot("02-quiz.png")
    }

    private fun screenshot(name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots")
        check(directory.mkdirs() || directory.isDirectory)
        val bitmap = checkNotNull(instrumentation.uiAutomation.takeScreenshot())
        File(directory, name).outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) }
        bitmap.recycle()
    }
}
