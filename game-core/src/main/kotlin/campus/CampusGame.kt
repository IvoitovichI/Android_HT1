package campus

import Exhaustion
import Lesson
import LessonCatalog
import Student

data class QuizProgress(val lessonIndex: Int, val questionIndex: Int = 0, val correct: Int = 0)

data class CampusState(
    val health: Int = 80,
    val mana: Int = 60,
    val mind: Int = 70,
    val minutesLeft: Int = 180,
    val completed: Set<String> = emptySet(),
    val knowledge: Map<String, Int> = emptyMap(),
    val quiz: QuizProgress? = null,
    val message: String = "Pass all three classes before the semester ends.",
    val finished: Boolean = false,
    val won: Boolean = false
)

/** Synchronous HW1 rules: no console input, Android APIs, or UI state. */
class CampusGame(val lessons: List<Lesson> = LessonCatalog.lessons) {
    private var student = Student("Student")
    var state = CampusState()
        private set

    fun restart() { student = Student("Student"); state = CampusState() }

    fun startLesson(index: Int) {
        if (state.finished || state.quiz != null) return
        val lesson = lessons.getOrNull(index) ?: return
        if (lesson.title in state.completed) return
        if (state.minutesLeft < 30) { state = state.copy(message = "A class requires 30 minutes."); return }
        if (student.mana < lesson.manaCost) { state = state.copy(message = "Not enough mana. Drink coffee first."); return }
        val focus = student.mind / 10 + student.mana / 10
        student.changeStats(manaDelta = -lesson.manaCost, mindDelta = -lesson.difficulty * 4)
        state = state.copy(minutesLeft = state.minutesLeft - 30)
        if (focus < lesson.difficulty * 3) {
            student.changeStats(healthDelta = -lesson.difficulty * 5)
            finishAction("You could not focus. Rest before trying again.", true)
        } else {
            state = snapshot().copy(quiz = QuizProgress(index), message = "Three correct answers are needed to pass.")
        }
    }

    fun answer(option: Int) {
        val progress = state.quiz ?: return
        if (option !in 1..4) return
        val lesson = lessons[progress.lessonIndex]
        val correct = progress.correct + if (option == lesson.quiz.questions[progress.questionIndex].correctOption) 1 else 0
        if (progress.questionIndex < 3) {
            state = state.copy(quiz = progress.copy(questionIndex = progress.questionIndex + 1, correct = correct))
        } else {
            if (correct >= 3) {
                student.completedLessons += lesson.title
                student.knowledge[lesson.subject] = (student.knowledge[lesson.subject] ?: 0) + lesson.difficulty * 10
            }
            state = state.copy(quiz = null)
            finishAction(if (correct >= 3) "Class passed! $correct/4 correct." else "Quiz failed: $correct/4. Try again.", true)
        }
    }

    fun rest() = recover(20, 20, 0, 25, "Rest restored health and mental state.")
    fun coffee() = recover(5, -8, 25, 0, "Coffee restored mana and cost 8 health.")

    private fun recover(minutes: Int, health: Int, mana: Int, mind: Int, message: String) {
        if (state.finished || state.quiz != null) return
        if (state.minutesLeft < minutes) { state = state.copy(message = "Not enough time for this action."); return }
        student.changeStats(health, mana, mind)
        state = state.copy(minutesLeft = state.minutesLeft - minutes)
        finishAction(message, false)
    }

    private fun snapshot() = state.copy(
        health = student.health, mana = student.mana, mind = student.mind,
        completed = student.completedLessons.toSet(), knowledge = student.knowledge.toMap()
    )

    private fun finishAction(message: String, lesson: Boolean) {
        val loss = Exhaustion.apply(student, lesson)
        val alive = student.health > 0 && student.mind > 0
        val won = alive && student.completedLessons.size == lessons.size
        val finished = won || !alive || state.minutesLeft < 5
        state = snapshot().copy(
            finished = finished, won = won,
            message = when {
                won -> "Victory! The semester is complete."
                !alive -> "Game over: you have run out of energy."
                state.minutesLeft < 5 -> "Game over: time has run out."
                else -> message + if (loss > 0) " Exhaustion: -$loss health and mental state." else ""
            }
        )
    }
}
