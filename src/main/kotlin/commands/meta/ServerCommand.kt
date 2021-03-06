package commands.meta

import Hakibot
import commands.utils.BotCommand
import commands.utils.CommandCategory
import commands.utils.CommandUsage
import dev.kord.core.event.message.MessageCreateEvent

object ServerCommand : BotCommand {
    override val name: String
        get() = "server"
    override val aliases: List<String>
        get() = listOf("guild", "serverlink", "guildlink", "link", "join")
    override val description: String
        get() = "Links an invite to Hakibot's server"
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(emptyList(), "Displays the Invite to Hakibot Server"))
    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        sendMessage(mCE.message.channel, "https://discord.gg/${Hakibot.SERVER_CODE}")
    }

}