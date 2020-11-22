package commands.meta

import Hakibot
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.*
import java.awt.Color
import java.util.*

object HelpCommand : BotCommand {

    override val name: String
        get() = "help"

    override val description: String
        get() = "Displays a list of commands or info about a specific command"

    override val usages: List<CommandUsage>
        get() = listOf(
                CommandUsage(emptyList(), "Displays a list of all commands"),
                CommandUsage(listOf(Argument("command", ArgumentType.PARAMETER)),
                        "Displays Info about a specific command"),
                CommandUsage(listOf(Argument("category", ArgumentType.FLAG_PARAM)),
                        "Displays a list of all commands in a specific category"),
        )

    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT


    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        when (args.size) {
            0 -> {
                val map: MutableMap<CommandCategory, MutableList<String>> = EnumMap(CommandCategory::class.java)
                for (cmd in commands) {
                    map.getOrPut(cmd.category, { mutableListOf() }).add(cmd.name)
                }
//                val helpMSG = StringBuilder("Hakibot Available Commands\n```\n")
//                for (cmd in commands) {
//                    if (cmd.category == CommandCategory.HIDDEN) continue
//                    helpMSG.append("${cmd.name}\n")
//                }
//                helpMSG.append("```")
                mCE.message.channel.createEmbed {
                    title = "${Hakibot.BOT_NAME} Available Commands"
                    color = Color.GREEN
                    footer {
                        icon = "https://cdn.discordapp.com/emojis/770777353956753419.png"
                        text = "h!help <cmd> for more information about a specific command"
                    }
                    for ((category, cmds) in map) {
                        if (category != CommandCategory.HIDDEN) {
                            field {
                                name = category.category
                                value = "`${cmds.joinToString("` `")}`"
                            }
                        }
                    }
                }
//                mCE.message.channel.createMessage(helpMSG.toString())
            }
            else -> {
                if (args.first().startsWith("-")) {
                    val categoryStr = args.joinToString(" ").drop(1)
                    val category = CommandCategory.values().find { it.category.toLowerCase() == categoryStr.toLowerCase().trim() }
                    val cmds = commands.filter { it.category == category }
                    if (cmds.isNotEmpty()) {
                        mCE.message.channel.createEmbed {
                            title = "${category!!.category} Commands"
                            description = cmds.map { it.name }.joinToString("\n")
                        }
                    } else {
                        sendMessage(mCE.message.channel, "Could find $categoryStr category")
                    }

                } else if (args.size == 1) {
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
                        sendMessage(mCE.message.channel, "No ${args.first()} command found", 5_000)
                    }
                } else {
                    sendMessage(mCE.message.channel, "Invalid help format. Check `h!help help` for more details", 5_000)
                }
            }
        }
    }
}