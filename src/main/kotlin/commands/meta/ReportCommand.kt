package commands.meta

import Hakibot
import Hakibot.Companion.REPORT_CHANNEL
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import commands.utils.*

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
            }

            val msg = messageChannelById(REPORT_CHANNEL, "", embed)

            client.rest.channel.createReaction(msg.channelId, msg.id, SuggestCommand.TRASH.urlFormat)
            mCE.message.addReaction(ReactionEmoji.Unicode("\ud83c\udd97"))
        } else {
            mCE.message.addReaction(ReactionEmoji.Unicode("\u274c"))
        }
    }
}