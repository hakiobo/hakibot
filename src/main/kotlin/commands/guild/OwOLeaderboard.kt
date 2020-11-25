package commands.guild

import HakiUser
import Hakibot
import UserGuildOwOCount
import UserGuildOwOCount.Companion.normalizeGuild
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.BotCommand
import commands.utils.CommandCategory
import org.litote.kmongo.*
import java.awt.Color
import kotlin.reflect.KProperty1

object OwOLeaderboard : BotCommand {
    override val name: String
        get() = "owoleaderboard"
    override val aliases: List<String>
        get() = listOf("owotop", "otop", "owoldb", "ldb", "top", "leaderboard")
    override val description: String
        get() = "Show the owo leaderboard for this server"
    override val category: CommandCategory
        get() = CommandCategory.GUILD

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (args.size <= 2) {
            var type = RankingType.TOTAL
            var size = 5
            var typeSet = false
            var sizeSet = false
            var valid = true
            for (a in args) {
                val arg = a.toLowerCase()
                if (arg.toIntOrNull() != null) {
                    if (sizeSet) {
                        valid = false
                    } else {
                        sizeSet = true
                        size = arg.toInt()
                    }
                } else if (typeSet) {
                    valid = false
                } else {
                    for (t in RankingType.values()) {
                        if (arg in t.triggers) {
                            type = t
                            typeSet = true
                            break
                        }
                    }
                    if (!typeSet) valid = false
                }
            }
            if (valid) {
                val guild = getGuildInfo(mCE.guildId!!)
                normalizeGuild(mCE, guild)


                val userCol = db.getCollection<HakiUser>("users")
                size = size.coerceAtLeast(3).coerceAtMost(15)
                val col = db.getCollection<UserGuildOwOCount>("owo-count")
                val result = col.aggregate<UserGuildOwOCount>(
                        match(UserGuildOwOCount::guild eq mCE.guildId!!.longValue),
                        sort(descending(type.stat)),
                        limit(size)
                ).toList()
                mCE.message.channel.createEmbed {
                    color = Color(0xABCDEF)
                    title = "${type.desc}OwO Leaderboard for ${mCE.getGuild()?.name ?: "No Name????"}"
                    for (x in result.indices) {
                        val res = result[x]
                        val username = getUserFromDB(Snowflake(res.user), col = userCol).username!!

                        field {
                            name = "#${x + 1}: $username"
                            value = "${type.stat.get(res)} OwOs"
                        }
                    }
                }
            } else {
                sendMessage(mCE.message.channel, "Invalid Format :(", 5_000)
            }
        } else {
            sendMessage(mCE.message.channel, "Invalid Format :(", 5_000)
        }

    }

    enum class RankingType(val stat: KProperty1<UserGuildOwOCount, Int>, val triggers: List<String>, val desc: String) {
        TOTAL(UserGuildOwOCount::owoCount, listOf("all", "total"), ""),
        YEAR(UserGuildOwOCount::yearlyCount, listOf("year", "yearly"), "Yearly "),
        LAST_YEAR(UserGuildOwOCount::lastYearCount, listOf("lastyear", "prevyear", "ly", "py"), "Last Year's "),
        MONTH(UserGuildOwOCount::monthlyCount, listOf("month", "m", "monthly"), "Monthly "),
        LAST_MONTH(UserGuildOwOCount::lastMonthCount, listOf("lastmonth", "prevmonth", "lm", "pm"), "Last Month's "),
        WEEK(UserGuildOwOCount::weeklyCount, listOf("week", "w", "weekly"), "Weekly "),
        LAST_WEEK(UserGuildOwOCount::lastWeekCount, listOf("lastweek", "prevweek", "lw", "pw"), "Last Week's "),
        DAY(UserGuildOwOCount::dailyCount, listOf("t", "today", "d", "day", "daily"), "Today's "),
        YESTERDAY(UserGuildOwOCount::yesterdayCount, listOf("y", "yesterday", "yes", "yday", "pday", "prevday", "lday", "lastday"), "Yesterday's "),
    }
}

