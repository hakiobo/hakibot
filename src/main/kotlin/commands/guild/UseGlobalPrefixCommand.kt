package commands.guild

import Hakibot
import Settings
import commands.utils.AccessType
import commands.utils.Argument
import commands.utils.CommandUsage

import kotlin.reflect.KProperty1

class UseGlobalPrefixCommand : SettingsCommand {
    override val setting: KProperty1<Settings, Boolean>
        get() = Settings::allowGlobalPrefix
    override val name: String
        get() = "globalprefix"
    override val aliases: List<String>
        get() = listOf("gprefix", "globalpre", "gpre", "gp")
    override val description: String
        get() = "Checks or sets whether the bot responds to the global bot prefix ${Hakibot.GLOBAL_PREFIX} when it differs from the server prefix"
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(listOf(), "Checks Hakibot's response to the global prefix"),
            CommandUsage(
                listOf(Argument(listOf("true", "false"))),
                "Enables/Disables Hakibot's responses to Global prefix", AccessType.ADMIN
            )
        )
}