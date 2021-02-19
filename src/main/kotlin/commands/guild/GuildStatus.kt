package commands.guild

import Hakibot
import commands.utils.BotCommand
import commands.utils.CommandCategory
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import entities.HakiGuild
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

object GuildStatus : BotCommand {
    override val name: String
        get() = "serverstatus"
    override val aliases: List<String>
        get() = listOf(
            "guildstatus",
            "gstatus",
            "sstatus",
            "gsettings",
            "ssettings",
            "guildsettings",
            "serversettings",
            "ginfo",
            "sinfo",
            "guildinfo",
            "serverinfo",
            "gs",
            "ss",
            "gi",
            "si",
        )
    override val description: String
        get() = "Shows the status of all settings specific to this server"
    override val category: CommandCategory
        get() = CommandCategory.GUILD

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        val guild = db.getCollection<HakiGuild>(HakiGuild.DB_NAME).findOne(HakiGuild::_id eq mCE.guildId!!.asString)
        if (guild == null) {
            sendMessage(mCE.message.channel, "no data found for this server somehow. sad", 5_000)
        } else {
            mCE.message.channel.createEmbed {
                title = " ${Hakibot.BOT_NAME} Settings for ${mCE.getGuild()!!.name}"
                description =
                    "Prefix: ${guild.prefix}\n" +
                            "Global Prefix (${Hakibot.GLOBAL_PREFIX}) Enabled : ${Hakibot.getCheckmarkOrCross(guild.settings.allowGlobalPrefix)}\n" +
                            "<@${Hakibot.OWO_ID}> prefix: ${guild.owoPrefix}\n" +
                            "Triggers Enabled: ${Hakibot.getCheckmarkOrCross(guild.settings.enableTriggers)}\n" +
                            "When Responses Enabled: ${Hakibot.getCheckmarkOrCross(guild.settings.enableWhen)}\n" +
                            "OwO Counting Enabled: ${Hakibot.getCheckmarkOrCross(guild.settings.owoCountingEnabled)}\n"
                field {
                    value = "Questions? Ask in the official [Hakibot Server](https://discord.gg/${Hakibot.SERVER_CODE})!"
                }
            }
        }
    }
}

