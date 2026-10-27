package campus

data class ExamQuestion(val text: String, val answer: String, val options: List<String> = emptyList(),
    val aliases: List<String> = emptyList(), val explanation: String) {
    val isOpen get() = options.isEmpty()
    fun accepts(input: String): Boolean {
        fun normalize(value: String) = value.trim().lowercase().replace('ё', 'е').replace(Regex("[\\s.!?,;:]+"), " ").trim()
        return (listOf(answer) + aliases).any { normalize(it) == normalize(input) }
    }
}
