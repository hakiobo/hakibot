package commands

import commands.utils.BotCommand
import Hakibot
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.AccessType
import commands.utils.CommandCategory
import commands.utils.CommandUsage

class InviteCommand : BotCommand {
    override val name: String
        get() = "invite"
    override val description: String
        get() = "Add Hakibot to your server!"
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(emptyList(), "Displays link to add Hakibot to your server", AccessType.HAKI))
    override val category: CommandCategory
        get() = CommandCategory.HIDDEN

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {

        if (mCE.message.author?.id?.longValue == Hakibot.HAKIOBO_ID) {
            sendMessage(mCE.message.channel, "https://discord.com/api/oauth2/authorize?client_id=750534176666550384&permissions=346176&scope=bot", 60_000)
        } else {
            sendMessage(mCE.message.channel, "Hakibot is in the max number of servers for an unverified bot (100)\nAn announcement will be made once Hakibot is verified")
        }
    }

}