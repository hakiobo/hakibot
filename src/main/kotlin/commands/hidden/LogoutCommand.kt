package commands.hidden

import Hakibot
import Hakibot.Companion.ONLINE_CHANNEL
import commands.utils.*
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.message.MessageCreateEvent

object LogoutCommand : BotCommand {
    override val name: String
        get() = "logout"
    override val description: String
        get() = "Logs the bot out"
    override val category: CommandCategory
        get() = CommandCategory.HIDDEN
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(
                listOf(Argument("Message", ArgumentType.TEXT)),
                "Sends the message `Offline: {Message}` to <#761020851029803078> and shuts down",
                AccessType.HAKI
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (mCE.message.author?.id?.value == 292483348738080769) {
            try {
                messageChannelById(ONLINE_CHANNEL, "Offline: ${args.joinToString(" ")}!")
            } finally {
                client.shutdown()
            }
        }
    }

}