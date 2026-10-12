sealed class ActionResult {
    data class Success(val message: String) : ActionResult()
    data class Failure(val reason: String) : ActionResult()
    data class GameOver(val reason: String) : ActionResult()
}
