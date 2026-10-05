import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

data class Lesson(val title: String, val subject: String, val difficulty: Int, val manaCost: Int)

open class Character(val name: String, var health: Int, var mana: Int, var mind: Int) {
    fun changeStats(healthDelta: Int = 0, manaDelta: Int = 0, mindDelta: Int = 0) {
        health = (health + healthDelta).coerceIn(0, 100)
        mana = (mana + manaDelta).coerceIn(0, 100)
        mind = (mind + mindDelta).coerceIn(0, 100)
    }
}

class Student(name: String) : Character(name, health = 80, mana = 60, mind = 70) {
    val completedLessons: MutableSet<String> = mutableSetOf()
    val knowledge: MutableMap<String, Int> = mutableMapOf()
}

sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Failure(val reason: String) : ActionResult()
    data class GameOver(val reason: String) : ActionResult()
}

interface CampusAction {
    val title: String
    suspend fun perform(student: Student): ActionResult
}

class AttendLesson(private val lesson: Lesson) : CampusAction {
    override val title = "Attend class: ${lesson.title}"

    override suspend fun perform(student: Student): ActionResult {
        if (lesson.title in student.completedLessons) return ActionResult.Failure("You have already passed this class.")
        if (student.mana < lesson.manaCost) return ActionResult.Failure("Not enough mana: ${lesson.manaCost} required.")

        println("The teacher asks a question about ${lesson.subject}...")
        delay(300) // Suspends this coroutine briefly.
        val focus = student.mind / 10 + student.mana / 10
        student.changeStats(manaDelta = -lesson.manaCost, mindDelta = -lesson.difficulty * 4)
        return if (focus >= lesson.difficulty * 3) {
            student.completedLessons += lesson.title
            student.knowledge[lesson.subject] = (student.knowledge[lesson.subject] ?: 0) + lesson.difficulty * 10
            ActionResult.Success("Class passed! +${lesson.difficulty * 10} knowledge in ${lesson.subject}.")
        } else {
            student.changeStats(healthDelta = -lesson.difficulty * 5)
            ActionResult.Failure("You could not focus. Health decreased; try resting.")
        }
    }
}

class Rest : CampusAction {
    override val title = "Rest in the dormitory (+health, +mental state)"
    override suspend fun perform(student: Student): ActionResult {
        student.changeStats(healthDelta = 20, mindDelta = 25)
        return ActionResult.Success("Rest helped you recover.")
    }
}

class Coffee : CampusAction {
    override val title = "Drink coffee (+mana, -health)"
    override suspend fun perform(student: Student): ActionResult {
        student.changeStats(healthDelta = -8, manaDelta = 25)
        return ActionResult.Success("Coffee restored mana but cost some health.")
    }
}

fun <T> choose(items: List<T>, selector: (T) -> Boolean): List<T> = items.filter(selector)

fun showResult(result: ActionResult) {
    when (result) {
        is ActionResult.Success -> println("SUCCESS: ${result.message}")
        is ActionResult.Failure -> println("FAILED: ${result.reason}")
        is ActionResult.GameOver -> println("GAME OVER: ${result.reason}")
    }
}

fun showStatus(student: Student, lessons: List<Lesson>) {
    val remaining = choose(lessons) { it.title !in student.completedLessons }
    val subjects = lessons.map { it.subject }.toSet()
    val totalKnowledge = (listOf(0) + student.knowledge.values).reduce { total, points -> total + points }
    println("\n${student.name}: health=${student.health}, mana=${student.mana}, mental state=${student.mind}")
    println("Classes passed: ${student.completedLessons.size}/${lessons.size}; knowledge: $totalKnowledge")
    println("Subjects: ${subjects.joinToString()}")
    println("Remaining: ${remaining.map { it.title }.joinToString().ifEmpty { "none" }}")
}

fun main(args: Array<String>) = runBlocking {
    val lessons: List<Lesson> = listOf(
        Lesson("Kotlin Basics", "Programming", 2, 15),
        Lesson("Algorithms", "Programming", 3, 20),
        Lesson("Discrete Mathematics", "Mathematics", 4, 25)
    )
    val student = Student("Student")
    val actions: List<CampusAction> = lessons.map { AttendLesson(it) } + listOf(Rest(), Coffee())
    val demo = "--demo" in args
    val demoChoices = listOf(1, 4, 5, 2, 4, 5, 3).iterator()

    println("=== University: Mini RPG ===")
    println("Goal: pass every class while keeping your health and mental state above zero.")
    while (student.completedLessons.size < lessons.size) {
        showStatus(student, lessons)
        actions.forEachIndexed { index, action -> println("${index + 1}. ${action.title}") }
        println("0. Exit")
        if (!demo) print("Choice: ")
        val input = if (demo) null else readlnOrNull()
        if (!demo && input == null) {
            println("Input ended. The game has stopped.")
            return@runBlocking
        }
        val choice: Int? = if (demo) {
            if (demoChoices.hasNext()) demoChoices.next().also { println("Choice: $it") } else 0
        } else {
            input?.trim()?.toIntOrNull()
        }
        if (choice == 0) {
            println("The player ended the game.")
            return@runBlocking
        }
        if (choice == null || choice !in 1..actions.size) {
            println("Enter an action number from 0 to ${actions.size}.")
            if (demo) return@runBlocking
            continue
        }
        val result = actions[choice - 1].perform(student) // Polymorphism through CampusAction.
        showResult(result)
        if (student.health <= 0 || student.mind <= 0) {
            showResult(ActionResult.GameOver("You have run out of energy."))
            return@runBlocking
        }
    }
    showStatus(student, lessons)
    println("VICTORY! The semester is complete.")
}
