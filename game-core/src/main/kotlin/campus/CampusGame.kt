package campus

import kotlin.random.Random

class CampusGame(val lessons: List<Course> = CourseCatalog.courses,
    initialState: CampusState = CampusState(), private val random: Random = Random.Default) {
    var state = initialState
        private set
    val question: ExamQuestion? get() = state.quiz?.let { lessons[it.lessonIndex].stages[it.stage][it.questionIndex] }
    fun restart(days: Int = 24) { state = CampusState(semesterDays = days.coerceIn(12, 60)) }
    fun startLesson(index: Int) {
        if (blocked()) return
        val course = lessons.getOrNull(index) ?: return
        val stage = state.passes[course.id] ?: 0
        if (stage >= course.requiredPasses) { announce("Все этапы этого предмета уже сданы."); return }
        if ((state.knowledge[course.id] ?: 0) < course.requiredKnowledge(stage)) {
            announce("Для этапа ${stage + 1} нужно ${course.requiredKnowledge(stage)} знаний. Загляните в библиотеку."); return
        }
        if (!canSpend(120)) return
        if (state.health <= 5 || state.mind < 16) { announce("Перед зачётом поешьте или отдохните: нужно >5 здоровья и ≥16 концентрации."); return }
        state = state.copy(minutesLeft = state.minutesLeft - 120, health = state.health - 3, mind = state.mind - 12,
            quiz = QuizProgress(index, stage), message = "Этап ${stage + 1}: ответьте верно минимум на 3 из 4 вопросов.")
        rollHint()
    }
    fun answer(option: Int) {
        val current = question ?: return
        if (option !in 1..current.options.size) return
        answerText(current.options[option - 1])
    }
    fun answerText(value: String) {
        val progress = state.quiz ?: return
        val current = question ?: return
        if (value.isBlank() && !progress.guaranteed) return
        val right = progress.guaranteed || current.accepts(value)
        val correct = progress.correct + if (right) 1 else 0
        val feedback = (if (right) "Верно! " else "Правильный ответ: ${current.answer}. ") + current.explanation
        if (progress.questionIndex < 3) {
            state = state.copy(quiz = progress.copy(questionIndex = progress.questionIndex + 1, correct = correct,
                highlighted = false, eliminated = false, guaranteed = false))
            announce(feedback); rollHint()
        } else {
            val course = lessons[progress.lessonIndex]
            val passed = correct >= 3
            val completed = passed && progress.stage + 1 == course.requiredPasses
            val reward = if (passed) 8 + (if (completed) 25 else 0) else 0
            state = state.copy(quiz = null,
                passes = if (passed) state.passes + (course.id to progress.stage + 1) else state.passes,
                knowledge = if (passed) state.knowledge + (course.id to ((state.knowledge[course.id] ?: 0) + 4).coerceAtMost(60)) else state.knowledge,
                coins = state.coins + reward, xp = state.xp + if (passed) 35 else 8)
            val won = lessons.all { (state.passes[it.id] ?: 0) >= it.requiredPasses }
            state = state.copy(won = won, finished = won)
            announce(feedback + "\n" + when {
                won -> "Семестр закрыт! Даже декан впечатлён."
                passed -> "Этап сдан: $correct/4. +35 XP, +4 знания, +$reward монет."
                else -> "Пока $correct/4. Этап можно пересдать после подготовки. +8 XP за опыт."
            })
        }
    }
    fun study(index: Int) {
        if (blocked()) return
        val course = lessons.getOrNull(index) ?: return
        if (!canSpend(60)) return
        if (state.mind < 10) { announce("Для чтения нужно хотя бы 10 концентрации. Отдохните."); return }
        val old = state.knowledge[course.id] ?: 0
        if (old >= 60) { announce("По этому предмету уже максимум знаний: 60."); return }
        state = state.copy(minutesLeft = state.minutesLeft - 60, mind = state.mind - 6,
            knowledge = state.knowledge + (course.id to (old + 12).coerceAtMost(60)), xp = state.xp + 12)
        announce("Библиотека: +12 знаний по «${course.title}», +12 XP. Конспект доступен ниже. Знания иногда подсвечивают ответ.")
    }
    fun trick(kind: String) {
        val progress = state.quiz ?: return
        val current = question ?: return
        val cost = when (kind) { "peek" -> 20; "fifty" -> 10; "dean" -> 30; else -> return }
        if (progress.guaranteed || (kind == "peek" && progress.highlighted) ||
            (kind == "fifty" && (current.isOpen || progress.eliminated || progress.highlighted))) return
        if (state.mana < cost) { announce("Не хватает смекалки: нужно $cost."); return }
        state = state.copy(mana = state.mana - cost, quiz = when (kind) {
            "peek" -> progress.copy(highlighted = true)
            "fifty" -> progress.copy(eliminated = true)
            else -> progress.copy(guaranteed = true, highlighted = true)
        })
        announce(when (kind) {
            "peek" -> "Сосед прикрыл ответ ладонью… слишком поздно. Правильный ответ открыт."
            "fifty" -> "Призрак старшекурсника вычеркнул два неверных варианта."
            else -> "Призыв деканата! Следующий ответ будет засчитан верно. Бюрократия сильнее магии."
        })
    }
    fun rest() {
        if (blocked() || !canSpend(60)) return
        state = state.copy(minutesLeft = state.minutesLeft - 60,
            health = (state.health + 10).coerceAtMost(100), mind = (state.mind + 30).coerceAtMost(100))
        announce("Отдых: +10 здоровья, +30 концентрации. −60 минут.")
    }
    fun nextDay() {
        if (blocked()) return
        if (state.day >= state.semesterDays) {
            state = state.copy(finished = true)
            announce("Семестр закончился. Сдано ${state.passes.values.sum()} из ${lessons.sumOf { it.requiredPasses }} этапов. Новый семестр — новый шанс."); return
        }
        state = state.copy(day = state.day + 1, minutesLeft = DAY_MINUTES,
            health = (state.health + 25).coerceAtMost(100), mind = (state.mind + 40).coerceAtMost(100), mana = (state.mana + 18).coerceAtMost(100))
        announce("Новый день: сон восстановил +25 здоровья, +40 концентрации и +18 смекалки.")
    }
    fun work() {
        if (blocked() || !canSpend(120)) return
        if (state.health < 15 || state.mind < 20) { announce("На смену нужно ≥15 здоровья и ≥20 концентрации."); return }
        state = state.copy(minutesLeft = state.minutesLeft - 120, health = state.health - 8,
            mind = state.mind - 12, coins = state.coins + 45, xp = state.xp + 15)
        val gift = state.inventory.values.sum() < 20 && random.nextInt(100) < 25
        if (gift) state = state.copy(inventory = state.inventory + ("coffee" to ((state.inventory["coffee"] ?: 0) + 1)))
        announce("Подработка курьером: +45 монет, +15 XP, −8 здоровья, −12 концентрации." + if (gift) " Бонус: кофе в рюкзаке!" else "")
    }
    fun buy(id: String) {
        if (blocked()) return
        val item = CampusEconomy.items.find { it.id == id } ?: return
        if (state.coins < item.price) { announce("Недостаточно монет. Подработка приносит 45 монет за смену."); return }
        if (state.inventory.values.sum() >= 20) { announce("В рюкзаке максимум 20 вещей. Используйте запасы."); return }
        state = state.copy(coins = state.coins - item.price, inventory = state.inventory + (id to ((state.inventory[id] ?: 0) + 1)))
        announce("В рюкзак добавлено: ${item.title}. −${item.price} монет.")
    }
    fun use(id: String) {
        if (blocked() || !canSpend(5)) return
        val item = CampusEconomy.items.find { it.id == id } ?: return
        val count = state.inventory[id] ?: 0
        if (count <= 0) return
        state = state.copy(minutesLeft = state.minutesLeft - 5, health = (state.health + item.health).coerceIn(1, 100),
            mana = (state.mana + item.mana).coerceIn(0, 100), mind = (state.mind + item.mind).coerceIn(0, 100),
            inventory = if (count == 1) state.inventory - id else state.inventory + (id to count - 1))
        announce("${item.title}: ${item.description}. −5 минут.")
    }
    private fun blocked() = state.finished || state.quiz != null
    private fun canSpend(minutes: Int): Boolean {
        if (state.minutesLeft >= minutes) return true
        announce("Сегодня не хватает времени: нужно $minutes минут. Завершите день в кампусе."); return false
    }
    private fun rollHint() {
        val progress = state.quiz ?: return
        val knowledge = state.knowledge[lessons[progress.lessonIndex].id] ?: 0
        val chance = (knowledge * 0.7).toInt().coerceAtMost(40)
        state = state.copy(quiz = progress.copy(highlighted = random.nextInt(100) < chance))
    }
    private fun announce(message: String) {
        state = state.copy(message = message, journal = (listOf("День ${state.day}: $message") + state.journal).take(40))
    }
}
