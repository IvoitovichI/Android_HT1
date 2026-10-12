object Exhaustion {
    private const val baseLoss = 5

    fun apply(student: Student, attemptingLesson: Boolean): Int {
        if (student.mana > 0) return 0
        val loss = baseLoss * if (attemptingLesson) 2 else 1
        student.changeStats(healthDelta = -loss, mindDelta = -loss)
        return loss
    }
}
