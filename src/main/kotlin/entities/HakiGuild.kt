package entities

data class HakiGuild(
        val _id: String,
        val prefix: String = "h!",
        val owoPrefix: String = "owo",
        val settings: Settings = Settings(),
        val lastOwONormalize: Long = 0,
)

data class Settings(val enableWhen: Boolean = false, val enableTriggers: Boolean = false, val allowGlobalPrefix: Boolean = true)