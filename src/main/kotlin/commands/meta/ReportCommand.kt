package commands.meta

import Hakibot
import Hakibot.Companion.REPORT_CHANNEL
import commands.utils.*
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.rest.builder.message.EmbedBuilder

object ReportCommand : BotCommand {
    override val name: String
        get() = "report"
    override val description: String
        get() = "Report a problem to Hakiobo!"

    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(
                listOf(Argument("issue", ArgumentType.TEXT)),
                "Report an issue to the Hakibot dev"
        ))

    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (args.isNotEmpty()) {
            val embed = EmbedBuilder().apply {
                description = args.joinToString(" ")
                author {
                    name = "Report from ${mCE.message.author?.tag ?: "Nobody"}"
                    icon = mCE.message.author?.avatar?.url
                }
                footer {
                    text = mCE.message.author!!.id.asString
                }
            }

            val msg = messageChannelById(REPORT_CHANNEL, "", embed)

            client.rest.channel.createReaction(msg.channelId, msg.id, SuggestCommand.TRASH.urlFormat)
            mCE.message.addReaction(ReactionEmoji.Unicode("\ud83c\udd97"))
        } else {
            mCE.message.addReaction(ReactionEmoji.Unicode("\u274c"))
        }
    }
}