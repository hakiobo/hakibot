package commands

import Hakibot
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.BotCommand
import commands.utils.CommandUsage

class ServerCommand : BotCommand {
    override val name: String
        get() = "server"
    override val aliases: List<String>
        get() = listOf("guild", "serverlink", "guildink", "link", "join")
    override val description: String
        get() = "Links an invite to Hakibot's server"
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(emptyList(), "Displays the Invite to Hakibot Server"))
//    override val category: CommandCategory
//        get() = CommandCategory.HIDDEN

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
//        if (mCE.message.author == getHaki()) {
        sendMessage(mCE.message.channel, "https://discord.gg/k3XgR4s", 60_000)
//        } else {
//            sendAndDelete(mCE.message.channel, "Hakibot server not ready to be public yet", 5_000)
//        }
    }

}