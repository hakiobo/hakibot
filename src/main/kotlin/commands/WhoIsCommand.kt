package commands

import commands.utils.BotCommand
import Hakibot
import commands.utils.Argument
import commands.utils.CommandUsage
import dev.kord.common.entity.Snowflake
import dev.kord.core.event.message.MessageCreateEvent

object WhoIsCommand : BotCommand {

    override val name: String
        get() = "whois"

    override val description: String
        get() = "Gets a user's tag from their userID"

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(listOf(Argument("UserId")), "Gets User tag of the user with the provided id")
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        sendMessage(
            mCE.message.channel,
            client.getUser(Snowflake(args.firstOrNull()?.toLongOrNull() ?: 0L))?.tag ?: "no user found"
        )
    }
}