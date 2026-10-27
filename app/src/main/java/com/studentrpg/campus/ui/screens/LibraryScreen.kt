package com.studentrpg.campus.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import campus.*
import com.studentrpg.campus.R
import com.studentrpg.campus.ui.components.*

@Composable
internal fun LibraryScreen(state: CampusState, courses: List<Course>, onStudy: (Int) -> Unit) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val course = courses[selected]
    val stage = (state.passes[course.id] ?: 0).coerceAtMost(course.requiredPasses - 1)
    val knowledge = state.knowledge[course.id] ?: 0
    Page(state, "Тихо, идёт прокачка", "Конспекты, открытые ответы и озарения.") {
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                courses.forEachIndexed { index, c -> FilterChip(selected == index, { selected = index }, label = { Text(c.title) }) }
            }
        }
        if (course.id == "warhammer") item { Banner(R.drawable.lore_banner, "Шлем космодесантника, книги и студенческие реликвии") }
        item {
            Panel(course.title, R.drawable.ic_library) {
                Text("Знания: $knowledge/60 · шанс озарения: ${(knowledge * 0.7).toInt().coerceAtMost(40)}% на каждый вопрос")
                Text("Чтение: 60 мин · +12 знаний · +12 XP · −6 концентрации. Порог этапов: 0 / 12 / 24" + if (course.requiredPasses == 4) " / 36." else ".")
                Button({ onStudy(selected) }, Modifier.fillMaxWidth(), enabled = !state.finished && state.minutesLeft >= 60 && state.mind >= 10 && knowledge < 60) {
                    Text("Изучить конспект")
                }
                Text("Введённые ответы проверяются без учёта регистра и лишних пробелов. В числовых задачах вводи значение, в остальных — слово или название.")
            }
        }
        if (knowledge == 0) item { Text("Сначала изучи конспект: откроются материалы текущего этапа.") }
        else {
            item { Text("Конспект этапа ${stage + 1}", style = MaterialTheme.typography.titleLarge) }
            items(course.stages[stage]) { question ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(question.text, fontWeight = FontWeight.SemiBold)
                        Text("Ответ: ${question.answer}", color = MaterialTheme.colorScheme.primary)
                        Text(question.explanation)
                        if (question.aliases.isNotEmpty()) Text("Также принимается: ${question.aliases.joinToString()}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Panel("Смекалка вместо маны", R.drawable.ic_spark) {
                Text("Подсмотреть у соседа — 20: открывает ответ, в том числе текстовый.\nПризрак старшекурсника — 10: убирает два неверных варианта.\nПризыв деканата — 30: следующий ответ засчитывается автоматически.\nЗапас смекалки восстанавливается сном, кофе и едой. На вход в зачёт смекалка не нужна.")
            }
        }
    }
}
