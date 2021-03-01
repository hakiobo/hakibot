package commands

import commands.utils.BotCommand
import Hakibot
import entities.HakiUser
import entities.OWOSettings
import commands.utils.Argument
import commands.utils.CommandCategory
import commands.utils.CommandUsage
import dev.kord.core.event.message.MessageCreateEvent
import org.litote.kmongo.div
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

object OWOHuntSettingsCMD : BotCommand {

    override val name: String
        get() = "owohunt"

    override val description: String
        get() = "Changes or sets your owo hunt reminders from Hakibot"

    override val aliases: List<String>
        get() = listOf("owoh", "hunt", "h")

    override val usages: List<CommandUsage>
        get() = listOf(
                CommandUsage(emptyList(), "Changes your owo hunt reminder"),
                CommandUsage(listOf(Argument(listOf("true", "false"))),
                        "Sets your owohunt reminder")
        )

    override val category: CommandCategory
        get() = CommandCategory.REMINDER

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        val user = getUserFromDB(mCE.message.author!!.id)
        val col = db.getCollection<HakiUser>("users")
        when (args.size) {
            0 -> {
                col.updateOne(
                        HakiUser::_id eq user._id,
                        setValue(HakiUser::owoSettings / OWOSettings::huntRemind, !user.owoSettings.huntRemind)
                )
                sendMessage(mCE.message.channel, "owohunt remind set to ${!user.owoSettings.huntRemind}")
            }

            1 -> {
                when (args.first().toLowerCase()) {
                    "true" -> {
                        col.updateOne(
                                HakiUser::_id eq user._id,
                                setValue(HakiUser::owoSettings / OWOSettings::huntRemind, true)
                        )
                        sendMessage(mCE.message.channel, "owohunt remind set to true")
                    }

                    "false" -> {
                        col.updateOne(
                                HakiUser::_id eq user._id,
                                setValue(HakiUser::owoSettings / OWOSettings::huntRemind, false)
                        )
                        sendMessage(mCE.message.channel, "owohunt remind set to false")
                    }
                    else -> {
                        sendMessage(mCE.message.channel, "invalid hunt setting, can only be true or false")
                    }
                }
            }
            else -> {
                sendMessage(mCE.message.channel, "invalid hunt setting, can only be true or false")
            }
        }
    }
}