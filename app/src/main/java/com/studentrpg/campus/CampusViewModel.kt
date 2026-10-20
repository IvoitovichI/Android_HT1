package com.studentrpg.campus

import androidx.lifecycle.ViewModel
import campus.CampusGame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CampusViewModel : ViewModel() {
    private val game = CampusGame()
    val lessons = game.lessons
    private val _state = MutableStateFlow(game.state)
    val state = _state.asStateFlow()

    fun startLesson(index: Int) = update { game.startLesson(index) }
    fun answer(option: Int) = update { game.answer(option) }
    fun rest() = update { game.rest() }
    fun coffee() = update { game.coffee() }
    fun restart() = update { game.restart() }

    private inline fun update(action: () -> Unit) { action(); _state.value = game.state }
}
