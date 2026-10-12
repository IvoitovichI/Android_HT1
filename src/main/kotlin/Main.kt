import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {
    val config = try {
        GameConfig.fromArgs(args)
    } catch (error: IllegalArgumentException) {
        println("Configuration error: ${error.message}")
        println("Usage: --demo --time-limit=180")
        return@runBlocking
    }
    val lessons = LessonCatalog.lessons
    val actions: List<CampusAction> =
        lessons.map { AttendLesson(it, QuizRunner(config.demo)) } + listOf(Rest(), Coffee())
    Game(Student("Student"), lessons, actions, config).play()
}
