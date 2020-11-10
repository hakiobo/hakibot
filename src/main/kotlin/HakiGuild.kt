data class HakiGuild(
    val _id: String,
    val prefix: String = "h!",
    val owoPrefix: String = "owo",
    val settings: Settings = Settings()
)

data class Settings(val enableWhen: Boolean = false, val enableTriggers: Boolean = false, val allowGlobalPrefix: Boolean = true)