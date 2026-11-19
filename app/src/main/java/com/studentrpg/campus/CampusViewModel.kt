package com.studentrpg.campus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import campus.CampusGame
import campus.CampusState
import com.studentrpg.campus.data.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CampusViewModel(application: Application, savedState: SavedStateHandle) : AndroidViewModel(application) {
    private val semesterState = SemesterSavedState(savedState)
    private val repository = SemesterRepository(CampusDatabase.get(application).semester())
    private var game = CampusGame()
    val lessons = game.lessons
    private val mutex = Mutex()
    private val _state = MutableStateFlow(game.state)
    val state = _state.asStateFlow()
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()
    private val _storageError = MutableStateFlow<String?>(null)
    val storageError = _storageError.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            mutex.withLock {
                if (_ready.value) return@withLock
                _storageError.value = null
                try {
                    game = CampusGame(initialState = repository.load() ?: semesterState.restore() ?: CampusState())
                    repository.save(game.state)
                    _state.value = game.state
                    _ready.value = true
                } catch (e: CancellationException) { throw e
                } catch (e: Exception) { _storageError.value = "Не удалось загрузить сохранение. Повторите попытку." }
            }
        }
    }

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
    fun dismissStorageError() { _storageError.value = null }

    private fun update(action: () -> Unit) {
        viewModelScope.launch {
            mutex.withLock {
                if (!_ready.value) return@withLock
                val previous = game.state
                try {
                    action()
                    repository.save(game.state)
                    semesterState.save(game.state)
                    _state.value = game.state
                    _storageError.value = null
                } catch (e: CancellationException) { throw e
                } catch (e: Exception) {
                    game = CampusGame(initialState = previous)
                    _storageError.value = "Действие не сохранено. Повторите попытку."
                }
            }
        }
    }
}
