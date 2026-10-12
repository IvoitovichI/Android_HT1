import kotlinx.coroutines.delay

class AttendLesson(private val lesson: Lesson, private val quizRunner: QuizRunner) : CampusAction {
    override val title = "Attend class: ${lesson.title}"
    override val durationMinutes = 30

    override suspend fun perform(student: Student): ActionResult {
        if (lesson.title in student.completedLessons) return ActionResult.Failure("You have already passed this class.")
        if (student.mana < lesson.manaCost) return ActionResult.Failure("Not enough mana: ${lesson.manaCost} required.")

        println("The teacher asks a question about ${lesson.subject}...")
        delay(300)
        val focus = student.mind / 10 + student.mana / 10
        student.changeStats(manaDelta = -lesson.manaCost, mindDelta = -lesson.difficulty * 4)
        if (focus < lesson.difficulty * 3) {
            student.changeStats(healthDelta = -lesson.difficulty * 5)
            return ActionResult.Failure("You could not focus. Health decreased; try resting.")
        }

        val correct = quizRunner.run(lesson.quiz)
            ?: return ActionResult.Failure("Input ended before the quiz was completed.")
        if (correct < 3) return ActionResult.Failure("Quiz failed: $correct/4 correct. Try this class again.")

        student.completedLessons += lesson.title
        student.knowledge[lesson.subject] = (student.knowledge[lesson.subject] ?: 0) + lesson.difficulty * 10
        return ActionResult.Success("Class passed! Quiz: $correct/4. +${lesson.difficulty * 10} knowledge in ${lesson.subject}.")
    }
}
