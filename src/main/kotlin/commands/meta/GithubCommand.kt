package commands.meta

import Hakibot
import commands.utils.BotCommand
import commands.utils.CommandCategory
import dev.kord.core.event.message.MessageCreateEvent

object GithubCommand : BotCommand {
    override val name: String
        get() = "github"
    override val description: String
        get() = "Links to the github containing Hakibot's code"
    override val aliases: List<String>
        get() = listOf("git", "code")
    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        sendMessage(mCE.message.channel, "Hakibot code is here: https://github.com/hakiobo/hakibot")
    }
}