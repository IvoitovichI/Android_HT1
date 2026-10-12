data class Question(val text: String, val options: List<String>, val correctOption: Int) {
    init {
        require(options.size == 4) { "A question needs exactly four options." }
        require(options.distinct().size == 4) { "Question options must be different." }
        require(correctOption in 1..4) { "Correct option must be from 1 to 4." }
    }
}
