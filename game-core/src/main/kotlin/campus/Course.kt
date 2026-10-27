package campus

data class Course(val id: String, val title: String, val subtitle: String, val icon: String,
    val stages: List<List<ExamQuestion>>) {
    val requiredPasses get() = stages.size
    fun requiredKnowledge(stage: Int) = stage * 12
}
