package commands

import commands.utils.BotCommand
import Hakibot
import HakiUser
import OWOSettings
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.Argument
import commands.utils.CommandUsage
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue

class OWOHuntSettingsCMD : BotCommand {

    override val name: String
        get() = "owohunt"

    override val description: String
        get() = "Changes or sets your owo hunt reminders from Hakibot"

    override val aliases: List<String>
        get() = listOf("owoh", "hunt", "h")

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(emptyList(), "Changes your owo hunt reminder"), CommandUsage(
                listOf(
                    Argument(
                        listOf(
                            "true", "false"
                        )
                    )
                ), "Sets your owohunt reminder"
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        val user = getUserFromDB(mCE.message.author!!.id)
        val col = db.getCollection<HakiUser>("users")
        when (args.size) {
            0 -> {
                col.updateOne(
                    HakiUser::_id eq user._id,
                    setValue(HakiUser::owoSettings / OWOSettings::huntRemind, !user.owoSettings.huntRemind)
                )
                mCE.message.channel.createMessage("owohunt remind set to ${!user.owoSettings.huntRemind}")
            }

            1 -> {
                when (args.first().toLowerCase()) {
                    "true" -> {
                        col.updateOne(
                            HakiUser::_id eq user._id,
                            setValue(HakiUser::owoSettings / OWOSettings::huntRemind, true)
                        )
                        mCE.message.channel.createMessage("owohunt remind set to true")
                    }

                    "false" -> {
                        col.updateOne(
                            HakiUser::_id eq user._id,
                            setValue(HakiUser::owoSettings / OWOSettings::huntRemind, false)
                        )
                        mCE.message.channel.createMessage("owohunt remind set to false")
                    }
                    else -> {
                        mCE.message.channel.createMessage("invalid hunt setting, can only be true or false")
                    }
                }
            }
            else -> {
                mCE.message.channel.createMessage("invalid hunt setting, can only be true or false")
            }
        }
    }
}