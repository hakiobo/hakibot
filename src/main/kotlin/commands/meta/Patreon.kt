package commands.meta

import Hakibot
import commands.utils.BotCommand
import dev.kord.core.event.message.MessageCreateEvent

object Patreon : BotCommand {
    override val name: String
        get() = "patreon"
    override val aliases: List<String>
        get() = listOf("donate")
    override val description: String
        get() = "Help cover the costs to keep Hakibot Running"

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        sendMessage(mCE.message.channel){
            description = "Support ${Hakibot.BOT_NAME} on [patreon](https://www.patreon.com/hakibot) to help keep the bot running smoothly!"
        }
    }
}