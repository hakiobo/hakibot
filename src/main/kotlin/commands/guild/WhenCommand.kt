package commands.guild

import Settings
import commands.utils.AccessType
import commands.utils.Argument
import commands.utils.CommandUsage

import kotlin.reflect.KProperty1

class WhenCommand : SettingsCommand {
    override val setting: KProperty1<Settings, Boolean>
        get() = Settings::enableWhen
    override val name: String
        get() = "when"
    override val description: String
        get() = "Sets or checks whether Hakibot replies 'when' to messages that end with 'when'"
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(listOf(), "Checks whether Hakibot replies 'when' to messages that end with 'when'"),
            CommandUsage(
                listOf(Argument(listOf("true", "false"))),
                "Sets whether Hakibot replies 'when' to messages that end with 'when'", AccessType.ADMIN
            )
        )
}