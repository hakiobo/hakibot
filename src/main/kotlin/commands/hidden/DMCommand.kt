package commands.hidden

import Hakibot
import commands.utils.*
import dev.kord.core.event.message.MessageCreateEvent

object DMCommand : BotCommand {

    override val name: String
        get() = "dm"

    override val description: String
        get() = "DMs a user by their id"

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(
                listOf(
                    Argument("userid", ArgumentType.PARAMETER),
                    Argument("message", ArgumentType.TEXT)
                ), "Sends a message to the User with the specified ID", AccessType.HAKI
            )
        )

    override val category: CommandCategory
        get() = CommandCategory.HIDDEN

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (mCE.message.author?.id?.value == Hakibot.HAKIOBO_ID) {
            val userID = args.firstOrNull()?.toLongOrNull()
            if (userID != null) {
                dmUser(userID, args.drop(1).joinToString(" "))
            } else {
                sendMessage(mCE.message.channel, "That's not a userID")
            }
        } else {
            sendMessage(mCE.message.channel, "Only Haki can use the DM command")
        }
    }
}