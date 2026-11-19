package com.studentrpg.campus

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Before
import androidx.lifecycle.ViewModelProvider
import org.junit.Rule
import org.junit.Test

class CampusRecreationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Before fun resetPersistedSemester() {
        compose.waitUntil(15000) { compose.onAllNodesWithTag("tab_campus").fetchSemanticsNodes().isNotEmpty() }
        compose.runOnUiThread { ViewModelProvider(compose.activity)[CampusViewModel::class.java].restart(24) }
        compose.waitUntil(15000) {
            ViewModelProvider(compose.activity)[CampusViewModel::class.java].state.value == campus.CampusState()
        }
    }

    @Test fun selectedTabFollowsDestinationAfterNavigationRecreationAndBack() {
        val routes = listOf("campus", "courses", "library", "market", "plans")
        fun assertOnlySelected(route: String) {
            routes.forEach { candidate ->
                if (candidate == route) compose.onNodeWithTag("tab_$candidate").assertIsSelected()
                else compose.onNodeWithTag("tab_$candidate").assertIsNotSelected()
            }
        }
        assertOnlySelected("campus")
        compose.onNodeWithText("На занятия").performScrollTo().performClick()
        assertOnlySelected("courses")
        compose.onNodeWithTag("tab_library").performClick()
        compose.onNodeWithText("Тихо, идёт прокачка").assertIsDisplayed()
        assertOnlySelected("library")
        compose.activityRule.scenario.recreate()
        assertOnlySelected("library")
        compose.runOnUiThread { compose.activity.onBackPressedDispatcher.onBackPressed() }
        assertOnlySelected("campus")
        listOf("market", "courses", "library", "campus").forEach { route ->
            compose.onNodeWithTag("tab_$route").performClick()
            assertOnlySelected(route)
        }
    }

    @Test fun navigationSearchAnswerAndProgressSurviveRecreation() {
        compose.onNodeWithText("Предметы").performClick()
        compose.onNodeWithTag("courseSearch").performTextInput("Kotlin")
        compose.onNodeWithTag("pendingFilter").performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("courseSearch").assertTextContains("Kotlin")
        compose.onNodeWithTag("pendingFilter").assertIsOn()
        compose.onNodeWithTag("start_kotlin").performScrollTo().performClick()
        compose.onNodeWithText("val", useUnmergedTree = true).performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("submitAnswer").performScrollTo().assertIsEnabled().performClick()
        listOf("Boolean", "println", "Список").forEach { answer ->
            compose.onNodeWithText(answer, useUnmergedTree = true).performScrollTo().performClick()
            compose.onNodeWithTag("submitAnswer").performScrollTo().performClick()
        }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Сдано 1/4 · знания 4/60").performScrollTo().assertExists()
        compose.onNodeWithText("День 1/24 · 240 мин").assertExists()
        compose.onNodeWithText("Библиотека").performClick()
        compose.onNodeWithText("Изучить конспект").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("Знания: 16/60 · шанс озарения: 11% на каждый вопрос").performScrollTo().assertExists()
        compose.onNodeWithText("Предметы").performClick()
        compose.onNodeWithTag("start_kotlin").performScrollTo().performClick()
        compose.onNodeWithTag("openAnswer").performTextInput("3")
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("openAnswer").assertTextContains("3")
        compose.onNodeWithTag("submitAnswer").performScrollTo().performClick()
        compose.onNodeWithText("Вопрос 2/4 · верно 1 · смекалка 60").assertExists()
    }

    @Test fun libraryMarketAndDayAreFunctional() {
        compose.onNodeWithText("Рынок").performClick()
        compose.onNodeWithText("Выйти на смену").performScrollTo().performClick()
        compose.onNodeWithText("145", substring = false).assertExists()
        compose.onNodeWithText("Купить · 24 монет").performScrollTo().performClick()
        compose.onNodeWithText("121", substring = false).assertExists()
        compose.onNodeWithText("Кампус").performClick()
        compose.onNodeWithText("Закончить день").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("День 2/24 · 360 мин").assertExists()
        compose.onNodeWithText("Библиотека").performClick()
        compose.onNodeWithText("Изучить конспект").performScrollTo().performClick()
        compose.onNodeWithText("Конспект этапа 1").performScrollTo().assertExists()
    }
}
