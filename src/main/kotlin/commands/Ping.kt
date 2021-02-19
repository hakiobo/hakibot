package commands

import Hakibot
import commands.utils.BotCommand
import commands.utils.CommandUsage
import dev.kord.core.behavior.edit
import dev.kord.core.event.message.MessageCreateEvent
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
        val received = Instant.now()
        val content = "\ud83c\udfd3 Pong! Received in ${Duration.between(mCE.message.id.toInstant(), received).toMillis()} ms"
        val msg = mCE.message.channel.createMessage("$content\nReply Sent In `Waiting . . .`")
        val time = Duration.between(received, msg.id.toInstant()).toMillis()
        msg.edit {
            this.content = "$content\nReply Sent In $time ms"
        }
    }
}