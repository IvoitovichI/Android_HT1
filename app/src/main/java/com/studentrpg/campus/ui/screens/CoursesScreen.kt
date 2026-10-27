package com.studentrpg.campus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import campus.*
import com.studentrpg.campus.R
import com.studentrpg.campus.ui.components.*

private fun courseIcon(course: Course) = when (course.icon) {
    "code" -> R.drawable.ic_code; "nodes" -> R.drawable.ic_nodes; "math" -> R.drawable.ic_math
    "data" -> R.drawable.ic_data; "network" -> R.drawable.ic_network; "phone" -> R.drawable.ic_phone
    else -> R.drawable.ic_relic
}

@Composable
internal fun CoursesScreen(state: CampusState, courses: List<Course>, onStart: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var pending by rememberSaveable { mutableStateOf(false) }
    val visible = remember(query, pending, state.passes, courses) {
        courses.withIndex().filter { (_, c) -> (c.title.contains(query, true) || c.subtitle.contains(query, true)) &&
            (!pending || (state.passes[c.id] ?: 0) < c.requiredPasses) }
    }
    Page(state, "Зачётная книжка", "Сложность растёт с каждым этапом.") {
        item {
            Column {
                OutlinedTextField(query, { query = it }, label = { Text("Поиск предмета") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("courseSearch"))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(pending, { pending = it }, Modifier.testTag("pendingFilter"))
                    Text("Только незакрытые", Modifier.clickable { pending = !pending })
                }
            }
        }
        if (visible.isEmpty()) item { Text("Таких предметов нет. Измени поиск или фильтр.") }
        items(visible, key = { it.value.id }) { (index, course) ->
            val stage = state.passes[course.id] ?: 0
            val done = stage >= course.requiredPasses
            val knowledge = state.knowledge[course.id] ?: 0
            val required = course.requiredKnowledge(stage)
            Panel(course.title, courseIcon(course)) {
                Text(course.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Сдано $stage/${course.requiredPasses} · знания $knowledge/60")
                LinearProgressIndicator(progress = { stage.toFloat() / course.requiredPasses }, modifier = Modifier.fillMaxWidth())
                if (!done) {
                    Text("Этап ${stage + 1} · 120 мин · −3 здоровья · −12 концентрации")
                    Text(if (stage == 0) "Тест с вариантами ответа" else "Тест + открытый ввод: подготовься в библиотеке")
                    if (knowledge < required) Text("Нужно $required знаний: посети библиотеку.", color = MaterialTheme.colorScheme.error)
                }
                Button({ onStart(index) }, Modifier.fillMaxWidth().testTag("start_${course.id}"),
                    enabled = !done && !state.finished && state.minutesLeft >= 120 && state.mind >= 16 && state.health > 5 && knowledge >= required) {
                    Text(if (done) "Предмет закрыт" else "Сдать этап ${stage + 1}")
                }
                if (!done && (state.minutesLeft < 120 || state.mind < 16 || state.health <= 5))
                    Text("Для занятия нужно 120 минут, ≥16 концентрации и >5 здоровья. Восстановись в кампусе.")
            }
        }
    }
}
