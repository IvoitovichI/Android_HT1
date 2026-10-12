interface CampusAction {
    val title: String
    val durationMinutes: Int
    suspend fun perform(student: Student): ActionResult
}
