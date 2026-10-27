package com.studentrpg.campus.ui.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.studentrpg.campus.R

internal data class Destination(val route: String, val label: String, val icon: Int)
internal val destinations = listOf(
    Destination("campus", "Кампус", R.drawable.ic_campus),
    Destination("courses", "Предметы", R.drawable.ic_book),
    Destination("library", "Библиотека", R.drawable.ic_library),
    Destination("market", "Рынок", R.drawable.ic_shop)
)

/** Use the same stack rules for dashboard shortcuts and bottom navigation. */
internal fun NavHostController.openTab(route: String) {
    if (currentDestination?.route == route) return
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
