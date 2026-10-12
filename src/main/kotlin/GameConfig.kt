data class GameConfig(val timeLimitMinutes: Int = 180, val demo: Boolean = false) {
    init {
        require(timeLimitMinutes > 0) { "Time limit must be positive." }
    }

    companion object {
        fun fromArgs(args: Array<String>): GameConfig {
            val unknown = args.firstOrNull { it != "--demo" && !it.startsWith("--time-limit=") }
            require(unknown == null) { "Unknown argument: $unknown" }
            val limits = args.filter { it.startsWith("--time-limit=") }
            require(limits.size <= 1) { "Specify --time-limit only once." }
            val minutes = if (limits.isEmpty()) 180 else limits.single().substringAfter('=').toIntOrNull()
            require(minutes != null && minutes > 0) { "Time limit must be a positive whole number of minutes." }
            return GameConfig(minutes, "--demo" in args)
        }
    }
}
