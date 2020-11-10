package commands

import commands.utils.BotCommand
import Hakibot
import Hakibot.Companion.SUGGESTION_CHANNEL
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.behavior.getChannelOf
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.gitlab.kordlib.core.entity.channel.MessageChannel
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.Argument
import commands.utils.ArgumentType
import commands.utils.CommandUsage


class SuggestCommand : BotCommand {

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

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (args.isNotEmpty()) {
            client.getChannelOf<MessageChannel>(Snowflake(SUGGESTION_CHANNEL))?.createMessage(
                "Suggestion from ${mCE.message.author!!.tag}: ${
                    args.joinToString(
                        " "
                    )
                }"
            )
            mCE.message.addReaction(ReactionEmoji.Unicode("\ud83c\udd97"))
        } else {
            mCE.message.addReaction(ReactionEmoji.Unicode("\u274c"))
        }
    }
}