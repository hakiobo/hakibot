package commands.guild

import entities.Settings

import commands.utils.AccessType
import commands.utils.Argument
import commands.utils.CommandUsage
import kotlin.reflect.KProperty1


object TriggerCommand : SettingsCommand {
    override val setting: KProperty1<Settings, Boolean>
        get() = Settings::enableTriggers
    override val name: String
        get() = "trigger"
    override val aliases: List<String>
        get() = listOf("triggers")
    override val description: String
        get() = "Sets or checks whether Hakibot replies to various trigger messages"
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(listOf(), "Checks whether Hakibot replies to various trigger messages"),
            CommandUsage(
                listOf(Argument(listOf("true", "false"))),
                "Sets whether Hakibot replies to various trigger messages", AccessType.ADMIN
            )
        )
}