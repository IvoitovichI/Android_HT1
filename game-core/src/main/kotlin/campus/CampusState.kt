package campus

import java.io.Serializable

const val DAY_MINUTES = 360

data class CampusState(
    val health: Int = 80, val mana: Int = 60, val mind: Int = 80,
    val semesterDays: Int = 24, val day: Int = 1, val minutesLeft: Int = DAY_MINUTES,
    val coins: Int = 100, val xp: Int = 0,
    val passes: Map<String, Int> = emptyMap(), val knowledge: Map<String, Int> = emptyMap(),
    val inventory: Map<String, Int> = mapOf("coffee" to 1), val quiz: QuizProgress? = null,
    val message: String = "Добро пожаловать! Библиотека готовит к зачётам, а смекалка спасает на сложных вопросах.",
    val journal: List<String> = emptyList(), val finished: Boolean = false, val won: Boolean = false
) : Serializable { val level get() = 1 + xp / 100 }
