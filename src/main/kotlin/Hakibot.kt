import commands.*
import commands.guild.*
import commands.guild.DeleteOwOCount.cancelDeletion
import commands.guild.DeleteOwOCount.confirmDeletion
import commands.hidden.DMCommand
import commands.hidden.GlobalDisableCommand
import commands.hidden.LogoutCommand
import commands.hidden.SearchForCPCommand
import commands.meta.*
import commands.utils.BotCommand
import commands.utils.CommandCategory
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.Embed
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.event.message.MessageUpdateEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.on
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.request.RestRequestException
import entities.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import java.lang.Exception
import java.time.Instant
import java.time.ZoneId
import java.util.regex.Pattern
import kotlin.reflect.KMutableProperty1

class Hakibot(val client: Kord, val db: CoroutineDatabase) {
    @Volatile
    internal var huntReminders = true

    @Volatile
    internal var prayReminders = true

    @Volatile
    internal var whenActive = true

    @Volatile
    internal var triggersActive = true

//    @Volatile
//    internal var abnormalThreshold = 10

    private val triggers = mapOf(
//        "haki" to "is coding god",
//        "hika" to "god is coding",
//        "anvi" to "sewen sewenty",
//        "asono" to "waifu's inva", "asona" to "waifu's inva", "asano" to "waifu's inva",
//        "nea" to "js in codes",
//        "yez" to "doesn't code enough",
//        "furo" to "Furory?",
//        "shufi" to "your bow lol god down",
        "sellchart" to "5 - B\n2.5 - F\n2 - P CP H\n1.67 - M\n1.5 - L G D\n1.2 - S\n1 - C E\n0.6 - U\n0.5 - R",
//        "licu" to "Should curse haki"
    )
    val commands = listOf(
        HelpCommand,
//        ReportCommand,
        DMCommand,
        PrefixCommand,
        OWOPrefixCommand,
        OWOHuntSettingsCMD,
        OWOPraySettingsCommand,
        CPCommand,
        WhoIsCommand,
//        SuggestCommand,
        SearchForCPCommand,
//        InviteCommand,
        GuildsCommand,
//        ServerCommand,
        LogoutCommand,
        TriggerCommand,
        WhenCommand,
        UseGlobalPrefixCommand,
//        OwOStat,
//        OwOLeaderboard,
        MathCommand,
        Ping,
        GithubCommand,
        GlobalDisableCommand,
        ViewGlobalSettings,
        GuildStatus,
//        DisableCounting,
//        DeleteOwOCount,
//        Patreon,
    )

    @KordPreview
    suspend fun startUp() {
        client.on<ReadyEvent> {
            messageChannelById(ONLINE_CHANNEL, "Online (as Hakibot, but only for LXV)! (${this.shard})")
        }
        client.on<ReactionAddEvent> {
            if (userId.value == OWO_ID && emoji == ReactionEmoji.Unicode("\u27a1\ufe0f") && (guildId?.value == HAKIBOT_SERVER || channelId.value in HAKI_SHOP_REACT_CHANNELS)) {
                val embed = getMessage().embeds.firstOrNull()
                if (embed?.author?.name == "Today's Available Weapons") {
                    readShopWeapon(embed.description!!).forEach {
                        message.addReaction(it)
                    }
                }
            } else if (userId.value == HAKIOBO_ID && emoji == SuggestCommand.TRASH && getMessage().author?.id == client.selfId) {
                message.delete()
//            } else if (emoji in listOf(
//                    ReactionEmoji.Unicode(CHECKMARK_EMOJI),
//                    ReactionEmoji.Unicode(CROSSMARK_EMOJI)
//                ) && (this.userAsMember?.asMember()?.isOwner() == true || userId.value == HAKIOBO_ID)
//            ) {
//                val msg = getMessage()
//                if (msg.author?.id?.value == client.selfId.value) {
//                    val embed = msg.embeds.firstOrNull()
//                    if (embed?.author?.name == userId.asString && getUserIdFromString(embed.footer!!.text) != null
//                        && embed.color?.rgb == 0x0000FF
//                    ) {
//                        if (emoji == ReactionEmoji.Unicode(CHECKMARK_EMOJI)) {
//                            confirmDeletion(msg, guildId!!)
//                        } else {
//                            cancelDeletion(msg)
//                        }
//                    }
//                }
            }
        }
        client.on<MessageCreateEvent> {
            client.launch {
                handleMessage(this@on)
            }
        }
        client.on<MessageUpdateEvent> {
            if (new.author.value?.id?.value == OWO_ID && (this.new.guildId.value?.value == HAKIBOT_SERVER || this.new.channelId.value in HAKI_SHOP_REACT_CHANNELS)) {
                if (new.embeds.value?.firstOrNull()?.author?.value?.name?.value == "Today's Available Weapons") {
                    try {
                        val embed = new.embeds.value!!.first()

                        val oldR = getMessage().reactions.filter { it.selfReacted }.map { it.emoji }
                        val newR = readShopWeapon(embed.description.value!!)

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
            if (col.findOne(HakiGuild::_id eq guild.id.asString) == null) {
                col.insertOne(HakiGuild(guild.id.asString))
            }
        }
        client.login {
            since = Instant.now().minusSeconds(60 * 60 * 8L)!!
            playing("h!help")
        }
    }

    suspend fun getUserFromDB(
        userID: Snowflake,
        u: User? = null,
        col: CoroutineCollection<HakiUser> = db.getCollection<HakiUser>("users")
    ): HakiUser {
        val query = col.findOne(HakiUser::_id eq userID.asString)
        return if (query == null) {
            val user = if (u == null) {
                HakiUser(userID.asString, client.getUser(userID)?.username ?: "Deleted User#${userID.value}")
            } else {
                HakiUser(userID.asString, u.username)
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

    @KordPreview
    private suspend fun handleMessage(mCE: MessageCreateEvent) {
        if (mCE.message.author?.id == client.selfId) return
        if (mCE.message.author == null) return
        if (mCE.message.author?.id?.value == OWO_ID) {
            val embed = mCE.message.embeds.firstOrNull()
            if (embed?.description?.startsWith("*Created by*") == true) {
                try {
                    handleOWODexEntry(mCE, embed)
                } catch (e: Exception) {
                    mCE.message.addReaction(ReactionEmoji.Unicode("\u274c"))
                    val logEmbed = EmbedBuilder()
                    embed.apply(logEmbed)
                    messageChannelById(CP_FAIL_CHANNEL, "```\n${embed.description}\n```", logEmbed)
                    println("Error Reading CP: ${e.message}")
                    sendMessage(mCE.message.channel, "Could not parse CP stats", 10_000)
                }
            } else if (embed?.description?.startsWith("**Name:**") == true) {
                val desc = embed.description!!
                val info = when (embed.author?.name?.split("'s ")?.last()) {
                    "Great Sword" -> sword(desc) to "Sword"
                    "Healing Staff" -> hStaff(desc) to "HStaff"
                    "Bow" -> bow(desc) to "Bow"
                    "Rune of the Forgotten" -> rune(desc) to "Rune"
                    "Aegis" -> shield(desc) to "Shield"
                    "Orb of Potency" -> orb(desc) to "Orb"
                    "Vampiric Staff" -> vStaff(desc) to "VStaff"
                    "Poison Dagger" -> dagger(desc) to "Dagger"
                    "Wand of Absorption" -> wand(desc) to "Wand"
                    "Flame Staff" -> fStaff(desc) to "FStaff"
                    "Energy Staff" -> eStaff(desc) to "EStaff"
                    "Spirit Staff" -> sStaff(desc) to "SStaff"
                    "Arcane Scepter" -> scepter(desc) to "Scepter"
                    "Resurrection Staff" -> rStaff(desc) to "RStaff"
                    "Glacial Axe" -> axe(desc) to "Axe"
                    "Banner" -> banner(desc) to "Banner"
                    "Culling Scythe" -> scythe(desc) to "Scythe"
                    else -> emptyList<ReactionEmoji>() to "nothing"
                }
                info.first.forEach {
                    mCE.message.addReaction(it)
                }
                if (info.first.firstOrNull() == ReactionEmoji.Custom(Snowflake(760023282878513161), "Fabled", true)) {
                    val id = desc.split("\n**ID:** ").last().split("`")[1]
                    val weaps = db.getCollection<Weapon>("weapons")
                    if (weaps.findOne(Weapon::_id eq id) == null) {
                        val msg = messageChannelById(FABLED_WEAP_CHANNEL, "", EmbedBuilder().apply {
                            title = "Fabled ${info.second} Found"
                            description = "ID: $id"
                        })
                        weaps.insertOne(Weapon(id, info.second))
                        client.rest.channel.crossPost(msg.channelId, msg.id)
                    }
                }
            }
        }
        if (mCE.message.author?.isBot == true) return



        if (mCE.guildId == null) return handleDM(mCE)

        val guild = getGuildInfo(mCE.guildId!!)
//        val user = getUserFromDB(mCE.message.author!!.id, mCE.message.author)


        if (whenActive && guild.settings.enableWhen && mCE.message.content.filter(Char::isLetterOrDigit).takeLast(4)
                .toLowerCase() == "when"
        ) {
            sendMessage(mCE.message.channel, "when", 10_000)
        }
        if (triggersActive && guild.settings.enableTriggers) {
            val triggerText = triggers[mCE.message.content.toLowerCase()]
            if (triggerText != null) {
                sendMessage(mCE.message.channel, triggerText)
            }
        }


        val pre = guild.prefix
        if (guild.settings.allowGlobalPrefix && mCE.message.content.startsWith(GLOBAL_PREFIX, ignoreCase = true)) {
            handleCommand(mCE, mCE.message.content.drop(GLOBAL_PREFIX.length).trim())
        } else if (mCE.message.content.startsWith(pre, ignoreCase = true)) {
            handleCommand(mCE, mCE.message.content.drop(pre.length).trim())
        }
        val owoPre = guild.owoPrefix
        val maybeCountOwO = mCE.message.content.contains("owo", true) || mCE.message.content.contains("uwu", true)
        if (mCE.message.content.startsWith(GLOBAL_OWO_PREFIX, ignoreCase = true)) {
            handleOWOCommand(
                mCE,
                guild,
                mCE.message.content.drop(GLOBAL_OWO_PREFIX.length).trim(),
                maybeCountOwO,
            )
        } else if (mCE.message.content.startsWith(owoPre, ignoreCase = true)) {
            handleOWOCommand(
                mCE,
                guild,
                mCE.message.content.drop(owoPre.length).trim(),
                maybeCountOwO,
            )
        } else if (maybeCountOwO) {
//            countOwO(mCE, getUserFromDB(mCE.message.author!!.id, mCE.message.author), guild)
        }
        if (mCE.message.mentionedUserIds.contains(client.selfId)) {
            if (!guild.settings.allowGlobalPrefix) {
                sendMessage(mCE.message.channel, "Global $BOT_NAME prefix Disabled\nServer $BOT_NAME prefix is $pre")
            } else if (GLOBAL_PREFIX == pre) {
                sendMessage(mCE.message.channel, "$BOT_NAME prefix is $pre")
            } else {
                sendMessage(
                    mCE.message.channel,
                    "Global $BOT_NAME prefix is $GLOBAL_PREFIX\nServer $BOT_NAME prefix is $pre"
                )
            }
        }
    }

    suspend fun getGuildInfo(
        guild: Snowflake,
        col: CoroutineCollection<HakiGuild> = db.getCollection<HakiGuild>("guilds")
    ): HakiGuild {
        return col.findOne(HakiGuild::_id eq guild.asString) ?: HakiGuild(guild.asString)
    }

    private suspend fun handleCommand(mCE: MessageCreateEvent, msg: String) {
        val split = msg.split(Pattern.compile("\\s+"))
        val userCMD = split.first().toLowerCase()
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

    private suspend fun handleOWOCommand(
        mCE: MessageCreateEvent,
        guild: HakiGuild,
        msg: String,
        countOwOOnFail: Boolean
    ) {
        val split = msg.split(Pattern.compile("\\s"))
        when (split.firstOrNull()) {
            "hunt", "h", "catch" -> if (huntReminders) owoHuntOWOCMD(
                mCE,
                getUserFromDB(mCE.message.author!!.id, mCE.message.author)
            )
            "pray", "curse" -> if (prayReminders) owoPrayOWOCMD(
                mCE,
                split.first(),
                getUserFromDB(mCE.message.author!!.id, mCE.message.author)
            )
//            in owoCommands -> {
//            }
//            else -> if (countOwOOnFail) countOwO(mCE, getUserFromDB(mCE.message.author!!.id, mCE.message.author), guild)
        }
    }

    private suspend fun owoHuntOWOCMD(mCE: MessageCreateEvent, user: HakiUser) {
        val authorID = mCE.message.author!!.id
        val col = db.getCollection<HakiUser>("users")
        val time = mCE.message.id.toInstant().toEpochMilli()
        if (user.owoSettings.huntRemind && (time - user.owoSettings.lastHunt) >= 15 * 1000) {
            col.updateOne(
                HakiUser::_id eq authorID.asString,
                setValue(HakiUser::owoSettings / OWOSettings::lastHunt, time)
            )
            client.launch {
                delay((15_000 + time - Instant.now().toEpochMilli()).coerceAtLeast(1))
                sendMessage(mCE.message.channel, "${mCE.message.author!!.mention} hunt cooldown is done", 5_000, true)
            }
        }

    }

    private suspend fun owoPrayOWOCMD(mCE: MessageCreateEvent, cmd: String, user: HakiUser) {
        val authorID = mCE.message.author!!.id
        val col = db.getCollection<HakiUser>("users")
        val time = mCE.message.id.toInstant().toEpochMilli()
        if (user.owoSettings.prayRemind && (time - user.owoSettings.lastPray) >= 5 * 60 * 1000) {
            col.updateOne(
                HakiUser::_id eq authorID.asString,
                setValue(HakiUser::owoSettings / OWOSettings::lastPray, time)
            )
            if (cmd.startsWith("p")) {
                mCE.message.addReaction(PRAY_EMOJI)
            } else {
                mCE.message.addReaction(CURSE_EMOJI)
            }
            client.launch {
                delay((300_000 + time - Instant.now().toEpochMilli()).coerceAtLeast(1))
                sendMessage(
                    mCE.message.channel,
                    "${mCE.message.author!!.mention} pray/curse cooldown is done",
                    mentionsAllowed = true
                )

//                delay(10_000)
//                resp.delete()
            }
        }

    }

    private suspend fun dmUser(user: User, message: String) {
        val channel = try {
            user.getDmChannel()
        } catch (exception: RestRequestException) {
            if (user.id.value != HAKIOBO_ID) dmUser(HAKIOBO_ID, "Failed to get ${user.tag}'s DM's")
            return
        }
        try {
            sendMessage(channel, message)
        } catch (exception: RestRequestException) {
            if (user.id.value != HAKIOBO_ID) dmUser(HAKIOBO_ID, "Failed to send DM to ${user.tag}")

        }
    }

    private suspend fun handleDM(mCE: MessageCreateEvent) {
        if (mCE.message.content.takeLast(4).toLowerCase() == "when") {
            sendMessage(mCE.message.channel, "when", 10_000)
            return
        }

        val split = (if (mCE.message.content.startsWith("h!", ignoreCase = true)) mCE.message.content.drop(2)
            .trim() else mCE.message.content).split(Pattern.compile("\\s+"))
        val userCMD = split.first().toLowerCase()

        val args = split.drop(1)
        val cmd = lookupCMD(userCMD)
        if (cmd != null) {
            if (cmd.category == CommandCategory.GUILD) {
                sendMessage(mCE.message.channel, "Guild related commands not allowed in DMs")
            } else {
                cmd.runCMD(this, mCE, args)
            }
        } else {
            messageChannelById(DM_CHANNEL, "", EmbedBuilder().apply {
                title = mCE.message.author?.tag ?: "no author"
                description = mCE.message.content
                if (mCE.message.author != null) {
                    footer {
                        text = mCE.message.author!!.id.asString
                    }
                }

            })
        }

    }

    internal fun getCPAdders() =
        arrayOf(
            HAKIOBO_ID
//        , client.getUser(Snowflake( 304511726907293697))
        )

    internal suspend fun sendMessage(
        channel: MessageChannelBehavior,
        message: String = "",
        deleteAfterMS: Long = 0L,
        mentionsAllowed: Boolean = false,
        embedToSend: (EmbedBuilder.() -> Unit)? = null,
    ) {
        client.launch {
            if (deleteAfterMS == 0L) {
                channel.createMessage {
                    content = message
                    if (!mentionsAllowed) {
                        allowedMentions()
                    }
                    if (embedToSend != null) {
                        embed(embedToSend)
                    }
                }
            } else {
                val msg = channel.createMessage {
                    content = message
                    if (!mentionsAllowed) {
                        allowedMentions()
                    }
                    if (embedToSend != null) {
                        embed(embedToSend)
                    }
                }
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

    internal suspend fun messageChannelById(
        channelId: Long,
        message: String,
        embedBuilder: EmbedBuilder? = null
    ): DiscordMessage {
        return client.rest.channel.createMessage(Snowflake(channelId)) {
            embed = embedBuilder
            content = message
        }
    }

    internal fun getUserIdFromString(s: String?): Long? {
        return if (s == null) {
            null
        } else if (s.toLongOrNull() != null) {
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

    @KordPreview
    private suspend fun handleOWODexEntry(mCE: MessageCreateEvent, embed: Embed) {
        val new = parseCP(mCE.message, embed)
        val name = new.name
        val col = db.getCollection<CustomPatreon>("cp")
        when (val search = col.findOne(CustomPatreon::name eq name)) {
            null -> {
                col.insertOne(new)
                val msg = messageChannelById(CP_ADD_CHANNEL, "added from ${mCE.message.getGuild().name}", new.toEmbed())
//                dmUser(HAKIOBO_ID, ")
                mCE.message.addReaction(ReactionEmoji.Unicode("\u2705"))
                sendMessage(mCE.message.channel, "added\n$new", 10_000)
                client.rest.channel.crossPost(msg.channelId, msg.id)
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
//        println(embed.description)
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
        val timestamp = message.id.value ushr 22
        return CustomPatreon(name, stats.map { it }, als, creationInfo, timestamp, embed.thumbnail?.url)
    }

    enum class DisableableFeatures(val desc: String, val property: KMutableProperty1<Hakibot, Boolean>) {
        WHEN("When Reaction", Hakibot::whenActive),
        TRIGGERS("Keyword Triggers", Hakibot::triggersActive),
        HUNT_REMINDER("Hunt Reminders", Hakibot::huntReminders),
        PRAY_REMIND("Pray/Curse Reminders", Hakibot::prayReminders),
    }

    companion object {
        const val BOT_NAME = "HikaBot"
        const val HAKIBOT_SERVER = 758479736564875265
        const val LXV_SERVER = 714152739252338749
        private const val LXV_ALERT_CHANNEL = 714178162757599344
        private const val HNS_ANNOUNCEMENT_CHANNEL = 572201299937067019
        private const val BXW_SHOP_CHANNEL = 766068414212210738
        val HAKI_SHOP_REACT_CHANNELS = listOf(HNS_ANNOUNCEMENT_CHANNEL, LXV_ALERT_CHANNEL, BXW_SHOP_CHANNEL)

        //        const val ONLINE_CHANNEL = 761020851029803078
        const val ONLINE_CHANNEL = 816768818088116225
        const val CP_ADD_CHANNEL = 766050133430239243
        const val CP_UPD_CHANNEL = 766050160949330001
        const val CP_FAIL_CHANNEL = 809676379020722186
        const val DM_CHANNEL = 766048618364796978
        const val SUGGESTION_CHANNEL = 759695877245239297
        const val REPORT_CHANNEL = 779547975289798677
        const val FABLED_WEAP_CHANNEL = 807512919046356993
        const val HAKIOBO_ID = 292483348738080769
        const val OWO_ID = 408785106942164992
        const val GLOBAL_PREFIX = "h!"
        const val GLOBAL_OWO_PREFIX = "owo"
        const val SERVER_CODE = "k3XgR4s"
        val PRAY_EMOJI = ReactionEmoji.Unicode("\ud83d\ude4f")
        val CURSE_EMOJI = ReactionEmoji.Unicode("\ud83d\udc7b")
        const val CHECKMARK_EMOJI = "\u2705"
        const val CROSSMARK_EMOJI = "\u274c"

        val PST: ZoneId = ZoneId.of("PST", ZoneId.SHORT_IDS)

        fun isResetTime(): Boolean {

            Instant.now().atZone(
                PST
            ).run {
                return hour == 0
            }
        }

        fun getCheckmarkOrCross(checkmark: Boolean): String = if (checkmark) CHECKMARK_EMOJI else CROSSMARK_EMOJI


    }
}

fun Snowflake.toInstant(): Instant = Instant.ofEpochMilli((value ushr 22) + 1420070400000L)
