package commands

import Hakibot
import commands.utils.Argument
import commands.utils.ArgumentType
import commands.utils.BotCommand
import commands.utils.CommandUsage
import dev.kord.core.event.message.MessageCreateEvent
import math.evaluate
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.sqrt

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
            evaluate(
                args.joinToString("").toLowerCase().map {
                    when (it) {
                        '[', '{' -> '('
                        ']', '}' -> ')'
                        else -> it
                    }
                }.joinToString(""),
                mapOf(
                    "e" to E,
                    "pi" to PI,
                    "phi" to (sqrt(5.0) + 1) / 2,
                    "inf" to Double.POSITIVE_INFINITY,
                    "infinity" to Double.POSITIVE_INFINITY
                )
            )
        }.onFailure {
            sendMessage(mCE.message.channel, "Could not parse your expression!")
        }.onSuccess {
            sendMessage(mCE.message.channel, if (it.toString().length >= 2000) "Answer was too long" else it.toString())
        }
    }
}