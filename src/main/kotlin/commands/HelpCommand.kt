package commands

import Hakibot
import com.gitlab.kordlib.core.entity.Embed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.*

class HelpCommand : BotCommand {

    override val name: String
        get() = "help"

    override val description: String
        get() = "Displays a list of commands or info about a specific command"

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(emptyList(), "Displays a list of all commands"),
            CommandUsage(listOf(Argument("command", ArgumentType.PARAMETER)), "Displays Info about a specific command")
        )


    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        when (args.size) {
            0 -> {
                val helpMSG = StringBuilder("Hakibot Available Commands\n```\n")
                for (cmd in commands) {
                    if(cmd.category == CommandCategory.HIDDEN) continue
                    helpMSG.append("${cmd.name}\n")
                }
                helpMSG.append("```")
                mCE.message.channel.createMessage(helpMSG.toString())
            }
            1 -> {
                val cmd = lookupCMD(args.first())
                if (cmd != null) {
                    val helpMSG = StringBuilder("Help for `${cmd.name}` command\n")

                    helpMSG.append("**Aliases:** ")
                    if (cmd.aliases.isNotEmpty()) {
                        helpMSG.append("`${cmd.aliases.joinToString("`  `")}`")
                    } else {
                        helpMSG.append("None")
                    }
                    helpMSG.append("\n**Description:** `${cmd.description}`")
                    helpMSG.append("\n**Usage:** ")
                    if (cmd.usages.isEmpty()) {
                        helpMSG.append("**None**")
                    } else {
                        for (usage in cmd.usages) {
                            helpMSG.append("\n`h!${cmd.name}")
                            for (arg in usage.args) {
                                helpMSG.append(" ").append(arg.argType.prefix).append(arg.text)
                                    .append(arg.argType.suffix)
                            }
                            helpMSG.append("`- ${usage.description}")
                            if (usage.accessType != AccessType.EVERYONE) {
                                helpMSG.append(" - Requires:`${usage.accessType.desc}`")
                            }
                        }
                    }
                    mCE.message.channel.createMessage(helpMSG.toString())
                } else {
                    mCE.message.channel.createMessage("No ${args.first()} command found")
                }
            }
            else -> {
                mCE.message.channel.createMessage("Invalid help format. Expecting `h!help <cmd>` or `h!help`")
            }
        }
    }


}