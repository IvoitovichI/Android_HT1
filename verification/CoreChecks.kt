import campus.CampusGame

fun main() {
    fun pass(game: CampusGame, index: Int) {
        game.startLesson(index)
        check(game.state.quiz != null)
        game.lessons[index].quiz.questions.forEach { game.answer(it.correctOption) }
    }
    val game = CampusGame()
    pass(game, 0)
    check(game.state.completed == setOf("Kotlin Basics"))
    check(game.state.knowledge["Programming"] == 20)
    check(game.state.minutesLeft == 150)
    val before = game.state
    game.startLesson(0)
    check(game.state == before) { "A completed class must not consume time." }
    game.rest(); game.coffee(); pass(game, 1)
    game.rest(); game.coffee(); pass(game, 2)
    check(game.state.won && game.state.finished)
    check(game.state.minutesLeft == 40)
    check(game.state.knowledge.values.sum() == 90)
    val victory = game.state
    game.coffee(); game.rest(); game.startLesson(0)
    check(game.state == victory)
    game.restart()
    check(game.state.minutesLeft == 180 && game.state.completed.isEmpty())

    game.startLesson(0)
    val inQuiz = game.state
    game.rest(); game.coffee(); game.startLesson(1); game.answer(0)
    check(game.state == inQuiz) { "Actions and invalid answers must not interrupt a quiz." }
    game.lessons[0].quiz.questions.forEach { game.answer(it.correctOption % 4 + 1) }
    check(game.state.completed.isEmpty() && game.state.quiz == null)
    check(game.state.message.contains("Quiz failed"))

    val exhausted = Student("Test")
    exhausted.mana = 0
    check(Exhaustion.apply(exhausted, true) == 10)
    check(exhausted.health == 70 && exhausted.mind == 60)
    check(Exhaustion.apply(exhausted, false) == 5)

    val outOfTime = CampusGame()
    repeat(9) { outOfTime.rest() }
    check(outOfTime.state.finished && !outOfTime.state.won && outOfTime.state.minutesLeft == 0)
    val clamped = CampusGame()
    repeat(4) { clamped.rest() }
    check(clamped.state.health == 100 && clamped.state.mind == 100)
    println("Core checks passed: victory, scoring, repeat protection, quiz failure, action guards, exhaustion, time, reset, stat bounds.")
}
