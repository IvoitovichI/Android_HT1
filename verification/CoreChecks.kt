import campus.*
import kotlin.random.Random
import java.io.*

private fun pass(game: CampusGame, index: Int) {
    game.startLesson(index)
    check(game.state.quiz != null) { game.state.message }
    repeat(4) { game.answerText(checkNotNull(game.question).answer) }
}
private fun restore(state: CampusState): CampusState {
    val buffer = ByteArrayOutputStream()
    ObjectOutputStream(buffer).use { it.writeObject(state) }
    return ObjectInputStream(ByteArrayInputStream(buffer.toByteArray())).use { it.readObject() as CampusState }
}
fun main() {
    val catalog = CourseCatalog.courses
    check(catalog.size == 7 && catalog.sumOf { it.requiredPasses } == 24)
    check(catalog.sumOf { c -> c.stages.sumOf { it.size } } == 96)
    catalog.forEach { c -> c.stages.forEachIndexed { stage, questions ->
        check(questions.size == 4)
        if (stage > 0) check(questions.any { it.isOpen })
        questions.forEach { q ->
            check(q.accepts(q.answer.uppercase() + "  "))
            check(q.explanation.isNotBlank())
            if (!q.isOpen) check(q.options.size == 4 && q.options.toSet().size == 4 && q.answer in q.options)
        }
    } }
    val game = CampusGame(random = Random(42))
    game.startLesson(0)
    val active = game.state
    game.rest(); game.study(1); game.work(); game.buy("tea"); game.use("coffee"); game.nextDay(); game.startLesson(1); game.answer(0); game.answerText("")
    check(game.state == active) { "An active quiz must guard all outside actions and invalid input." }
    repeat(4) { game.answerText("wrong answer") }
    check(game.state.passes.isEmpty() && game.state.quiz == null && game.state.xp == 8)
    pass(game, 0)
    check(game.state.passes["kotlin"] == 1)
    val minutes = game.state.minutesLeft
    game.startLesson(0)
    check(game.state.quiz == null && game.state.minutesLeft == minutes) { "Knowledge gate must protect later stages." }
    game.study(0)
    check(game.state.knowledge["kotlin"] == 16)
    check(game.state.minutesLeft == 60)
    game.startLesson(0)
    check(game.state.quiz == null) { "Classes may not overrun the day." }
    game.nextDay(); game.startLesson(0)
    check(game.question!!.isOpen)
    check(restore(game.state) == game.state) { "Quiz progress and hint flags must survive saved-state serialization." }
    val restored = CampusGame(initialState = restore(game.state))
    restored.answerText("  3  ")
    check(restored.state.quiz!!.correct == 1)
    val trick = CampusGame(random = Random(99))
    trick.startLesson(0); trick.trick("fifty")
    check(trick.state.quiz!!.eliminated && trick.state.mana == 50)
    val once = trick.state
    trick.trick("fifty"); check(trick.state == once)
    trick.trick("peek"); check(trick.state.quiz!!.highlighted && trick.state.mana == 30)
    trick.trick("dean"); check(trick.state.quiz!!.guaranteed && trick.state.mana == 0)
    trick.answerText("absurd")
    check(trick.state.quiz!!.correct == 1 && !trick.state.quiz!!.guaranteed)
    val zeroRandom = object : Random() { override fun nextBits(bitCount: Int): Int = 0 }
    val hint = CampusGame(initialState = CampusState(knowledge = mapOf("kotlin" to 60)), random = zeroRandom)
    hint.startLesson(0); check(hint.state.quiz!!.highlighted)
    val shop = CampusGame(random = zeroRandom)
    shop.buy("lunch"); check(shop.state.coins == 76 && shop.state.inventory["lunch"] == 1)
    shop.use("lunch"); check(shop.state.health == 100 && "lunch" !in shop.state.inventory && shop.state.minutesLeft == 355)
    val inventory = shop.state.inventory
    shop.use("lunch"); check(shop.state.inventory == inventory)
    shop.work(); check(shop.state.coins == 121 && shop.state.inventory["coffee"] == 2)
    val broke = CampusGame(initialState = CampusState(coins = 0))
    broke.buy("coffee"); check(broke.state.coins == 0 && broke.state.inventory["coffee"] == 1)
    val full = CampusGame(initialState = CampusState(inventory = mapOf("coffee" to 20)), random = zeroRandom)
    full.buy("tea"); full.work(); check(full.state.inventory.values.sum() == 20)
    val deadline = CampusGame(initialState = CampusState(semesterDays = 12, day = 12))
    deadline.nextDay(); check(deadline.state.finished && !deadline.state.won)
    val ended = deadline.state
    deadline.rest(); deadline.work(); deadline.buy("tea"); deadline.nextDay(); deadline.startLesson(0)
    check(deadline.state == ended)
    deadline.restart(2); check(deadline.state.semesterDays == 12 && deadline.state.day == 1)
    deadline.restart(100); check(deadline.state.semesterDays == 60)
    // A conservative path includes a paid meal + shift before each course, library preparation,
    // and rests as needed. It must be winnable without spending any smarts or random gifts.
    fun simulate(days: Int): Int {
        val g = CampusGame(initialState = CampusState(semesterDays = days), random = Random(123))
        fun room(minutes: Int, mind: Int = 16) {
            if (g.state.minutesLeft < minutes || g.state.mind < mind || g.state.health < 20) g.nextDay()
            check(!g.state.finished) { "Balance failed at day ${g.state.day}: ${g.state.message}" }
        }
        catalog.forEachIndexed { index, course ->
            room(120, 20); g.work(); g.buy("lunch"); room(5); g.use("lunch")
            repeat(course.requiredPasses) { stage ->
                while ((g.state.knowledge[course.id] ?: 0) < course.requiredKnowledge(stage)) {
                    room(60, 10); g.study(index)
                }
                room(120); pass(g, index)
                check(g.state.mana in 0..100 && g.state.health in 1..100 && g.state.mind in 0..100 && g.state.coins >= 0)
            }
        }
        check(g.state.won && g.state.finished)
        val victory = g.state
        g.startLesson(0); g.buy("coffee"); g.work(); g.nextDay()
        check(g.state == victory)
        return g.state.day
    }
    val day = simulate(24)
    println("PASS: 7 courses, 24 stages, 96 questions, typed answers, guards, failure/retry, knowledge gates, tricks, hints, economy, inventory, deadline, saved-state round trip.")
    println("BALANCE: conservative full playthrough won on day $day/24, with 7 jobs and 7 meals; no smarts needed.")
}
