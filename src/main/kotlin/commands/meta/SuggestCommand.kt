package commands.meta

import Hakibot
import Hakibot.Companion.SUGGESTION_CHANNEL
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import commands.utils.*


object SuggestCommand : BotCommand {
    val THUMBS_UP = ReactionEmoji.Unicode("\ud83d\udc4d")
    val THUMBS_DOWN = ReactionEmoji.Unicode("\ud83d\udc4e")
    val TRASH = ReactionEmoji.Unicode("\ud83d\uddd1\ufe0f")

    override val name: String
        get() = "suggest"

    override val description: String
        get() = "Suggest something to add to Hakibot!"

    override val aliases: List<String>
        get() = listOf("sug", "suggestion", "feedback")

    override val usages: List<CommandUsage>
        get() = listOf(
                CommandUsage(
                        listOf(Argument("suggestion", ArgumentType.TEXT)),
                        "Sends your suggestion to Hakibot dev"
                )
        )

    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (args.isNotEmpty()) {

            val embed = EmbedBuilder().apply {
                description = args.joinToString(" ")
                author {
                    name = "${mCE.message.author?.tag ?: "Nobody"}'s Suggestion"
                    icon = mCE.message.author?.avatar?.url
                }
            }
            val msg = messageChannelById(SUGGESTION_CHANNEL, "", embed)
            client.rest.channel.createReaction(msg.channelId, msg.id, THUMBS_UP.urlFormat)
            client.rest.channel.createReaction(msg.channelId, msg.id, THUMBS_DOWN.urlFormat)
            client.rest.channel.createReaction(msg.channelId, msg.id, TRASH.urlFormat)

            mCE.message.addReaction(ReactionEmoji.Unicode("\ud83c\udd97"))
        } else {
            mCE.message.addReaction(ReactionEmoji.Unicode("\u274c"))
        }
    }
}