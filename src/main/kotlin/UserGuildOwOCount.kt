import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.UpdateOptions
import org.litote.kmongo.*
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

data class UserGuildOwOCount(
        val _id: String,
        val user: Long,
        val guild: Long,
        var owoCount: Int = 0,
        var dailyCount: Int = 0,
        var weeklyCount: Int = 0,
        var monthlyCount: Int = 0,
        var yearlyCount: Int = 0,
        var lastOWO: Long = 0,
) {

    companion object {
        private const val OWO_CD = 10;

        fun Hakibot.countOwO(mCE: MessageCreateEvent, user: HakiUser, guild: HakiGuild) {
            if (guild._id != Hakibot.LXV_SERVER.toString()) return
            val newInstant = mCE.message.id.toInstant()
            if (Duration.between(Snowflake(user.owoCount.lastOwO).toInstant(), newInstant).seconds < OWO_CD) return
            val col = db.getCollection<UserGuildOwOCount>("owo-count")
            val id = "${user._id}|${guild._id}"
            val entry = col.findOne(UserGuildOwOCount::_id eq id)
                    ?: UserGuildOwOCount(id, user._id.toLong(), guild._id.toLong())
            val newTime = newInstant.atZone(ZoneId.of("PST", ZoneId.SHORT_IDS))!!
            val oldTime = Instant.ofEpochMilli(entry.lastOWO).atZone(ZoneId.of("PST", ZoneId.SHORT_IDS))!!
            val newTimeMS = newTime.toInstant().toEpochMilli()
            db.getCollection<HakiUser>("users").updateOne(HakiUser::_id eq user._id, setValue(HakiUser::owoCount, UserOWOCount(user.owoCount.count + 1, newTimeMS)))

            entry.lastOWO = newTimeMS
            entry.owoCount++
            entry.yearlyCount++
            entry.monthlyCount++
            entry.weeklyCount++
            entry.dailyCount++


            if (newTime.year != oldTime.year) {
                entry.dailyCount = 1
                entry.weeklyCount = 1
                entry.monthlyCount = 1
                entry.yearlyCount = 1
            } else if (newTime.month != oldTime.month) {
                entry.dailyCount = 1
                entry.weeklyCount = 1
                entry.monthlyCount = 1
            } else if (newTime.dayOfWeek.value - oldTime.dayOfWeek.value != newTime.dayOfMonth - oldTime.dayOfMonth) {
                entry.dailyCount = 1
                entry.weeklyCount = 1
            } else if (newTime.dayOfMonth != oldTime.dayOfMonth) {
                entry.dailyCount = 1
            }
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
        "alastor", "army", "bully", "bunny", "cake", "coffee", "java", "crown", "cupachicake", "cpc", "dish", "donut",
        "gauntlet", "icecream", "lollipop", "meshi", "milk", "pika", "pikapika", "piku", "pizza", "poutine",
        "rose", "bouquet", "rum", "sharingan", "slime", "teddy", "yinyang", "yy",
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