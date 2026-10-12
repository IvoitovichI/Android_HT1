class Student(name: String) : Character(name, health = 80, mana = 60, mind = 70) {
    val completedLessons: MutableSet<String> = mutableSetOf()
    val knowledge: MutableMap<String, Int> = mutableMapOf()
}
