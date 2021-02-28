package commands.meta

import Hakibot
import commands.utils.BotCommand
import commands.utils.CommandCategory
import commands.utils.CommandUsage
import dev.kord.core.entity.Guild
import dev.kord.core.event.message.MessageCreateEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

object GuildsCommand : BotCommand {
    override val name: String
        get() = "guilds"
    override val description: String
        get() = "Lists the guilds Hakibot is a part of"
    override val aliases: List<String>
        get() = listOf("guildcount", "servers", "servercount")
    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(listOf(), "Lists the guilds Hakibot is currently in"))

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        val guilds = mutableListOf<Guild>()
        client.guilds.onEach { guilds.add(it) }.collect()
        sendMessage(mCE.message.channel, "${guilds.size} Hakibot Guilds")
    }
}