package com.studentrpg.campus

import Lesson
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import campus.CampusState

@Composable
fun CampusRoute(viewModel: CampusViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CampusScreen(state, viewModel.lessons, viewModel::startLesson, viewModel::answer,
        viewModel::rest, viewModel::coffee, viewModel::restart)
}

@Composable
fun CampusScreen(
    state: CampusState, lessons: List<Lesson>, onStart: (Int) -> Unit,
    onAnswer: (Int) -> Unit, onRest: () -> Unit, onCoffee: () -> Unit, onRestart: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var onlyPending by rememberSaveable { mutableStateOf(false) }
    var confirmRestart by remember { mutableStateOf(false) }
    val visible = remember(query, onlyPending, state.completed, lessons) {
        lessons.withIndex().filter { (_, lesson) ->
            (lesson.title.contains(query, true) || lesson.subject.contains(query, true)) &&
                (!onlyPending || lesson.title !in state.completed)
        }
    }
    Scaffold { padding ->
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
                contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(painterResource(R.drawable.ic_campus), "Campus emblem", Modifier.size(52.dp))
                        Column(Modifier.weight(1f)) {
                            Text("STUDENT RPG", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text("Your semester", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { StudentStatusCard(state, lessons.size) }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Text(state.message, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                item {
                    RecoveryActions(state, onRest, onCoffee)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Class schedule", style = MaterialTheme.typography.titleLarge)
                        OutlinedTextField(query, { query = it }, label = { Text("Search classes") },
                            singleLine = true, modifier = Modifier.fillMaxWidth())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(onlyPending, { onlyPending = it })
                            Text("Only unfinished classes", Modifier.clickable { onlyPending = !onlyPending })
                        }
                    }
                }
                if (visible.isEmpty()) item { Text("No matching classes. Change your search or filter.") }
                items(visible, key = { it.index }) { (index, lesson) ->
                    LessonCard(lesson, lesson.title in state.completed,
                        !state.finished && state.quiz == null && state.minutesLeft >= 30 && state.mana >= lesson.manaCost,
                        { onStart(index) })
                }
                item { OutlinedButton(onClick = { confirmRestart = true }, modifier = Modifier.fillMaxWidth()) { Text("New semester") } }
            }
        }
    }
    state.quiz?.let { progress ->
        val lesson = lessons[progress.lessonIndex]
        QuizDialog(lesson, progress.questionIndex, onAnswer)
    }
    if (confirmRestart) AlertDialog(
        onDismissRequest = { confirmRestart = false }, title = { Text("Start a new semester?") },
        text = { Text("Current progress will be reset.") },
        confirmButton = { TextButton(onClick = { onRestart(); confirmRestart = false }) { Text("Restart") } },
        dismissButton = { TextButton(onClick = { confirmRestart = false }) { Text("Cancel") } }
    )
}

@Composable
fun StudentStatusCard(state: CampusState, totalLessons: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Semester progress", fontWeight = FontWeight.Bold)
                Text("${state.minutesLeft} min left")
            }
            LinearProgressIndicator(progress = { state.completed.size.toFloat() / totalLessons.coerceAtLeast(1) }, modifier = Modifier.fillMaxWidth())
            Text("${state.completed.size} / $totalLessons classes passed · ${state.knowledge.values.sum()} knowledge")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatMeter("Health", state.health, Modifier.weight(1f))
                StatMeter("Mana", state.mana, Modifier.weight(1f))
                StatMeter("Mind", state.mind, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun StatMeter(label: String, value: Int, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text("$value / 100", fontWeight = FontWeight.SemiBold)
        LinearProgressIndicator(progress = { value / 100f }, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun RecoveryActions(state: CampusState, onRest: () -> Unit, onCoffee: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(onRest, Modifier.weight(1f), enabled = !state.finished && state.quiz == null && state.minutesLeft >= 20) {
            Text("Rest · 20 min")
        }
        FilledTonalButton(onCoffee, Modifier.weight(1f), enabled = !state.finished && state.quiz == null && state.minutesLeft >= 5) {
            Text("Coffee · 5 min")
        }
    }
}

@Composable
fun LessonCard(lesson: Lesson, completed: Boolean, enabled: Boolean, onStart: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(lesson.subject.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Text(lesson.title, style = MaterialTheme.typography.titleLarge)
            Text("30 min · ${lesson.manaCost} mana · Difficulty ${lesson.difficulty}")
            Text(if (completed) "Passed · +${lesson.difficulty * 10} knowledge" else "4 questions. Get at least 3 right to pass.")
            Button(onStart, enabled = enabled && !completed, modifier = Modifier.fillMaxWidth()) {
                Text(if (completed) "Completed" else "Attend ${lesson.title}")
            }
        }
    }
}

@Composable
fun QuizDialog(lesson: Lesson, questionIndex: Int, onAnswer: (Int) -> Unit) {
    // Key includes the question: an answer is saved on rotation, but reset for the next question.
    var selected by rememberSaveable(lesson.title, questionIndex) { mutableStateOf(-1) }
    val question = lesson.quiz.questions[questionIndex]
    AlertDialog(
        onDismissRequest = {}, title = { Text("${lesson.title} · ${questionIndex + 1}/4") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(question.text, style = MaterialTheme.typography.titleMedium)
                question.options.forEachIndexed { index, text ->
                    Row(Modifier.fillMaxWidth().clickable { selected = index }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected == index, onClick = { selected = index })
                        Text(text)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onAnswer(selected + 1) }, enabled = selected in 0..3) {
                Text(if (questionIndex == 3) "Finish quiz" else "Next question")
            }
        }
    )
}
