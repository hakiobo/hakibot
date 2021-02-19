package commands.guild

import Hakibot
import entities.UserGuildOwOCount
import commands.utils.BotCommand
import commands.utils.CommandCategory
import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.event.message.MessageCreateEvent
import entities.HakiUser
import org.litote.kmongo.*
import toInstant
import java.time.Duration
import java.time.Instant
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
                val userCol = db.getCollection<HakiUser>("users")
                size = size.coerceAtLeast(3).coerceAtMost(25)
                val result =
                    type.interval.getIdDataPairs(this, mCE, type.unit, size)

                val filters = List(result.size) {
                    HakiUser::_id eq result[it].first.toString()
                }

                val names = userCol.find(or(filters)).toList()
                mCE.message.channel.createEmbed {
                    color = Color(0xABCDEF)
                    title = "${type.desc}OwO Leaderboard for ${mCE.getGuild()?.name ?: "No Name????"}"
                    if (type == RankingType.GLOBAL) {
                        title = "${type.desc}OwO Leaderboard"
                    }
                    for (x in result.indices) {
                        val res = result[x]
                        val username =
                            names.find { it._id == res.first.toString() }?.username ?: "Deleted User ${res.first}"
                        field {
                            name = "#${x + 1}: $username"
                            value = "${res.second} OwOs"
                        }
                    }
                    val timeLeft = type.unit.untilEndOfCurrent(mCE.message.id)
                    if (timeLeft != null) {
                        footer {
                            val d = timeLeft.toDays()
                            val h = timeLeft.toHours() % 24
                            val m = timeLeft.toMinutes() % 60
                            val s = timeLeft.seconds % 60

                            text = "${type.interval.note} ${d}D ${h}H ${m}M ${s}S"
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

    fun getYearStart(id: Snowflake): Instant =
        id.toInstant().atZone(Hakibot.PST).toLocalDate().withDayOfYear(1).atStartOfDay(Hakibot.PST).toInstant()

    fun getPrevYearStart(id: Snowflake): Instant =
        id.toInstant().atZone(Hakibot.PST).toLocalDate().minusYears(1L).withDayOfYear(1).atStartOfDay(Hakibot.PST)
            .toInstant()

    fun getMonthStart(id: Snowflake): Instant =
        id.toInstant().atZone(Hakibot.PST).toLocalDate().withDayOfMonth(1).atStartOfDay(Hakibot.PST).toInstant()

    fun getPrevMonthStart(id: Snowflake): Instant =
        id.toInstant().atZone(Hakibot.PST).toLocalDate().withDayOfMonth(1).minusMonths(1).atStartOfDay(Hakibot.PST)
            .toInstant()

    fun getWeekStart(id: Snowflake): Instant {
        val time = id.toInstant().atZone(Hakibot.PST).toLocalDate()
        return time.minusDays(time.dayOfWeek.value % 7L).atStartOfDay(Hakibot.PST).toInstant()
    }

    fun getPrevWeekStart(id: Snowflake): Instant {
        val time = id.toInstant().atZone(Hakibot.PST).toLocalDate()
        return time.minusDays((time.dayOfWeek.value % 7L)).minusWeeks(1).atStartOfDay(Hakibot.PST).toInstant()
    }

    fun getTodayStart(id: Snowflake): Instant =
        id.toInstant().atZone(Hakibot.PST).toLocalDate().atStartOfDay(Hakibot.PST).toInstant()

    fun getYesterdayStart(id: Snowflake): Instant =
        id.toInstant().atZone(Hakibot.PST).toLocalDate().minusDays(1).atStartOfDay(Hakibot.PST).toInstant()


    private enum class TimeUnit(
        val untilEndOfCurrent: (Snowflake) -> Duration?,
        val start: (Snowflake) -> Instant,
        val prevStart: (Snowflake) -> Instant,
        val curStat: KProperty1<UserGuildOwOCount, Int>,
        val prevStat: KProperty1<UserGuildOwOCount, Int>,
    ) {
        TOTAL({ null }, { Instant.MIN }, { Instant.MIN }, UserGuildOwOCount::owoCount, UserGuildOwOCount::owoCount),
        YEAR(
            OwOLeaderboard::toEndOfYear,
            OwOLeaderboard::getYearStart,
            OwOLeaderboard::getPrevYearStart,
            UserGuildOwOCount::yearlyCount,
            UserGuildOwOCount::lastYearCount,
        ),
        MONTH(
            OwOLeaderboard::toEndOfMonth,
            OwOLeaderboard::getMonthStart,
            OwOLeaderboard::getPrevMonthStart,
            UserGuildOwOCount::monthlyCount,
            UserGuildOwOCount::lastMonthCount,
        ),
        WEEK(
            OwOLeaderboard::toEndOfWeek,
            OwOLeaderboard::getWeekStart,
            OwOLeaderboard::getPrevWeekStart,
            UserGuildOwOCount::weeklyCount,
            UserGuildOwOCount::lastWeekCount,
        ),
        DAY(
            OwOLeaderboard::toEndOfDay,
            OwOLeaderboard::getTodayStart,
            OwOLeaderboard::getYesterdayStart,
            UserGuildOwOCount::dailyCount,
            UserGuildOwOCount::yesterdayCount,
        ),

    }


    private fun getTotalIdDataPairs(
        bot: Hakibot,
        mCE: MessageCreateEvent,
        unit: TimeUnit,
        size: Int,
    ): List<Pair<Long, Int>> {
        return bot.db.getCollection<UserGuildOwOCount>("owo-count").aggregate<UserGuildOwOCount>(
            match(UserGuildOwOCount::guild eq mCE.guildId!!.value),
            sort(descending(unit.curStat)),
            limit(size)
        ).toList().map { Pair(it.user, unit.curStat.get(it)) }
    }

    private fun getCurrentIdDataPairs(
        bot: Hakibot,
        mCE: MessageCreateEvent,
        unit: TimeUnit,
        size: Int,
    ): List<Pair<Long, Int>> {
        return bot.db.getCollection<UserGuildOwOCount>("owo-count").aggregate<UserGuildOwOCount>(
            match(UserGuildOwOCount::guild eq mCE.guildId!!.value),
            match(UserGuildOwOCount::lastOWO gte unit.start(mCE.message.id).toEpochMilli()),
            sort(descending(unit.curStat)),
            limit(size)
        ).toList().map { Pair(it.user, unit.curStat.get(it)) }
    }

    private fun getPreviousIdDataPairs(
        bot: Hakibot,
        mCE: MessageCreateEvent,
        unit: TimeUnit,
        size: Int,
    ): List<Pair<Long, Int>> {
        val start = unit.start(mCE.message.id).toEpochMilli()
        val prevStart = unit.prevStart(mCE.message.id).toEpochMilli()
        val col = bot.db.getCollection<UserGuildOwOCount>("owo-count")
        return col.aggregate<UserGuildOwOCount>(
            match(UserGuildOwOCount::guild eq mCE.guildId!!.value),
            match(UserGuildOwOCount::lastOWO gte start),
            sort(descending(unit.prevStat)),
            limit(size)
        ).toList().map { Pair(it.user, unit.prevStat.get(it)) }
            .union(
                col.aggregate<UserGuildOwOCount>(
                    match(UserGuildOwOCount::guild eq mCE.guildId!!.value),
                    match(UserGuildOwOCount::lastOWO gte prevStart),
                    match(UserGuildOwOCount::lastOWO lt start),
                    sort(descending(unit.curStat)),
                    limit(size)
                ).toList().map { Pair(it.user, unit.curStat.get(it)) }
            ).sortedByDescending { it.second }.take(size)
    }

    private fun getGlobalIdPairs(
        bot: Hakibot,
        mCE: MessageCreateEvent,
        unit: TimeUnit,
        size: Int,
    ): List<Pair<Long, Int>> {
        return bot.db.getCollection<HakiUser>("users").aggregate<HakiUser>(
            sort(descending(HakiUser::owoCount)),
            limit(size)
        ).toList().map { Pair(it._id.toLong(), it.owoCount.count) }
    }

    private enum class IntervalType(
        val note: String?,
        val getIdDataPairs: (Hakibot, MessageCreateEvent, TimeUnit, Int) -> List<Pair<Long, Int>>
    ) {
        GLOBAL(null, OwOLeaderboard::getGlobalIdPairs),
        TOTAL(null, OwOLeaderboard::getTotalIdDataPairs),
        CURRENT("Resets in", OwOLeaderboard::getCurrentIdDataPairs),
        PREVIOUS("Viewable for", OwOLeaderboard::getPreviousIdDataPairs),
    }

    private enum class RankingType(
        val triggers: List<String>,
        val desc: String,
        val interval: IntervalType,
        val unit: TimeUnit,
    ) {
        GLOBAL(listOf("g", "global"), "Global ", IntervalType.GLOBAL, TimeUnit.TOTAL),
        TOTAL(listOf("all", "total"), "", IntervalType.TOTAL, TimeUnit.TOTAL),
        YEAR(listOf("year", "yearly"), "Yearly ", IntervalType.CURRENT, TimeUnit.YEAR),
        LAST_YEAR(listOf("lastyear", "prevyear", "ly", "py"), "Last Year's ", IntervalType.PREVIOUS, TimeUnit.YEAR),
        MONTH(listOf("month", "m", "monthly"), "Monthly ", IntervalType.CURRENT, TimeUnit.MONTH),
        LAST_MONTH(
            listOf("lastmonth", "prevmonth", "lm", "pm"),
            "Last Month's ",
            IntervalType.PREVIOUS,
            TimeUnit.MONTH
        ),
        WEEK(listOf("week", "w", "weekly"), "Weekly ", IntervalType.CURRENT, TimeUnit.WEEK),
        LAST_WEEK(listOf("lastweek", "prevweek", "lw", "pw"), "Last Week's ", IntervalType.PREVIOUS, TimeUnit.WEEK),
        DAY(
            listOf("t", "today", "d", "day", "daily"),
            "Today's ",
            IntervalType.CURRENT,
            TimeUnit.DAY
        ),
        YESTERDAY(
            listOf("y", "yesterday", "yes", "yday", "pday", "prevday", "lday", "lastday"),
            "Yesterday's ",
            IntervalType.PREVIOUS,
            TimeUnit.DAY
        ),
    }
}

