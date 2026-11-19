package com.studentrpg.campus

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class PlansUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun waitForTab() {
        compose.waitUntil(15000) { compose.onAllNodesWithTag("tab_plans").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("tab_plans").performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithTag("newPlan").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun createValidateEditWorkflowAndDeleteThroughUi() {
        waitForTab()
        val title = "Exam ${System.currentTimeMillis()}"
        compose.onNodeWithTag("newPlan").performClick()
        compose.onNodeWithTag("savePlan").performScrollTo().performClick()
        compose.onNodeWithText("Название: от 3 до 80 символов").assertExists()
        compose.onNodeWithTag("planTitle").performScrollTo().performTextInput(title)
        compose.onNodeWithTag("planTarget").performScrollTo().performTextReplacement("2")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("planTitle").assertTextContains(title)
        compose.onNodeWithTag("savePlan").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithTag("plansList").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(title).performScrollTo().performClick()
        compose.onNodeWithText("Редактировать").performScrollTo().performClick()
        compose.onNodeWithTag("planTitle").performTextReplacement("$title edited")
        compose.onNodeWithTag("savePlan").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithTag("planProgress").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("$title edited").assertExists()
        compose.onNodeWithText("Начать план").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithTag("addStep").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Завершить план").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("addStep").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Шаги: 1/2").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithTag("addStep").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Шаги: 2/2").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Завершить план").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Вернуть в работу").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Статус: Завершён").assertExists()
        compose.onNodeWithText("Редактировать").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Вернуть в работу").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithTag("addStep").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Отменить шаг").performScrollTo().performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("Шаги: 1/2").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Удалить план").performScrollTo().performClick()
        compose.onNodeWithText("Отмена").performClick()
        compose.onNodeWithText("$title edited").assertExists()
        compose.onNodeWithText("Удалить план").performScrollTo().performClick()
        compose.onNodeWithText("Удалить", substring = false).performClick()
        compose.waitUntil(15000) { compose.onAllNodesWithText("План удалён или не найден. Вернитесь к списку.").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("К списку планов").performScrollTo().performClick()
        compose.onNodeWithText("$title edited").assertDoesNotExist()
    }
}
