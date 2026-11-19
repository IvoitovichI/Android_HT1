package com.studentrpg.campus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.studentrpg.campus.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PlansUiState(
    val plans: List<StudyPlan> = emptyList(),
    val history: List<PlanEvent> = emptyList(),
    val loading: Boolean = true,
    val busy: Boolean = false,
    val message: String? = null
)

class PlansViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlanRepository(CampusDatabase.get(application))
    private val busy = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    val state = combine(repository.plans, repository.history, busy, message) { plans, history, working, feedback ->
        PlansUiState(plans, history, loading = false, busy = working, message = feedback)
    }.catch { emit(PlansUiState(loading = false, message = "Не удалось прочитать планы. Откройте вкладку снова.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlansUiState())

    fun act(id: Long, action: PlanAction) = execute("Прогресс сохранён") { repository.act(id, action) }
    fun delete(id: Long) = execute("План удалён") { repository.delete(id) }
    fun saved() { message.value = "План сохранён" }
    fun dismissMessage() { message.value = null }
    private fun execute(success: String, block: suspend () -> Unit) {
        if (busy.value) return
        busy.value = true
        viewModelScope.launch {
            try { block(); message.value = success
            } catch (e: CancellationException) { throw e
            } catch (e: IllegalArgumentException) { message.value = e.message
            } catch (e: Exception) { message.value = "Не удалось сохранить изменения. Попробуйте ещё раз."
            } finally { busy.value = false }
        }
    }
}

data class PlanForm(
    val title: String = "", val description: String = "", val target: String = "3",
    val loading: Boolean = true, val busy: Boolean = false, val saved: Boolean = false,
    val error: String? = null, val submitted: Boolean = false, val minimum: Int = 0
) {
    val titleError get() = if (title.trim().length !in 3..80) "Название: от 3 до 80 символов" else null
    val descriptionError get() = if (description.length > 1000) "Максимум 1000 символов" else null
    val targetError get() = when {
        target.toIntOrNull() !in 1..100 -> "Введите целое число от 1 до 100"
        (target.toIntOrNull() ?: 0) < minimum -> "Уже выполнено $minimum шагов"
        else -> null
    }
}

class PlanEditorViewModel(application: Application, private val handle: SavedStateHandle) : AndroidViewModel(application) {
    private val repository = PlanRepository(CampusDatabase.get(application))
    private val id: Long? = handle.get<Long>("id")
    private val _state = MutableStateFlow(PlanForm())
    val state = _state.asStateFlow()
    init {
        viewModelScope.launch {
            try {
                val plan = id?.let { target -> repository.plans.first().find { it.id == target } }
                require(id == null || plan != null) { "План уже удалён." }
                require(plan?.status != StudyPlan.DONE) { "Сначала верните план в работу." }
                _state.value = PlanForm(
                    title = handle["title"] ?: plan?.title.orEmpty(),
                    description = handle["description"] ?: plan?.description.orEmpty(),
                    target = handle["target"] ?: (plan?.targetSteps ?: 3).toString(),
                    loading = false, minimum = plan?.completedSteps ?: 0
                )
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) { _state.value = PlanForm(loading = false, error = e.message ?: "Ошибка загрузки") }
        }
    }
    fun title(value: String) { handle["title"] = value; _state.update { it.copy(title = value, saved = false) } }
    fun description(value: String) { handle["description"] = value; _state.update { it.copy(description = value, saved = false) } }
    fun target(value: String) { handle["target"] = value; _state.update { it.copy(target = value, saved = false) } }
    fun save() {
        val form = _state.value
        if (form.loading || form.busy || form.saved) return
        _state.value = form.copy(submitted = true)
        if (form.titleError != null || form.descriptionError != null || form.targetError != null) return
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                repository.save(id, form.title, form.description, form.target.toInt())
                _state.update { it.copy(busy = false, saved = true) }
            } catch (e: CancellationException) { throw e
            } catch (e: IllegalArgumentException) { _state.update { it.copy(busy = false, error = e.message) }
            } catch (e: Exception) { _state.update { it.copy(busy = false, error = "Не удалось сохранить план. Повторите попытку.") } }
        }
    }
}
