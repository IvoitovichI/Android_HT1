open class Character(val name: String, var health: Int, var mana: Int, var mind: Int) {
    fun changeStats(healthDelta: Int = 0, manaDelta: Int = 0, mindDelta: Int = 0) {
        health = (health + healthDelta).coerceIn(0, 100)
        mana = (mana + manaDelta).coerceIn(0, 100)
        mind = (mind + mindDelta).coerceIn(0, 100)
    }
}
