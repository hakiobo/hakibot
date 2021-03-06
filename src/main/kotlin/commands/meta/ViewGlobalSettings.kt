package commands.meta

import Hakibot
import commands.utils.BotCommand
import commands.utils.CommandCategory
import dev.kord.common.Color
import dev.kord.core.event.message.MessageCreateEvent

object ViewGlobalSettings : BotCommand {
    override val name: String
        get() = "status"
    override val aliases: List<String>
        get() = listOf("globalstatus", "globalsettings", "globalinfo")
    override val description: String
        get() = "Shows the current status of hakibot's functionality"
    override val category: CommandCategory
        get() = CommandCategory.HAKIBOT


    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        var numDisabled = 0
        val features = Hakibot.DisableableFeatures.values()
        sendMessage(mCE.message.channel){
            title = "${Hakibot.BOT_NAME} Status"
            for (feature in features) {
                field {
                    name = feature.desc
                    value = if (feature.property.get(this@cmd)) {
                        "Enabled "
                    } else {
                        numDisabled++
                        "Disabled "
                    } + Hakibot.getCheckmarkOrCross(feature.property.get(this@cmd))
                }
            }
            color = Color((numDisabled * 255) / features.size, 255 - (numDisabled * 255) / features.size, 0)
        }
    }
}