package commands.guild

import commands.utils.AccessType
import commands.utils.Argument
import commands.utils.CommandUsage
import entities.Settings
import kotlin.reflect.KProperty1

object DisableCounting : SettingsCommand {
    override val setting: KProperty1<Settings, Boolean>
        get() = Settings::owoCountingEnabled
    override val name: String
        get() = "owocount"
    override val aliases: List<String>
        get() = listOf("count")
    override val description: String
        get() = "Sets or checks whether Hakibot counts owo's in this server"
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(listOf(), "Checks whether Hakibot is currently counting owos in this server"),
            CommandUsage(
                listOf(Argument(listOf("true", "false"))),
                "Sets whether Hakibot counts owos in this server", AccessType.ADMIN
            )
        )

}