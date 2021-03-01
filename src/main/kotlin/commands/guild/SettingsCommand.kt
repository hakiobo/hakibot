package commands.guild

import entities.HakiGuild
import Hakibot
import entities.Settings
import commands.utils.BotCommand
import commands.utils.CommandCategory
import dev.kord.common.entity.Permission
import dev.kord.core.event.message.MessageCreateEvent
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import kotlin.reflect.KProperty1

interface SettingsCommand : BotCommand {
    val setting: KProperty1<Settings, Boolean>

    override val category: CommandCategory
        get() = CommandCategory.GUILD


    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        when (args.size) {
            0 -> {
                sendMessage(
                    mCE.message.channel,
                    "Current setting is `${setting.get(getGuildInfo(mCE.guildId!!).settings)}`"
                )
            }
            1 -> {
                if (mCE.member!!.getPermissions()
                        .contains(Permission.Administrator) || mCE.member!!.id.value == Hakibot.HAKIOBO_ID
                ) {
                    when (args.first().toLowerCase()) {
                        "true" -> {
                            val col = db.getCollection<HakiGuild>("guilds")
                            val curGuild = getGuildInfo(mCE.guildId!!, col)
                            col.updateOne(
                                HakiGuild::_id eq curGuild._id,
                                setValue(HakiGuild::settings / setting, true)
                            )
                            sendMessage(mCE.message.channel, "Set to True", 10_000)
                        }
                        "false" -> {
                            val col = db.getCollection<HakiGuild>("guilds")
                            val curGuild = getGuildInfo(mCE.guildId!!, col)
                            col.updateOne(
                                HakiGuild::_id eq curGuild._id,
                                setValue(HakiGuild::settings / setting, false)
                            )
                            sendMessage(mCE.message.channel, "Set to False", 10_000)
                        }
                        else -> sendMessage(mCE.message.channel, "Incorrect format! try `h!help $name`", 5_000)
                    }
                } else {
                    sendMessage(mCE.message.channel, "Insufficient Permissions!", 5_000)
                }
            }
            else -> sendMessage(mCE.message.channel, "Incorrect format! try `h!help $name`", 5_000)
        }
    }
}