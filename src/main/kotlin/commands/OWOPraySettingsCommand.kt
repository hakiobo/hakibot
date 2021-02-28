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
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue

object OWOPraySettingsCommand : BotCommand {

    override val name: String
        get() = "owopray"

    override val description: String
        get() = "Changes or sets your owo pray/curse reminders from Hakibot"

    override val aliases: List<String>
        get() = listOf("pray", "owocurse", "curse")

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(emptyList(), "Changes your owo pray/curse reminder"), CommandUsage(
                listOf(
                    Argument(
                        listOf(
                            "true", "false"
                        )
                    )
                ), "Sets your owo pray/curse reminder"
            )
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
                    setValue(HakiUser::owoSettings / OWOSettings::prayRemind, !user.owoSettings.prayRemind)
                )
                sendMessage(mCE.message.channel, "owo pray/curse remind set to ${!user.owoSettings.prayRemind}")
            }

            1 -> {
                when (args.first().toLowerCase()) {
                    "true" -> {
                        col.updateOne(
                            HakiUser::_id eq user._id,
                            setValue(HakiUser::owoSettings / OWOSettings::prayRemind, true)
                        )
                        sendMessage(mCE.message.channel, "owo pray/curse remind set to true")
                    }

                    "false" -> {
                        col.updateOne(
                            HakiUser::_id eq user._id,
                            setValue(HakiUser::owoSettings / OWOSettings::prayRemind, false)
                        )
                        sendMessage(mCE.message.channel, "owo pray/curse remind set to false")
                    }
                    else -> {
                        sendMessage(mCE.message.channel, "invalid pray/curse setting, can only be true or false")
                    }
                }
            }
            else -> {
                sendMessage(mCE.message.channel, "invalid pray/curse setting, can only be true or false")
            }
        }
    }
}