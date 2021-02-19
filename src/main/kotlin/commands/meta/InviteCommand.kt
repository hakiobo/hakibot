package commands.meta
import commands.utils.BotCommand
import Hakibot
import commands.utils.CommandCategory
import commands.utils.CommandUsage
import dev.kord.core.event.message.MessageCreateEvent

object InviteCommand : BotCommand {
    override val name: String
        get() = "invite"
    override val description: String
        get() = "Add Hakibot to your server!"
    override val usages: List<CommandUsage>
        get() = listOf(CommandUsage(emptyList(), "Displays link to add Hakibot to your server"))
    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        sendMessage(
            mCE.message.channel,
            "https://discord.com/api/oauth2/authorize?client_id=750534176666550384&permissions=346176&scope=bot"
        )
    }
}