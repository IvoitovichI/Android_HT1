class QuizRunner(private val demo: Boolean) {
    fun run(quiz: Quiz): Int? {
        println("Mini quiz: four questions; three correct answers are needed to pass.")
        var correct = 0
        quiz.questions.forEachIndexed { index, question ->
            println("${index + 1}. ${question.text}")
            question.options.forEachIndexed { option, text -> println("  ${option + 1}) $text") }
            var answer: Int?
            do {
                if (!demo) print("Answer (1-4): ")
                val input = if (demo) question.correctOption.toString() else readlnOrNull() ?: return null
                answer = input.trim().toIntOrNull()
                if (answer !in 1..4) println("Enter a number from 1 to 4.")
            } while (answer !in 1..4)
            if (demo) println("Answer: $answer")
            if (answer == question.correctOption) {
                correct++
                println("Correct.")
            } else {
                println("Incorrect. Correct answer: ${question.correctOption}.")
            }
        }
        return correct
    }
}
