package commands.hidden

import Hakibot
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.*
import kotlin.reflect.KMutableProperty1

object GlobalDisableCommand : BotCommand {

    override val name: String
        get() = "disable"

    override val description: String
        get() = "disables Hakibot reminders globally"

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(
                listOf(
                    Argument(listOf("hunt", "pray", "when", "triggers")),
                ), "Toggles the specified functionality globally", AccessType.HAKI
            ),
            CommandUsage(
                listOf(
                    Argument(listOf("hunt", "pray", "when", "triggers")),
                    Argument(listOf("true", "false")),
                ), "Sets the specified functionality to the specified setting globally", AccessType.HAKI
            )
        )

    override val category: CommandCategory
        get() = CommandCategory.HIDDEN

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (mCE.message.author?.id?.longValue == Hakibot.HAKIOBO_ID) {
            if (args.isEmpty()) {
                sendMessage(mCE.message.channel, "Invalid Syntax!", 5_000)
            } else {
                val a1 = args.first()
                val a2 = args.drop(1).firstOrNull()
                var flag = false

                val setting = when (a1) {
                    "hunt" -> Hakibot.DisableableFeatures.HUNT_REMINDER
                    "pray", "curse" -> Hakibot.DisableableFeatures.PRAY_REMIND
                    "when" -> Hakibot.DisableableFeatures.WHEN
                    "triggers", "trigger" -> Hakibot.DisableableFeatures.TRIGGERS
                    else -> null
                }
                if (setting == null) {
                    sendMessage(mCE.message.channel, "Invalid Setting!")
                } else {
                    setting.property.set(
                        this, when (a2) {
                            "true" -> true
                            "false" -> false
                            null -> !setting.property.get(this)
                            else -> {
                                flag = true
                                setting.property.get(this)
                            }
                        }
                    )
                    if (flag) {
                        sendMessage(mCE.message.channel, "Invalid Syntax!")
                    } else {
                        sendMessage(mCE.message.channel, "Set to ${setting.property.get(this)}!")
                    }
                }
            }
        } else {
            mCE.message.channel.createMessage("Only Haki can use this command")
        }
    }
}