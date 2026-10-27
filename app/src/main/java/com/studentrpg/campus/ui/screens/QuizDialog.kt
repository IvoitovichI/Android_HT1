package com.studentrpg.campus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import campus.*
import com.studentrpg.campus.R
import com.studentrpg.campus.ui.components.*

@Composable
internal fun QuizDialog(state: CampusState, course: Course, progress: QuizProgress,
    onAnswer: (String) -> Unit, onTrick: (String) -> Unit) {
    val question = course.stages[progress.stage][progress.questionIndex]
    val key = "${state.day}_${course.id}_${progress.stage}_${progress.questionIndex}_${state.minutesLeft}"
    var selected by rememberSaveable(key) { mutableStateOf("") }
    val keptOptions = remember(question, progress.eliminated) {
        if (progress.eliminated) setOf(question.answer, question.options.first { it != question.answer }) else question.options.toSet()
    }
    Dialog(onDismissRequest = {}) {
        Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 6.dp,
            modifier = Modifier.heightIn(max = 680.dp)) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${course.title} · этап ${progress.stage + 1}", style = MaterialTheme.typography.titleLarge)
                Text("Вопрос ${progress.questionIndex + 1}/4 · верно ${progress.correct} · смекалка ${state.mana}")
                LinearProgressIndicator(progress = { progress.questionIndex / 4f }, modifier = Modifier.fillMaxWidth())
                Text(question.text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.testTag("quizQuestion"))
                if (progress.highlighted) Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(12.dp)) {
                    Text("${if (progress.guaranteed) "Деканат одобрил" else "Озарение / подсказка"}: ${question.answer}", Modifier.padding(12.dp))
                }
                if (question.isOpen) OutlinedTextField(selected, { selected = it }, label = { Text("Твой ответ") },
                    modifier = Modifier.fillMaxWidth().testTag("openAnswer"), singleLine = true)
                else question.options.forEach { option ->
                    val enabled = option in keptOptions
                    Surface(color = if (progress.highlighted && option == question.answer) MaterialTheme.colorScheme.tertiaryContainer
                        else if (selected == option) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)) {
                        Row(Modifier.fillMaxWidth().clickable(enabled = enabled) { selected = option }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected == option, { selected = option }, enabled = enabled)
                            Text(option, color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.weight(1f))
                        }
                    }
                }
                if (progress.questionIndex > 0 || progress.highlighted || progress.eliminated) Text(state.message, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider()
                Text("Студенческая магия", fontWeight = FontWeight.SemiBold)
                OutlinedButton({ onTrick("peek") }, Modifier.fillMaxWidth(), enabled = state.mana >= 20 && !progress.highlighted) { Text("Подсмотреть · 20") }
                if (!question.isOpen) OutlinedButton({ onTrick("fifty") }, Modifier.fillMaxWidth(),
                    enabled = state.mana >= 10 && !progress.eliminated && !progress.highlighted) { Text("Призрак: 50/50 · 10") }
                OutlinedButton({ onTrick("dean") }, Modifier.fillMaxWidth(), enabled = state.mana >= 30 && !progress.guaranteed) { Text("Призыв деканата · 30") }
                Button({ onAnswer(if (progress.guaranteed) question.answer else selected) },
                    Modifier.fillMaxWidth().testTag("submitAnswer"), enabled = (selected.isNotBlank() && (question.isOpen || selected in keptOptions)) || progress.guaranteed) {
                    Text(if (progress.questionIndex == 3) "Завершить зачёт" else "Ответить")
                }
            }
        }
    }
}
