package entities

import Hakibot
import com.mongodb.client.model.UpdateOptions
import dev.kord.core.event.message.MessageCreateEvent
import org.litote.kmongo.*
import toInstant
import java.time.*

data class UserGuildOwOCount(
    val _id: String,
    val user: Long,
    val guild: Long,
    var owoCount: Int = 0,
    var dailyCount: Int = 0,
    var yesterdayCount: Int = 0,
    var weeklyCount: Int = 0,
    var lastWeekCount: Int = 0,
    var monthlyCount: Int = 0,
    var lastMonthCount: Int = 0,
    var yearlyCount: Int = 0,
    var lastYearCount: Int = 0,
    var lastOWO: Long = 0,
) {

    fun normalize(mCE: MessageCreateEvent): Boolean {
        val curTime = mCE.message.id.toInstant().atZone(ZoneId.of("PST", ZoneId.SHORT_IDS)).toLocalDate()
        val oldTime = Instant.ofEpochMilli(lastOWO).atZone(ZoneId.of("PST", ZoneId.SHORT_IDS)).toLocalDate()

        when (curTime.year - oldTime.year) {
            0 -> {
                when (curTime.monthValue - oldTime.monthValue) {
                    0 -> {
                    }
                    1 -> {
                        lastMonthCount = monthlyCount
                        monthlyCount = 0
                    }
                    else -> {
                        lastMonthCount = 0
                        monthlyCount = 0
                    }
                }
                when (curTime.dayOfYear - oldTime.dayOfYear) {
                    0 -> {
                        return false
                    }
                    1 -> {
                        yesterdayCount = dailyCount
                        dailyCount = 0
                    }
                    else -> {
                        yesterdayCount = 0
                        dailyCount = 0
                    }
                }

                //weekly stuff
                val difDoW = curTime.dayOfWeek.value % 7 - oldTime.dayOfWeek.value % 7
                val dif = curTime.dayOfYear - oldTime.dayOfYear
                if (dif == difDoW + 7) {
                    lastWeekCount = weeklyCount
                    weeklyCount = 0
                } else if (dif != difDoW) {
                    lastWeekCount = 0
                    weeklyCount = 0
                }
            }
            1 -> {
                lastYearCount = yearlyCount
                lastMonthCount = if (curTime.monthValue == 1 && oldTime.monthValue == 12) {
                    yesterdayCount = if (curTime.dayOfMonth == 1 && oldTime.dayOfMonth == 31) {
                        dailyCount
                    } else {
                        0
                    }
                    monthlyCount
                } else {
                    yesterdayCount = 0
                    0
                }
                yearlyCount = 0
                monthlyCount = 0
                dailyCount = 0

                //weekly stuff
                val day2 = curTime.dayOfYear + 365 + if (Year.isLeap(oldTime.year.toLong())) 1 else 0
                val difDoW = curTime.dayOfWeek.value % 7 - oldTime.dayOfWeek.value % 7
                val dif = day2 - oldTime.dayOfYear
                if (dif == difDoW + 7) {
                    lastWeekCount = weeklyCount
                    weeklyCount = 0
                } else if (dif != difDoW) {
                    lastWeekCount = 0
                    weeklyCount = 0
                }

            }
            else -> {
                dailyCount = 0
                yesterdayCount = 0
                weeklyCount = 0
                lastWeekCount = 0
                monthlyCount = 0
                lastMonthCount = 0
                yearlyCount = 0
                lastYearCount = 0
            }
        }
        return true
    }

    companion object {
        private const val OWO_CD = 10;
        const val DB_NAME = "owo-count"


//        fun Hakibot.normalizeGuild(mCE: MessageCreateEvent, guild: HakiGuild) {
//            val curDate = mCE.message.id.toInstant().atZone(ZoneId.of("PST", ZoneId.SHORT_IDS)).toLocalDate()
//            val prevDate =
//                Instant.ofEpochMilli(guild.lastOwONormalize).atZone(ZoneId.of("PST", ZoneId.SHORT_IDS)).toLocalDate()
//            if (curDate != prevDate) {
//                db.getCollection<HakiGuild>("guilds").updateOne(
//                    HakiGuild::_id eq guild._id,
//                    setValue(HakiGuild::lastOwONormalize, mCE.message.id.toInstant().toEpochMilli())
//                )
//                val col = db.getCollection<UserGuildOwOCount>("owo-count")
//                val query = col.find(UserGuildOwOCount::guild eq guild._id.toLong())
//                val dayStart = curDate.atStartOfDay(ZoneId.of("PST", ZoneId.SHORT_IDS)).toInstant().toEpochMilli()
//                query.forEach {
//                    if (it.normalize(mCE)) {
//                        it.lastOWO = dayStart
//                        col.replaceOne(HakiGuild::_id eq it._id, it)
//                    }
//                }
//            }
//        }

        suspend fun Hakibot.countOwO(mCE: MessageCreateEvent, user: HakiUser, guild: HakiGuild) {
            if (!guild.settings.owoCountingEnabled) return
            val newInstant = mCE.message.id.toInstant()
            val duration = Duration.between(Instant.ofEpochMilli(user.owoCount.lastOwO), newInstant).seconds
            if (duration < OWO_CD) return
            if (guild._id.toLong() == Hakibot.LXV_SERVER && newInstant.atZone(Hakibot.PST).toLocalDate().run {
                    year == 2021 && month == Month.MARCH && dayOfMonth == 6
                } && duration < 13) return
            val col = db.getCollection<UserGuildOwOCount>("owo-count")
            val id = "${user._id}|${guild._id}"
            val entry = col.findOne(UserGuildOwOCount::_id eq id)
                ?: UserGuildOwOCount(id, user._id.toLong(), guild._id.toLong())
            val newTimeMS = newInstant.toEpochMilli()
            db.getCollection<HakiUser>("users").updateOne(
                HakiUser::_id eq user._id,
                setValue(HakiUser::owoCount, UserOWOCount(user.owoCount.count + 1, newTimeMS))
            )

            entry.normalize(mCE)

            entry.lastOWO = newTimeMS
            entry.owoCount++
            entry.yearlyCount++
            entry.monthlyCount++
            entry.weeklyCount++
            entry.dailyCount++
            col.updateOne(UserGuildOwOCount::_id eq entry._id, entry, UpdateOptions().upsert(true))
        }
    }

}

val owoCommands = hashSetOf(
    "ab", "acceptbattle",
    "battle", "b", "fight",
    "battlesetting", "bs", "battlesettings",
    "crate", "weaponcrate", "wc",
    "db", "declinebattle",
    "pets", "pet",
    "rename",
    "team", "squad",
    "teams", "setteam", "squads", "useteams",
    "weapon", "w", "weapons", "wep",
    "weaponshard", "ws", "weaponshards", "dismantle",
    "claim", "reward", "compensation",
    "cowoncy", "money", "currency", "cash", "credit", "balance",
    "daily",
    "give", "send",
    "quest",
    "gif", "pic",
    "blush", "cry", "dance", "lewd", "pout", "shrug", "sleepy", "smile", "smug", "thumbsup", "wag", "thinking",
    "triggered", "teehee", "deredere", "thonking", "scoff", "happy", "thumbs", "grin",
    "cuddle", "hug", "kiss", "lick", "nom", "pat", "poke", "slap", "stare", "highfive", "bite", "greet", "punch",
    "handholding", "tickle", "kill", "hold", "pats", "wave", "boop", "snuggle", "fuck", "sex",
    "blackjack", "bj", "21",
    "coinflip", "cf", "coin", "flip",
    "drop", "pickup",
    "lottery", "bet", "lotto",
    "slots", "slot", "s",
    "communism", "communismcat",
    "distractedbf", "distracted",
    "drake",
    "eject", "amongus",
    "emergency", "emergencymeeting",
    "headpat",
    "isthisa",
    "slapcar", "slaproof",
    "spongebobchicken", "schicken",
    "bully", "pika", "pikapika",
    "alastor", "army", "gauntlet", "piku",
    "bunny", "cake", "java", "crown", "cpc", "dish", "donut", "icecream", "lollipop", "meshi", "milk",
    "pizza", "poutine", "rose", "bouquet", "rum", "sharingan", "slime", "teddy", "yy",
    "coffee", "cupachicake", "yinyang",
    "tarot",
    "bell", "strengthtest",
    "roll", "d20",
    "choose", "pick", "decide",
    "my", "me", "guild",
    "top", "rank", "ranking",
    "buy",
    "describe", "desc",
    "equip", "use",
    "inventory", "inv",
    "shop", "market",
    "acceptmarriage", "am",
    "cookie", "rep",
    "declinemarriage", "dm",
    "define",
    "divorce",
    "eightball", "8b", "ask", "8ball",
    "emoji", "enlarge", "jumbo",
    "level", "lvl", "levels", "xp",
    "propose", "marry", "marriage", "wife", "husband",
    "owo", "owoify", "ify",
    "pray", "curse",
    "profile",
    "ship", "combine",
    "translate", "listlang",
    "wallpaper", "wp", "wallpapers", "background", "backgrounds",
    "announce", "changelog", "announcement", "announcements",
    "avatar", "user",
    "censor",
    "checklist", "task", "tasks", "cl",
    "color", "randcolor", "colour", "randcolour",
    "covid", "cv", "covid19", "coronavirus",
    "disable",
    "enable",
    "feedback", "question", "report", "suggest",
    "guildlink",
    "help",
    "invite", "link",
    "math", "calc", "calculate",
    "merch",
    "patreon", "donate",
    "ping", "pong",
    "prefix",
    "rule", "rules",
    "shards", "shard",
    "stats", "stat", "info",
    "uncensor",
    "vote",
    "autohunt", "huntbot", "hb",
    "hunt", "h", "catch",
    "lootbox", "lb",
    "owodex", "od", "dex", "d",
    "sacrifice", "essence", "butcher", "sac", "sc",
    "sell",
    "upgrade", "upg",
    "zoo",
)