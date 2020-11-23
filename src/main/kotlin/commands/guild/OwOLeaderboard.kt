package commands.guild

import Hakibot
import UserGuildOwOCount
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.BotCommand
import commands.utils.CommandCategory
import org.litote.kmongo.*
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
            for (arg in args) {
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
                size = size.coerceAtLeast(3).coerceAtMost(5)
                val col = db.getCollection<UserGuildOwOCount>("owo-count")
                val result = col.aggregate<UserGuildOwOCount>(
                        match(UserGuildOwOCount::guild eq mCE.guildId!!.longValue),
                        sort(descending(type.stat)),
                        limit(size)
                ).toList()
                mCE.message.channel.createEmbed {
                    title = "${type.desc} OwO Leaderboard for ${mCE.getGuild()?.name ?: "No Name????"}"
                    for (x in result.indices) {
                        val res = result[x]
                        field {
                            name = "#${x + 1}: ${client.getUser(Snowflake(res.user))?.username ?: "Deleted User#${res.user}"}"
                            value =  "${type.stat.get(res)} OwOs"
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
        YEAR(UserGuildOwOCount::yearlyCount, listOf("year", "y", "yearly"), "Yearly"),
        MONTH(UserGuildOwOCount::monthlyCount, listOf("month", "m", "monthly"), "Monthly"),
        WEEK(UserGuildOwOCount::weeklyCount, listOf("week", "w", "weekly"), "Weekly"),
        DAY(UserGuildOwOCount::dailyCount, listOf("today", "d", "day", "daily"), "Today's "),
    }
}

