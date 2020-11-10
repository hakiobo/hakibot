package commands

import Hakibot
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.AccessType
import commands.utils.BotCommand
import commands.utils.CommandCategory
import commands.utils.CommandUsage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

class ListGuildsCommand : BotCommand {
    override val name: String
        get() = "listguilds"
    override val description: String
        get() = "Lists the guilds Hakibot is a part of"
    override val aliases: List<String>
        get() = listOf("guilds")
    override val category: CommandCategory
        get() = CommandCategory.HIDDEN
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(listOf(), "Lists the guilds Hakibot is currently in"))

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
//        if (mCE.message.author?.id?.longValue == Hakibot.HAKIOBO_ID) {
//            if (args.isEmpty()) {
//                val guilds =
//                    client.rest.user.getCurrentUserGuilds(limit = 100).map { it.name }
//                mCE.message.channel.createMessage(
//                    "**Hakibot Guilds ${guilds.size}**\n${
//                        guilds.sortedBy { it.toLowerCase() }.joinToString("\n")
//                    }"
//                )
//            } else {
            val guilds = mutableListOf<Guild>()
            client.guilds.onEach { guilds.add(it) }.collect()
//            guilds.sortedBy { it.name }
            mCE.message.channel.createMessage("${guilds.size} Hakibot Guilds")
//            mCE.message.channel.createMessage("**Hakibot Guilds ${guilds.size}**\n${guilds.joinToString("\n")}")
//            }
//        } else {
//            sendAndDelete(mCE.message.channel, "Only haki can list the guilds Hakibot is in", 5_000)
//        }
    }
}