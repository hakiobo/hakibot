package commands.meta

import commands.utils.BotCommand
import Hakibot
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.AccessType
import commands.utils.CommandCategory
import commands.utils.CommandUsage
import java.awt.Color

object InviteCommand : BotCommand {
    override val name: String
        get() = "invite"
    override val description: String
        get() = "Add Hakibot to your server!"
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(emptyList(), "Displays link to add Hakibot to your server", AccessType.HAKI))
    override val category: CommandCategory
        get() = CommandCategory.HIDDEN

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (mCE.message.author?.id?.longValue == Hakibot.HAKIOBO_ID && args.isNotEmpty()) {
            sendMessage(mCE.message.channel, "https://discord.com/api/oauth2/authorize?client_id=750534176666550384&permissions=346176&scope=bot", 60_000)
        } else {
            mCE.message.channel.createEmbed {
                field {
                    name = "Hakibot is in the max number of servers for an unverified bot (100)"
                    value = "An announcment will be made in the [Hakibot Official Server](https://discord.gg/k3XgR4s) once the bot is verified"
                }

                color = Color(0xCC3333)
            }

        }
    }
}