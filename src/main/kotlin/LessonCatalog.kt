object LessonCatalog {
    val lessons = listOf(
        Lesson("Kotlin Basics", "Programming", 2, 15, Quiz(listOf(
            Question("Which keyword declares an immutable variable in Kotlin?", listOf("var", "val", "let", "const"), 2),
            Question("Which type stores true or false?", listOf("Int", "String", "Boolean", "Double"), 3),
            Question("Which function prints a line?", listOf("println", "readln", "filter", "map"), 1),
            Question("What does List contain?", listOf("Only one value", "An ordered collection", "A function", "A package"), 2)
        ))),
        Lesson("Algorithms", "Programming", 3, 20, Quiz(listOf(
            Question("Which search needs sorted input?", listOf("Linear search", "Binary search", "Depth-first search", "Breadth-first search"), 2),
            Question("What is the worst-case cost of scanning n items?", listOf("O(1)", "O(log n)", "O(n)", "O(n^2)"), 3),
            Question("Which structure uses FIFO order?", listOf("Stack", "Queue", "Tree", "Set"), 2),
            Question("Which algorithm finds a shortest path in an unweighted graph?", listOf("BFS", "DFS", "Bubble sort", "Binary search"), 1)
        ))),
        Lesson("Discrete Mathematics", "Mathematics", 4, 25, Quiz(listOf(
            Question("How many subsets does a set of 3 elements have?", listOf("3", "6", "8", "9"), 3),
            Question("What is the negation of true?", listOf("true", "false", "unknown", "zero"), 2),
            Question("What is 5 modulo 2?", listOf("0", "1", "2", "3"), 2),
            Question("Which graph has no cycles?", listOf("Complete graph", "Tree", "Cycle graph", "Directed graph"), 2)
        )))
    )
}
