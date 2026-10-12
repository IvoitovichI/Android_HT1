class Coffee : CampusAction {
    override val title = "Drink coffee (+mana, -health)"
    override val durationMinutes = 5

    override suspend fun perform(student: Student): ActionResult {
        student.changeStats(healthDelta = -8, manaDelta = 25)
        return ActionResult.Success("Coffee restored mana but cost some health.")
    }
}
