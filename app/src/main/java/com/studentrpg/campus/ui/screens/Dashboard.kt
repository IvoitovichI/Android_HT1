package com.studentrpg.campus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import campus.*
import com.studentrpg.campus.R
import com.studentrpg.campus.ui.components.*

@Composable
internal fun Dashboard(state: CampusState, courses: List<Course>, onRest: () -> Unit, onNextDay: () -> Unit,
    onUse: (String) -> Unit, onRestart: () -> Unit, onCourses: () -> Unit) {
    val total = courses.sumOf { it.requiredPasses }
    val passed = state.passes.values.sum()
    Page(state, "Твой маленький эпос", "Большие знания. Маленькая стипендия.") {
        item { Banner(R.drawable.campus_banner, "Осенний кампус с библиотекой и кофейней") }
        item {
            Panel(if (state.won) "Семестр закрыт!" else "Студент · уровень ${state.level}", R.drawable.ic_relic) {
                Text("${state.xp} XP · следующий уровень через ${100 - state.xp % 100} XP")
                LinearProgressIndicator(progress = { (state.xp % 100) / 100f }, modifier = Modifier.fillMaxWidth())
                Text("$passed / $total этапов · ${courses.count { (state.passes[it.id] ?: 0) >= it.requiredPasses }} / ${courses.size} предметов закрыто")
                LinearProgressIndicator(progress = { passed.toFloat() / total }, modifier = Modifier.fillMaxWidth())
                Text("На каждом этапе — 4 вопроса, для зачёта нужны 3 верных. Начиная со второго этапа появляются открытые ответы.")
                Button(onCourses, Modifier.fillMaxWidth(), enabled = !state.finished) { Text("На занятия") }
            }
        }
        item {
            Panel("Общежитие", R.drawable.ic_moon) {
                Text("Несколько занятий в день: распределяй 360 минут между учёбой, чтением, работой и отдыхом.")
                Text("Отдых: 60 мин · +10 здоровья, +30 концентрации")
                OutlinedButton(onRest, Modifier.fillMaxWidth(), enabled = !state.finished && state.minutesLeft >= 60) { Text("Отдохнуть") }
                Text("Сон: завершает день · +25 здоровья, +40 концентрации, +18 смекалки. Неиспользованное время сгорает.")
                Button(onNextDay, Modifier.fillMaxWidth(), enabled = !state.finished) {
                    Text(if (state.day == state.semesterDays) "Подвести итоги семестра" else "Закончить день")
                }
                if (!state.finished && state.day == state.semesterDays) Text("Последний день: после итогов продолжить занятия будет нельзя.")
            }
        }
        item { Inventory(state, onUse) }
        item {
            Panel("Журнал приключений", R.drawable.ic_book) {
                if (state.journal.isEmpty()) Text("Здесь появятся зачёты, покупки и великие подвиги курьера.")
                state.journal.take(8).forEach { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
        }
        item { OutlinedButton(onRestart, Modifier.fillMaxWidth()) { Text("Настроить новый семестр") } }
    }
}
