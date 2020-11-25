import entities.UserGuildOwOCount.Companion.countOwO
import com.gitlab.kordlib.common.entity.DiscordMessage
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.behavior.MessageBehavior
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.Embed
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.event.gateway.ReadyEvent
import com.gitlab.kordlib.core.event.guild.GuildCreateEvent
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.gitlab.kordlib.core.event.message.MessageUpdateEvent
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.core.on
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import com.gitlab.kordlib.rest.request.RestRequestException
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import commands.*
import commands.guild.*
import commands.meta.*
import commands.utils.BotCommand
import commands.utils.CommandCategory
import entities.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import java.lang.Exception
import java.time.Instant
import java.time.ZoneId
import java.util.regex.Pattern

class Hakibot(val client: Kord, val db: MongoDatabase) {


    private val triggers = mapOf(
            "haki" to "is coding god",
            "hika" to "god is coding",
            "anvi" to "sewen sewenty",
            "asono" to "waifu's inva", "asona" to "waifu's inva", "asano" to "waifu's inva",
            "nea" to "js in codes",
            "yez" to "doesn't code enough",
            "furo" to "Furory?",
            "shufi" to "your bow lol god down",
            "sellchart" to "5 - B\n2.5 - F\n2 - P CP H\n1.67 - M\n1.5 - L G D\n1.2 - S\n1 - C E\n0.6 - U\n0.5 - R",
            "licu" to "Should curse haki"
    )
    val commands = listOf(
            HelpCommand,
            ReportCommand,
            DMCommand,
            PrefixCommand,
            OWOPrefixCommand,
            OWOHuntSettingsCMD,
            OWOPraySettingsCommand,
            CPCommand,
            WhoIsCommand,
            SuggestCommand,
            SearchForCPCommand,
            InviteCommand,
            GuildsCommand,
            ServerCommand,
            LogoutCommand,
            TriggerCommand,
            WhenCommand,
            UseGlobalPrefixCommand,
            OwOStat,
            OwOLeaderboard,
    )

    suspend fun startUp() {
        val users = db.getCollection<HakiUser>("users")
        users.updateMany(
                HakiUser::owoSettings / OWOSettings::huntCD eq true,
                setValue(HakiUser::owoSettings / OWOSettings::huntCD, false)
        )
        users.updateMany(
                HakiUser::owoSettings / OWOSettings::prayCD eq true,
                setValue(HakiUser::owoSettings / OWOSettings::prayCD, false)
        )
        client.on<ReadyEvent> {
            messageChannelById(ONLINE_CHANNEL, "Online!")
        }
        client.on<ReactionAddEvent> {
            if (userId.longValue == OWO_ID && emoji == ReactionEmoji.Unicode("\u27a1\ufe0f") && !isResetTime()) {
                val embed = getMessage().embeds.firstOrNull()
                if (embed?.author?.name == "Today's Available Weapons") {
                    readShopWeapon(embed.description!!).forEach {
                        message.addReaction(it)
                    }
                }
            } else if (userId.longValue == HAKIOBO_ID && emoji == SuggestCommand.TRASH && getMessage().author?.id == client.selfId) {
                message.delete()
            }
        }
        client.on<MessageCreateEvent> {
            client.launch {
                handleMessage(this@on)
            }
        }
        client.on<MessageUpdateEvent> {
            if (new.author?.id == OWO_ID.toString() && !isResetTime()) {
                if (new.embeds?.firstOrNull()?.author?.name == "Today's Available Weapons") {
                    try {

                        val oldR = getMessage().reactions.filter { it.selfReacted }.map { it.emoji }
                        val newR = readShopWeapon(new.embeds?.first()?.description!!)

                        oldR.forEach {
                            message.deleteOwnReaction(it)
                        }
                        newR.forEach {
                            message.addReaction(it)
                        }
                    } catch (e: Exception) {
                        println("issue reading update ${message.id.value} in ${channel.mention}")
                    }
                }
            }
        }
        client.on<GuildCreateEvent> {
            val col = db.getCollection<HakiGuild>("guilds")
            if (col.findOne(HakiGuild::_id eq guild.id.value) == null) {
                col.insertOne(HakiGuild(guild.id.value))
            }
        }

        client.login {
            this.since = Instant.now()!!
            this.playing("h!help")
        }
    }

    suspend fun getUserFromDB(userID: Snowflake, u: User? = null, col: MongoCollection<HakiUser> = db.getCollection<HakiUser>("users")): HakiUser {
        val query = col.findOne(HakiUser::_id eq userID.value)
        return if (query == null) {
            val user = if (u == null) {
                HakiUser(userID.value, client.getUser(userID)?.username ?: "Deleted User#${userID.value}")
            } else {
                HakiUser(userID.value, u.username)
            }
            col.insertOne(user)
            user
        } else {
            if (u != null && u.username != query.username) {
                col.updateOne(HakiUser::_id eq query._id, setValue(HakiUser::username, u.username))
                query.copy(username = u.username)
            } else if (query.username == null) {
                val name = client.getUser(userID)?.username ?: "Deleted User#${userID.value}"
                col.updateOne(HakiUser::_id eq query._id, setValue(HakiUser::username, name))
                query.copy(username = name)
            } else {
                query
            }
        }
    }

    suspend fun handleMessage(mCE: MessageCreateEvent) {
        if (mCE.message.author?.id == client.selfId) return
        if (mCE.message.author == null) return
        if (mCE.message.author?.id?.longValue == OWO_ID) {
            val embed = mCE.message.embeds.firstOrNull()
            if (embed?.description?.startsWith("*Created by*") == true) {
                try {
                    handleOWODexEntry(mCE, embed)
                } catch (e: Exception) {
                    mCE.message.addReaction(ReactionEmoji.Unicode("\u274c"))
                    sendMessage(mCE.message.channel, "Could not parse CP stats", 10_000)
                }
            } else if (embed?.description?.startsWith("**Name:**") == true) {
                val desc = embed.description!!
                when (embed.author?.name?.split("'s ")?.last()) {
                    "Great Sword" -> sword(desc)
                    "Healing Staff" -> hStaff(desc)
                    "Bow" -> bow(desc)
                    "Rune of the Forgotten" -> rune(desc)
                    "Aegis" -> shield(desc)
                    "Orb of Potency" -> orb(desc)
                    "Vampiric Staff" -> vStaff(desc)
                    "Poison Dagger" -> dagger(desc)
                    "Wand of Absorption" -> wand(desc)
                    "Flame Staff" -> fStaff(desc)
                    "Energy Staff" -> eStaff(desc)
                    "Spirit Staff" -> sStaff(desc)
                    "Arcane Scepter" -> scepter(desc)
                    "Resurrection Staff" -> rStaff(desc)
                    "Glacial Axe" -> axe(desc)
                    "Banner" -> banner(desc)
                    "Culling Scythe" -> scythe(desc)
                    else -> emptyList()
                }.forEach {
                    mCE.message.addReaction(it)
                }
            }
        }
        if (mCE.message.author?.isBot == true) return



        if (mCE.guildId == null) return handleDM(mCE)

        val guild = getGuildInfo(mCE.guildId!!)
//        val user = getUserFromDB(mCE.message.author!!.id, mCE.message.author)


        if (guild.settings.enableWhen && mCE.message.content.filter(Char::isLetterOrDigit).takeLast(4)
                        .toLowerCase() == "when"
        ) {
            sendMessage(mCE.message.channel, "when", 10_000)
        }
        if (guild.settings.enableTriggers) {
            val triggerText = triggers[mCE.message.content.toLowerCase()]
            if (triggerText != null) {
                mCE.message.channel.createMessage(triggerText)
            }
        }


        val pre = guild.prefix
        if (guild.settings.allowGlobalPrefix && mCE.message.content.startsWith(GLOBAL_PREFIX, ignoreCase = true)) {
            handleCommand(mCE, mCE.message.content.drop(GLOBAL_PREFIX.length).trim())
        } else if (mCE.message.content.startsWith(pre, ignoreCase = true)) {
            handleCommand(mCE, mCE.message.content.drop(pre.length).trim())
        }
        val owoPre = guild.owoPrefix
        if (mCE.message.content.startsWith(GLOBAL_OWO_PREFIX, ignoreCase = true)) {
            handleOWOCommand(mCE, guild, mCE.message.content.drop(GLOBAL_OWO_PREFIX.length).trim(), getUserFromDB(mCE.message.author!!.id, mCE.message.author))
        } else if (mCE.message.content.startsWith(owoPre, ignoreCase = true)) {
            handleOWOCommand(mCE, guild, mCE.message.content.drop(owoPre.length).trim(), getUserFromDB(mCE.message.author!!.id, mCE.message.author))
        } else {
            if (mCE.message.content.contains("owo", true) || mCE.message.content.contains("uwu", true)) {
                countOwO(mCE, getUserFromDB(mCE.message.author!!.id, mCE.message.author), guild)
            }
        }
        if (mCE.message.mentionedUserIds.contains(client.selfId)) {
            if (!guild.settings.allowGlobalPrefix) {
                mCE.message.channel.createMessage("Global Bot prefix Disabled\nBot prefix is $pre")
            } else if (GLOBAL_PREFIX == pre) {
                mCE.message.channel.createMessage("Bot prefix is $pre")
            } else {
                mCE.message.channel.createMessage("Global bot prefix is $GLOBAL_PREFIX\nServer bot prefix is $pre")
            }
        }

    }

    fun getGuildInfo(
            guild: Snowflake,
            col: MongoCollection<HakiGuild> = db.getCollection<HakiGuild>("guilds")
    ): HakiGuild {
        return col.findOne(HakiGuild::_id eq guild.value) ?: HakiGuild(guild.value)
    }

    private suspend fun handleCommand(mCE: MessageCreateEvent, msg: String) {
        val split = msg.split(Pattern.compile("\\s+"))
        val userCMD = split.first().toLowerCase()
//        if (userCMD == "channel") {
//            println((client.getChannel(Snowflake(728474015181570070)) as GuildMessageChannel).getGuild().name)
//        }
        val args = split.drop(1)
        if (userCMD == "") return
        val cmd = lookupCMD(userCMD)
        if (cmd != null) {
            cmd.runCMD(this, mCE, args)
        } else {
            sendMessage(mCE.message.channel, "$userCMD is not a valid command", 5_000)
        }
    }

    internal fun lookupCMD(userCMD: String): BotCommand? {
        for (cmd in commands) {
            if (userCMD == cmd.name || userCMD in cmd.aliases) {
                return cmd
            }
        }
        return null
    }

    private suspend fun handleOWOCommand(mCE: MessageCreateEvent, guild: HakiGuild, msg: String, user: HakiUser) {
        val split = msg.split(Pattern.compile("\\s"))
        when (split.firstOrNull()) {
            "hunt", "h" -> owoHuntOWOCMD(mCE, user)
            "pray", "curse" -> owoPrayOWOCMD(mCE, split.first(), user)
            in owoCommands -> {
            }
            else -> countOwO(mCE, user, guild)
        }
    }

    private suspend fun owoHuntOWOCMD(mCE: MessageCreateEvent, user: HakiUser) {
        val authorID = mCE.message.author!!.id
        val col = db.getCollection<HakiUser>("users")
        if (user.owoSettings.huntRemind && !user.owoSettings.huntCD) {
            col.updateOne(
                    HakiUser::_id eq authorID.value,
                    setValue(HakiUser::owoSettings / OWOSettings::huntCD, true)
            )
            client.launch {
                delay(14500)
                col.updateOne(
                        HakiUser::_id eq authorID.value,
                        setValue(HakiUser::owoSettings / OWOSettings::huntCD, false)
                )
                sendMessage(mCE.message.channel, "${mCE.message.author!!.mention} hunt cooldown is done", 4500)
            }
        }
    }

    private suspend fun owoPrayOWOCMD(mCE: MessageCreateEvent, cmd: String, user: HakiUser) {
        val authorID = mCE.message.author!!.id
        val col = db.getCollection<HakiUser>("users")
        if (user.owoSettings.prayRemind && !user.owoSettings.prayCD) {
            col.updateOne(
                    HakiUser::_id eq authorID.value,
                    setValue(HakiUser::owoSettings / OWOSettings::prayCD, true)
            )
            if (cmd.startsWith("p")) {
                mCE.message.addReaction(PRAY_EMOJI)
            } else {
                mCE.message.addReaction(CURSE_EMOJI)
            }

            client.launch {
                delay(300_000)
//                val resp =
                col.updateOne(
                        HakiUser::_id eq authorID.value,
                        setValue(HakiUser::owoSettings / OWOSettings::prayCD, false)
                )
                mCE.message.channel.createMessage("${mCE.message.author!!.mention} pray/curse cooldown is done")

//                delay(10_000)
//                resp.delete()
            }
        }
    }

    private suspend fun dmUser(user: User, message: String) {
        val channel = try {
            user.getDmChannel()
        } catch (exception: RestRequestException) {
            if (user.id.longValue != HAKIOBO_ID) dmUser(HAKIOBO_ID, "Failed to get ${user.tag}'s DM's")
            return
        }
        try {
            channel.createMessage(message)
        } catch (exception: RestRequestException) {
            if (user.id.longValue != HAKIOBO_ID) dmUser(HAKIOBO_ID, "Failed to send DM to ${user.tag}")

        }
    }

    private suspend fun handleDM(mCE: MessageCreateEvent) {
        if (mCE.message.content.takeLast(4).toLowerCase() == "when") {
            sendMessage(mCE.message.channel, "when", 10_000)
        }

        val split = (if (mCE.message.content.startsWith("h!", ignoreCase = true)) mCE.message.content.drop(2)
                .trim() else mCE.message.content).split(Pattern.compile("\\s+"))
        val userCMD = split.first().toLowerCase()

        val args = split.drop(1)
        val cmd = lookupCMD(userCMD)
        if (cmd != null) {
            if (cmd.category == CommandCategory.GUILD) {
                mCE.message.channel.createMessage("Guild related commands not allowed in DMs")
            } else {
                cmd.runCMD(this, mCE, args)
            }
        } else {
            messageChannelById(DM_CHANNEL, "${mCE.message.author?.tag ?: "no author"}: ${mCE.message.content}")
        }

    }

//    internal suspend fun getHaki() = client.getUser(Snowflake(292483348738080769))!!

    internal fun getCPAdders() =
            arrayOf(
                    HAKIOBO_ID
//        , client.getUser(Snowflake( 304511726907293697))
            )

    internal suspend fun sendMessage(channel: MessageChannelBehavior, message: String, deleteAfterMS: Long = 0L) {
        client.launch {
            if (deleteAfterMS == 0L) {
                channel.createMessage(message)
            } else {
                val msg = channel.createMessage(message)
                delay(deleteAfterMS)
                msg.delete()
            }
        }
    }

    internal suspend fun dmUser(userID: Long, message: String) {
        val user = client.getUser(Snowflake(userID))
        if (user == null) {
            if (userID != HAKIOBO_ID) dmUser(HAKIOBO_ID, "Failed to find user $userID")
        } else {
            dmUser(user, message)
        }
    }

    internal suspend fun messageChannelById(channelId: Long, message: String, embed: EmbedBuilder? = null): DiscordMessage {
        return client.rest.channel.createMessage(channelId.toString()) {
            content = message
            this.embed = embed
        }
    }

    fun getUserIdFromString(s: String): Long? {
        return if (s.toLongOrNull() != null) {
            s.toLong()
        } else if (s.startsWith("<@") && s.endsWith(">")) {
            if (s[2] == '!') {
                s.drop(3).dropLast(1).toLongOrNull()
            } else {
                s.drop(2).dropLast(1).toLongOrNull()
            }
        } else {
            null
        }
    }

    private suspend fun handleOWODexEntry(mCE: MessageCreateEvent, embed: Embed) {
        val new = parseCP(mCE.message, embed)
        val name = new.name
        val col = db.getCollection<CustomPatreon>("cp")
        when (val search = col.findOne(CustomPatreon::name eq name)) {
            null -> {
                col.insertOne(new)
                messageChannelById(CP_ADD_CHANNEL, "added from ${mCE.message.getGuild().name}", new.toEmbed())
//                dmUser(HAKIOBO_ID, ")
                mCE.message.addReaction(ReactionEmoji.Unicode("\u2705"))
                sendMessage(mCE.message.channel, "added\n$new", 10_000)
            }
            new -> mCE.message.addReaction(ReactionEmoji.Unicode("\ud83d\udd01"))
            else -> {
                col.replaceOne(CustomPatreon::name eq name, new)
                messageChannelById(CP_UPD_CHANNEL, "OLD", search.toEmbed())
                messageChannelById(CP_UPD_CHANNEL, "NEW from ${mCE.message.getGuild().name}", new.toEmbed())

//                messageChannelById(CP_UPD_CHANNEL, "$search\n----->\n$new\nfrom ${mCE.message.getGuild().name}")
                mCE.message.addReaction(ReactionEmoji.Unicode("\ud83d\udd04"))
                sendMessage(
                        mCE.message.channel,
                        "Updated info for $name\nOld:\n$search\nNew:\n$new",
                        15_000
                )
                //                            col.updateOne(, setValue(entities.CustomPatreon::aliases, als))
            }
        }
    }

    internal fun parseCP(message: MessageBehavior, embed: Embed): CustomPatreon {
//        println(embed.image)
//        println(embed.thumbnail)
//        println(embed.url)
//        println(embed.timestamp?.epochSecond)
        val split = embed.description!!.split("`")
        val name = embed.title!!.split(" ").last()
        val base = split.lastIndex
        val stats = arrayOf(
                split[base - 11],
                split[base - 9],
                split[base - 7],
                split[base - 5],
                split[base - 3],
                split[base - 1]
        ).map { it.toInt() }
        val als =
                embed.description!!.split("\n**Alias:**").last().split("\n**Points:**").first().split(",")
                        .map { it.trim() }.filter { it != "None" }
        val date = embed.description!!.split("\n*for being ")[1].split(" ").take(3).drop(1)
        val creationInfo = CreationInfo(CreationInfo.getMonthNum(date.first())!!, date.last().toInt())
        val timestamp = message.id.longValue ushr 22
        return CustomPatreon(name, stats.map { it }, als, creationInfo, timestamp, embed.thumbnail?.url)
    }

    companion object {
        const val BOT_NAME = "HakiBot"
        const val HAKIBOT_SERVER = 758479736564875265
        const val LXV_SERVER = 714152739252338749
        const val ONLINE_CHANNEL = 761020851029803078
        const val CP_ADD_CHANNEL = 766050133430239243
        const val CP_UPD_CHANNEL = 766050160949330001
        const val DM_CHANNEL = 766048618364796978
        const val SUGGESTION_CHANNEL = 759695877245239297
        const val REPORT_CHANNEL = 779547975289798677
        const val HAKIOBO_ID = 292483348738080769
        const val OWO_ID = 408785106942164992
        const val GLOBAL_PREFIX = "h!"
        const val GLOBAL_OWO_PREFIX = "owo"
        val PRAY_EMOJI = ReactionEmoji.Unicode("\ud83d\ude4f")
        val CURSE_EMOJI = ReactionEmoji.Unicode("\ud83d\udc7b")
        val PST = ZoneId.of("PST", ZoneId.SHORT_IDS)

        fun isResetTime(): Boolean {

            Instant.now().atZone(
                    PST
            ).run {
                return hour == 0 && minute < 30
            }
        }

    }
}

fun Snowflake.toInstant(): Instant = Instant.ofEpochMilli((longValue shr 22) + 1420070400000)
