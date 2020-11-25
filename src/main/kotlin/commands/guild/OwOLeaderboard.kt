package commands.guild

import entities.HakiUser
import Hakibot
import entities.UserGuildOwOCount
import entities.UserGuildOwOCount.Companion.normalizeGuild
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.BotCommand
import commands.utils.CommandCategory
import org.litote.kmongo.*
import toInstant
import java.awt.Color
import java.time.Duration
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
                    val timeLeft = type.untilReset(mCE.message.id)
                    if (timeLeft != null) {
                        footer {
                            val d = timeLeft.toDays()
                            val h = timeLeft.toHours() % 24
                            val m = timeLeft.toMinutes() % 60
                            val s = timeLeft.seconds % 60

                            text = "${type.timeNote} ${d}D ${h}H ${m}M ${s}S"
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

    fun toEndOfWeek(id: Snowflake): Duration? {
        val time = id.toInstant().atZone(Hakibot.PST)
        val endTime = time.toLocalDate().plusDays(7L - (time.dayOfWeek.value % 7)).atStartOfDay(Hakibot.PST)

        return Duration.between(time, endTime)
    }

    fun toEndOfDay(id: Snowflake): Duration? {
        val time = id.toInstant().atZone(Hakibot.PST)
        val endTime = time.toLocalDate().plusDays(1).atStartOfDay(Hakibot.PST)
        return Duration.between(time, endTime)
    }

    fun toEndOfMonth(id: Snowflake): Duration? {
        val time = id.toInstant().atZone(Hakibot.PST)
        val endTime = time.toLocalDate().plusMonths(1).withDayOfMonth(1).atStartOfDay(Hakibot.PST)

        return Duration.between(time, endTime)
    }

    fun toEndOfYear(id: Snowflake): Duration? {
        val time = id.toInstant().atZone(Hakibot.PST)
        val endTime = time.toLocalDate().plusYears(1).withDayOfYear(1).atStartOfDay(Hakibot.PST)

        return Duration.between(time, endTime)
    }

    enum class RankingType(val stat: KProperty1<UserGuildOwOCount, Int>, val triggers: List<String>, val desc: String, val untilReset: (Snowflake) -> Duration?, val timeNote: String) {
        TOTAL(UserGuildOwOCount::owoCount, listOf("all", "total"), "", { null }, ""),
        YEAR(UserGuildOwOCount::yearlyCount, listOf("year", "yearly"), "Yearly ", OwOLeaderboard::toEndOfYear, "Resets in"),
        LAST_YEAR(UserGuildOwOCount::lastYearCount, listOf("lastyear", "prevyear", "ly", "py"), "Last Year's ", OwOLeaderboard::toEndOfYear, "Viewable for"),
        MONTH(UserGuildOwOCount::monthlyCount, listOf("month", "m", "monthly"), "Monthly ", OwOLeaderboard::toEndOfMonth, "Resets in"),
        LAST_MONTH(UserGuildOwOCount::lastMonthCount, listOf("lastmonth", "prevmonth", "lm", "pm"), "Last Month's ", OwOLeaderboard::toEndOfMonth, "Viewable for"),
        WEEK(UserGuildOwOCount::weeklyCount, listOf("week", "w", "weekly"), "Weekly ", OwOLeaderboard::toEndOfWeek, "Resets in"),
        LAST_WEEK(UserGuildOwOCount::lastWeekCount, listOf("lastweek", "prevweek", "lw", "pw"), "Last Week's ", OwOLeaderboard::toEndOfWeek, "Viewable for"),
        DAY(UserGuildOwOCount::dailyCount, listOf("t", "today", "d", "day", "daily"), "Today's ", OwOLeaderboard::toEndOfDay, "Resets in"),
        YESTERDAY(UserGuildOwOCount::yesterdayCount, listOf("y", "yesterday", "yes", "yday", "pday", "prevday", "lday", "lastday"), "Yesterday's ", OwOLeaderboard::toEndOfDay, "Viewable for"),
    }
}

