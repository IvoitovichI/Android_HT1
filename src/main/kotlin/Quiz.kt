data class Quiz(val questions: List<Question>) {
    init {
        require(questions.size == 4) { "A quiz needs exactly four questions." }
    }
}
