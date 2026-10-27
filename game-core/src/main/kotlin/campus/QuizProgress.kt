package campus

import java.io.Serializable

data class QuizProgress(val lessonIndex: Int, val stage: Int, val questionIndex: Int = 0, val correct: Int = 0,
    val highlighted: Boolean = false, val eliminated: Boolean = false, val guaranteed: Boolean = false) : Serializable
