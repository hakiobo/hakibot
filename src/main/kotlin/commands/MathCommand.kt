package commands

import Hakibot
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.Argument
import commands.utils.ArgumentType
import commands.utils.BotCommand
import commands.utils.CommandUsage
import pl.kremblewski.expressionevaluator.evaluate

object MathCommand : BotCommand {
    override val name: String
        get() = "math"
    override val description: String
        get() = "Let Hakibot do some math for you!"
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(
                listOf(Argument("expression", ArgumentType.TEXT)),
                "Evaluates your math expression"
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (args.isEmpty()) {
            sendMessage(mCE.message.channel, "Could not parse your expression!")
        } else {
            try {
                val res = evaluate(args.joinToString(""))
                sendMessage(mCE.message.channel, res.toString())
            } catch (e: Exception) {
                sendMessage(mCE.message.channel, "Could not parse your expression!")
            }
        }
    }
}