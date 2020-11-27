package commands

import Hakibot
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.Argument
import commands.utils.ArgumentType
import commands.utils.BotCommand
import commands.utils.CommandUsage
import math.evaluate

object MathCommand : BotCommand {
    override val name: String
        get() = "math"
    override val description: String
        get() = "Let Hakibot do some math for you!\n"
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(
                listOf(Argument("expression", ArgumentType.TEXT)),
                "Evaluates your math expression"
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        client.runCatching {
            evaluate(args.joinToString(""))
        }.onFailure {
            sendMessage(mCE.message.channel, "Could not parse your expression!")
        }.onSuccess {
            sendMessage(mCE.message.channel, it.toString())
        }
    }
}