package com.studentrpg.campus.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.studentrpg.campus.PlanForm
import com.studentrpg.campus.PlansUiState
import com.studentrpg.campus.data.*
import java.text.DateFormat
import java.util.Date

private fun statusLabel(status: String) = when (status) {
    StudyPlan.DRAFT -> "Черновик"
    StudyPlan.ACTIVE -> "В работе"
    else -> "Завершён"
}

@Composable
private fun PlanFeedback(message: String?, dismiss: () -> Unit) {
    if (message != null) Surface(color = MaterialTheme.colorScheme.secondaryContainer) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(message)
            TextButton(dismiss) { Text("Понятно") }
        }
    }
}

@Composable
internal fun PlansListScreen(state: PlansUiState, onNew: () -> Unit, onOpen: (Long) -> Unit, onDismiss: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize().testTag("plansList"), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Учебные планы", style = MaterialTheme.typography.headlineMedium)
            Text("Разбейте подготовку на шаги и следите за прогрессом.")
            Button(onNew, Modifier.testTag("newPlan")) { Text("Создать план") }
            PlanFeedback(state.message, onDismiss)
        }
        when {
            state.loading -> item { CircularProgressIndicator() }
            state.plans.isEmpty() -> item { Text("Планов пока нет. Создайте первый учебный план.") }
            else -> {
                item {
                    Text("Всего: ${state.plans.size} · Завершено: ${state.plans.count { it.status == StudyPlan.DONE }}")
                    Text("Выполнено шагов: ${state.plans.sumOf { it.completedSteps }}/${state.plans.sumOf { it.targetSteps }}")
                }
                items(state.plans, key = { it.id }) { plan ->
                    Card(onClick = { onOpen(plan.id) }, modifier = Modifier.fillMaxWidth().testTag("plan_${plan.id}")) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(plan.title, style = MaterialTheme.typography.titleLarge)
                            Text("${statusLabel(plan.status)} · ${plan.completedSteps}/${plan.targetSteps} шагов")
                            LinearProgressIndicator(progress = { plan.completedSteps.toFloat() / plan.targetSteps }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun PlanDetailScreen(
    id: Long, state: PlansUiState, onBack: () -> Unit, onEdit: () -> Unit,
    onAction: (PlanAction) -> Unit, onDelete: () -> Unit, onDismiss: () -> Unit
) {
    val plan = state.plans.find { it.id == id }
    var confirmDelete by rememberSaveable(id) { mutableStateOf(false) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            TextButton(onBack) { Text("К списку планов") }
            PlanFeedback(state.message, onDismiss)
        }
        if (state.loading) item { CircularProgressIndicator() }
        else if (plan == null) item { Text("План удалён или не найден. Вернитесь к списку.") }
        else {
            item {
                Text(plan.title, style = MaterialTheme.typography.headlineMedium)
                Text(plan.description.ifBlank { "Описание не добавлено" })
                Text("Статус: ${statusLabel(plan.status)}")
                Text("Шаги: ${plan.completedSteps}/${plan.targetSteps}", Modifier.testTag("planProgress"))
                LinearProgressIndicator(progress = { plan.completedSteps.toFloat() / plan.targetSteps }, modifier = Modifier.fillMaxWidth())
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    when (plan.status) {
                        StudyPlan.DRAFT -> Button({ onAction(PlanAction.START) }, enabled = !state.busy) { Text("Начать план") }
                        StudyPlan.ACTIVE -> {
                            Button({ onAction(PlanAction.STEP) }, enabled = !state.busy && plan.completedSteps < plan.targetSteps,
                                modifier = Modifier.testTag("addStep")) { Text("Выполнить шаг") }
                            OutlinedButton({ onAction(PlanAction.UNDO_STEP) }, enabled = !state.busy && plan.completedSteps > 0) { Text("Отменить шаг") }
                            Button({ onAction(PlanAction.COMPLETE) }, enabled = !state.busy && plan.completedSteps == plan.targetSteps) { Text("Завершить план") }
                            if (plan.completedSteps < plan.targetSteps) Text("Для завершения выполните все шаги.")
                        }
                        StudyPlan.DONE -> Button({ onAction(PlanAction.REOPEN) }, enabled = !state.busy) { Text("Вернуть в работу") }
                    }
                    OutlinedButton(onEdit, enabled = !state.busy && plan.status != StudyPlan.DONE) { Text("Редактировать") }
                    OutlinedButton({ confirmDelete = true }, enabled = !state.busy) { Text("Удалить план") }
                    Text("История действий", style = MaterialTheme.typography.titleLarge)
                }
            }
            val events = state.history.filter { it.planId == id }
            if (events.isEmpty()) item { Text("История пока пуста.") }
            items(events, key = { "event_${it.id}" }) { event ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(event.text)
                        Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(event.timestamp)),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
    if (confirmDelete && plan != null) AlertDialog(
        onDismissRequest = { confirmDelete = false },
        title = { Text("Удалить план?") },
        text = { Text("«${plan.title}» и вся его история будут удалены без возможности восстановления.") },
        confirmButton = { TextButton({ confirmDelete = false; onDelete() }, enabled = !state.busy) { Text("Удалить") } },
        dismissButton = { TextButton({ confirmDelete = false }) { Text("Отмена") } }
    )
}

@Composable
internal fun PlanEditorScreen(
    form: PlanForm, onTitle: (String) -> Unit, onDescription: (String) -> Unit,
    onTarget: (String) -> Unit, onSave: () -> Unit, onBack: () -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().imePadding(), contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            TextButton(onBack, enabled = !form.busy) { Text("Назад") }
            Text("Учебный план", style = MaterialTheme.typography.headlineMedium)
            Text("План — личная цель подготовки. Его шаги учитываются отдельно от игровых минут и зачётов.")
        }
        if (form.loading) item { CircularProgressIndicator() }
        else {
            item { ValidatedField(form.title, onTitle, "Название", form.titleError.takeIf { form.submitted }, "planTitle", enabled = !form.busy) }
            item { ValidatedField(form.description, onDescription, "Описание", form.descriptionError.takeIf { form.submitted }, "planDescription", singleLine = false, enabled = !form.busy) }
            item { ValidatedField(form.target, onTarget, "Количество шагов (1–100)", form.targetError.takeIf { form.submitted }, "planTarget", number = true, enabled = !form.busy) }
            item {
                form.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(onSave, Modifier.fillMaxWidth().testTag("savePlan"), enabled = !form.busy && !form.saved) {
                    Text(if (form.busy) "Сохранение…" else "Сохранить план")
                }
            }
        }
    }
}

@Composable
private fun ValidatedField(
    value: String, onChange: (String) -> Unit, label: String, error: String?, tag: String,
    singleLine: Boolean = true, number: Boolean = false, enabled: Boolean = true
) {
    OutlinedTextField(value, onChange, Modifier.fillMaxWidth().testTag(tag), label = { Text(label) },
        singleLine = singleLine, enabled = enabled, isError = error != null,
        supportingText = { if (error != null) Text(error) },
        keyboardOptions = KeyboardOptions(keyboardType = if (number) KeyboardType.Number else KeyboardType.Text))
}
