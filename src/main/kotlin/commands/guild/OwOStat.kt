package commands.guild

import Hakibot
import entities.UserGuildOwOCount
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.*
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import toInstant
import java.awt.Color

object OwOStat : BotCommand {
    override val name: String
        get() = "owostat"
    override val aliases: List<String>
        get() = listOf("owos", "ostat", "stat")
    override val category: CommandCategory
        get() = CommandCategory.GUILD
    override val description: String
        get() = "Gets a user's owo count stats for this server"

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(listOf(), "Gets your OwO stats in this server"),
            CommandUsage(
                listOf(
                    Argument(listOf("userId", "userMention"), ChoiceType.DESCRIPTION)
                ),
                "Gets the OwO stats for the given user in this server"
            ),
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        when (args.size) {
            0 -> {
                displayOwOStats(mCE, mCE.member!!.id.longValue)
            }
            1 -> {
                val userId = getUserIdFromString(args.first())
                if (userId == null) {
                    sendMessage(mCE.message.channel, "That's not a user", 5_000)
                } else {
                    displayOwOStats(mCE, userId)
                }
            }
            else -> {
                sendMessage(mCE.message.channel, "Invalid Format, expecting h!owostat <userId|mention>", 5_000)
            }
        }
    }

    private suspend fun Hakibot.displayOwOStats(mCE: MessageCreateEvent, userId: Long) {
        val query = db.getCollection<UserGuildOwOCount>("owo-count")
            .findOne { UserGuildOwOCount::_id eq "$userId|${mCE.guildId!!.value}" }
        if (query == null) {
            sendMessage(mCE.message.channel, "Could not find any OwO's for that user in this server", 10_000)
        } else {
            val now = mCE.message.id.toInstant().atZone(Hakibot.PST)
            query.normalize(mCE)
            val username = getUserFromDB(Snowflake(query.user)).username!!
            mCE.message.channel.createEmbed {
                title = "$username's OwOs in ${mCE.getGuild()!!.name}"
                description = "__**Total**__: ${query.owoCount}\n"
                field {
                    name = "Current Stats"
                    value = "__Today__: ${query.dailyCount}\n" +
                            "__This Week__: ${query.weeklyCount}\n" +
                            "__${now.month.name.toLowerCase().capitalize()}__: ${query.monthlyCount}\n" +
                            "__${now.year}__: ${query.yearlyCount}"
                }
                field {
                    name = "Past Stats"
                    value = "__Yesterday__: ${query.yesterdayCount}\n" +
                            "__Last Week__: ${query.lastWeekCount}\n" +
                            "__${now.minusMonths(1).month.name.toLowerCase().capitalize()}__: ${query.lastMonthCount}\n" +
                            "__${now.year - 1}__: ${query.lastYearCount}"
                }
                color = Color(0xABCDEF)
            }
        }
    }
}