class Rest : CampusAction {
    override val title = "Rest in the dormitory (+health, +mental state)"
    override val durationMinutes = 20

    override suspend fun perform(student: Student): ActionResult {
        student.changeStats(healthDelta = 20, mindDelta = 25)
        return ActionResult.Success("Rest helped you recover.")
    }
}
