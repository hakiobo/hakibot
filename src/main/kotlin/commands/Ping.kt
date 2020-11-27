package commands

import Hakibot
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.BotCommand
import commands.utils.CommandUsage
import toInstant
import java.time.Duration
import java.time.Instant

object Ping : BotCommand {
    override val name: String
        get() = "ping"
    override val description: String
        get() = "Pong!"
    override val aliases: List<String>
        get() = listOf("pong")
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(listOf(), "Ping ${Hakibot.BOT_NAME}!"))

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        sendMessage(
            mCE.message.channel,
            "${Duration.between(mCE.message.id.toInstant(), Instant.now()).toMillis()} ms"
        )
    }
}