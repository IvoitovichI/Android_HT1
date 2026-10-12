class Game(
    private val student: Student,
    private val lessons: List<Lesson>,
    private val actions: List<CampusAction>,
    private val config: GameConfig
) {
    private var minutesLeft = config.timeLimitMinutes

    suspend fun play() {
        val demoChoices = listOf(1, 4, 5, 2, 4, 5, 3).iterator()
        println("=== University: Mini RPG ===")
        println("Goal: pass every class before time runs out; keep health and mental state above zero.")
        while (student.completedLessons.size < lessons.size) {
            if (actions.none { it.durationMinutes <= minutesLeft }) {
                showResult(ActionResult.GameOver("Time has run out."))
                return
            }
            showStatus()
            actions.forEachIndexed { index, action -> println("${index + 1}. ${action.title} (${action.durationMinutes} min)") }
            println("0. Exit")
            if (!config.demo) print("Choice: ")
            val input = if (config.demo) {
                (if (demoChoices.hasNext()) demoChoices.next() else 0).toString().also { println("Choice: $it") }
            } else readlnOrNull() ?: run {
                println("Input ended. The game has stopped.")
                return
            }
            val choice = input.trim().toIntOrNull()
            if (choice == 0) {
                println("The player ended the game.")
                return
            }
            if (choice == null || choice !in 1..actions.size) {
                println("Enter an action number from 0 to ${actions.size}.")
                if (config.demo) return
                continue
            }

            val action = actions[choice - 1]
            if (action.durationMinutes > minutesLeft) {
                showResult(ActionResult.Failure("Not enough time for this action: $minutesLeft min left."))
                if (config.demo) return
                continue
            }
            val result = action.perform(student)
            minutesLeft -= action.durationMinutes
            showResult(result)
            val exhaustionLoss = Exhaustion.apply(student, action is AttendLesson)
            if (exhaustionLoss > 0) {
                println("No mana: exhaustion reduced health and mental state by $exhaustionLoss each.")
            }
            if (student.health <= 0 || student.mind <= 0) {
                showResult(ActionResult.GameOver("You have run out of energy."))
                return
            }
            if (student.completedLessons.size == lessons.size) break
            if (minutesLeft == 0) {
                showResult(ActionResult.GameOver("Time has run out."))
                return
            }
        }
        showStatus()
        println("VICTORY! The semester is complete.")
    }

    private fun showStatus() {
        val remaining = lessons.filter { it.title !in student.completedLessons }
        val totalKnowledge = student.knowledge.values.sum()
        println("\n${student.name}: health=${student.health}, mana=${student.mana}, mental state=${student.mind}")
        println("Time left: $minutesLeft/${config.timeLimitMinutes} min")
        println("Classes passed: ${student.completedLessons.size}/${lessons.size}; knowledge: $totalKnowledge")
        println("Subjects: ${lessons.map { it.subject }.toSet().joinToString()}")
        println("Remaining: ${remaining.map { it.title }.joinToString().ifEmpty { "none" }}")
    }

    private fun showResult(result: ActionResult) {
        when (result) {
            is ActionResult.Success -> println("SUCCESS: ${result.message}")
            is ActionResult.Failure -> println("FAILED: ${result.reason}")
            is ActionResult.GameOver -> println("GAME OVER: ${result.reason}")
        }
    }
}
