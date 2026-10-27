package com.studentrpg.campus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import campus.CampusGame
import campus.CampusState
import com.studentrpg.campus.data.SemesterSavedState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CampusViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    private val semesterState = SemesterSavedState(savedState)
    private val restored = semesterState.restore()
    private val game = CampusGame(initialState = restored ?: CampusState())
    val lessons = game.lessons
    private val _state = MutableStateFlow(game.state)
    val state = _state.asStateFlow()
    fun startLesson(index: Int) = update { game.startLesson(index) }
    fun answer(value: String) = update { game.answerText(value) }
    fun rest() = update { game.rest() }
    fun nextDay() = update { game.nextDay() }
    fun study(index: Int) = update { game.study(index) }
    fun work() = update { game.work() }
    fun buy(id: String) = update { game.buy(id) }
    fun use(id: String) = update { game.use(id) }
    fun trick(kind: String) = update { game.trick(kind) }
    fun restart(days: Int) = update { game.restart(days) }
    private fun update(action: () -> Unit) {
        viewModelScope.launch {
            action()
            _state.value = game.state
            semesterState.save(game.state)
        }
    }
}
