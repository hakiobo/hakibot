package commands.meta

import Hakibot
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.BotCommand
import commands.utils.CommandCategory
import java.awt.Color

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
        mCE.message.channel.createEmbed {

            title = "${Hakibot.BOT_NAME} Status"
            for (feature in features) {
                field {
                    name = feature.desc
                    value = if (feature.property.get(this@cmd)) {
                        "Enabled \u2705"
                    } else {
                        numDisabled++
                        "Disabled \u274c"
                    }
                }
            }
            color = Color((numDisabled * 255) / features.size, 255 - (numDisabled * 255) / features.size, 0)

        }
    }
}