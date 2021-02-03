package commands.hidden

import Hakibot
import Hakibot.Companion.ONLINE_CHANNEL
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.*

object LogoutCommand : BotCommand{
    override val name: String
        get() = "logout"
    override val description: String
        get() = "Logs the bot out"
    override val category: CommandCategory
        get() = CommandCategory.HIDDEN
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(listOf(Argument("Message",ArgumentType.TEXT)), "Sends the message `Offline: {Message}` to <#761020851029803078> and shuts down", AccessType.HAKI))

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if(mCE.message.author?.id?.longValue == 292483348738080769){
            client.getChannelOf<MessageChannel>(Snowflake( ONLINE_CHANNEL))?.createMessage("Offline: ${args.joinToString(" ")}!")
            client.logout()
        }
    }

}