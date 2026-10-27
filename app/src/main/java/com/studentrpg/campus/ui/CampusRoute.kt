package com.studentrpg.campus.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.studentrpg.campus.R
import com.studentrpg.campus.CampusViewModel
import com.studentrpg.campus.ui.components.*
import com.studentrpg.campus.ui.navigation.*
import com.studentrpg.campus.ui.screens.*

@Composable
fun CampusRoute(viewModel: CampusViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val nav = rememberNavController()
    // Observe the actual destination, including system Back and state restoration.
    var currentRoute by remember(nav) { mutableStateOf(nav.currentDestination?.route) }
    DisposableEffect(nav) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route
        }
        nav.addOnDestinationChangedListener(listener)
        onDispose { nav.removeOnDestinationChangedListener(listener) }
    }
    var confirmRestart by remember { mutableStateOf(false) }
    var days by rememberSaveable { mutableFloatStateOf(24f) }
    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(R.drawable.ic_campus), null, Modifier.size(30.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("CAMPUS QUEST", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                            Text("День ${state.day}/${state.semesterDays} · ${state.minutesLeft} мин", style = MaterialTheme.typography.labelMedium)
                        }
                        Icon(painterResource(R.drawable.ic_coin), "Монеты", Modifier.size(20.dp), tint = Color(0xFF99641B))
                        Text(" ${state.coins}", fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MiniStat(R.drawable.ic_heart, "Здоровье", state.health, Color(0xFFB84B50))
                        MiniStat(R.drawable.ic_spark, "Смекалка", state.mana, Color(0xFF6950A1))
                        MiniStat(R.drawable.ic_focus, "Фокус", state.mind, MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(selected = currentRoute == destination.route,
                        modifier = Modifier.testTag("tab_${destination.route}"),
                        onClick = { nav.openTab(destination.route) }, icon = { Icon(painterResource(destination.icon), null, Modifier.size(24.dp)) },
                        label = { Text(destination.label, maxLines = 1) })
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = "campus", modifier = Modifier.padding(padding)) {
            composable("campus") {
                Dashboard(state, viewModel.lessons, viewModel::rest, viewModel::nextDay, viewModel::use,
                    { confirmRestart = true }, { nav.openTab("courses") })
            }
            composable("courses") { CoursesScreen(state, viewModel.lessons, viewModel::startLesson) }
            composable("library") { LibraryScreen(state, viewModel.lessons, viewModel::study) }
            composable("market") { MarketScreen(state, viewModel::buy, viewModel::work, viewModel::use) }
        }
    }
    state.quiz?.let { progress ->
        val course = viewModel.lessons[progress.lessonIndex]
        QuizDialog(state, course, progress, viewModel::answer, viewModel::trick)
    }
    if (confirmRestart) AlertDialog(
        onDismissRequest = { confirmRestart = false }, title = { Text("Новый семестр") },
        text = {
            Column {
                Text("Прогресс текущего семестра будет сброшен. Длительность: ${days.toInt()} дней.")
                Slider(days, { days = it }, valueRange = 12f..60f, steps = 47)
                Text("24 дня — обычный режим. 12 дней — интенсив. 60 — спокойная учёба. В дне 360 минут.")
            }
        }, confirmButton = {
            TextButton(onClick = { viewModel.restart(days.toInt()); confirmRestart = false }) { Text("Начать") }
        }, dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Отмена") } }
    )
}
