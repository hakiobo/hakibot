package commands

import CustomPatreon
import Hakibot
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.GuildMessageChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.entity.Message

import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.mongodb.client.MongoCollection
import commands.utils.*
import kotlinx.coroutines.flow.collect
import org.litote.kmongo.*

import kotlin.Exception

object SearchForCPCommand : BotCommand {
    override val name: String
        get() = "cpsearch"
    override val description: String
        get() = "Searchs a channel for cp dexes"
    override val category
        get() = CommandCategory.HIDDEN

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(
                listOf(Argument(listOf("Channel Id", "Channel mention"), ChoiceType.DESCRIPTION)),
                "Searches the provided channel for CP dex entries",
                AccessType.HAKI
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (mCE.message.author?.id?.longValue == Hakibot.HAKIOBO_ID) {

            if (args.size == 1) {
                val guild = mCE.getGuild()!!

                val channel = if (args.first().toLongOrNull() != null) {
                    guild.getChannelOrNull(Snowflake(args.first()))
                } else if (args.first().startsWith("<#") && args.first().endsWith(">") && args.first().drop(2)
                        .dropLast(1).toLongOrNull() != null
                ) {
                    guild.getChannelOrNull(Snowflake(args.first().drop(2).dropLast(1)))
                } else {
                    null
                }
                if (channel != null) {
                    if (channel is GuildMessageChannelBehavior) {
                        mCE.message.channel.createMessage("Beginning Search in ${channel.mention}")
                        val stats = intArrayOf(0, 0, 0, 0, 0)
                        try {
                            val col = db.getCollection<CustomPatreon>("cp")
                            channel.messages.collect {
                                stats[parseMessage(it, col, mCE.message.channel)]++
                            }
                        } catch (e: Exception) {
                            this.dmUser(Hakibot.HAKIOBO_ID, "Some exception occured in reading ${channel.name}")
                            sendMessage(mCE.message.channel, "Failed after some point in time", 10_000)
                        }
                        mCE.message.channel.createMessage(
                            "Search Complete ${channel.mention}\n${stats.sum()} messages searched\n" +
                                    "${stats[0]} NonCps Found\n${stats[1]} new CPs added\n${stats[2]} Duplicate cps found\n" +
                                    "${stats[3]} CPs Updated\n${stats[4]} CPs Not Parsed"
                        )
                    } else sendMessage(mCE.message.channel, "Mentioned Channel must be a Text Channel", 5_000)
                } else {
                    sendMessage(mCE.message.channel, "You must mention a valid channel", 5_000)
                }
            } else if (args.size == 2) {
                val guild = mCE.getGuild()!!
                val channel = if (args.first().toLongOrNull() != null) {
                    guild.getChannelOrNull(Snowflake(args.first()))
                } else if (args.first().startsWith("<#") && args.first().endsWith(">") && args.first().drop(2)
                        .dropLast(1).toLongOrNull() != null
                ) {
                    guild.getChannelOrNull(Snowflake(args.first().drop(2).dropLast(1)))
                } else {
                    null
                }
                if (channel != null) {
                    if (channel is GuildMessageChannelBehavior) {
                        val msg = if (args.last().toLongOrNull() != null) {
                            channel.getMessageOrNull(Snowflake(args.last()))
                        } else {
                            null
                        }
                        if (msg != null) {
                            parseMessage(msg, db.getCollection<CustomPatreon>("cp"), mCE.message.channel)
                        }
                    }
                }
            } else {
                sendMessage(
                    mCE.message.channel,
                    "You must mention exactly one channel, or a channel and a message",
                    10_000
                )
            }
        } else {
            mCE.message.channel.createMessage("Only Haki can use the CP Search command")
        }
    }

    private suspend fun Hakibot.parseMessage(
        message: Message,
        col: MongoCollection<CustomPatreon>,
        srcChannel: MessageChannelBehavior
    ): Int {
        if (message.author?.id?.longValue == Hakibot.OWO_ID) {
            val embed = message.embeds.firstOrNull()
            if (embed?.description?.startsWith("*Created by*") == true) {
                try {
                    val new = parseCP(message, embed)
                    return when (val old = col.findOne(CustomPatreon::name eq new.name)) {
                        null -> {
                            col.insertOne(new)
                            sendMessage(srcChannel, "Added\n$new", 10_000)
                            1
                        }
                        new -> {
                            if (new.lastUpdatedMS > old.lastUpdatedMS) {
                                col.updateOne(
                                    CustomPatreon::name eq new.name,
                                    setValue(CustomPatreon::lastUpdatedMS, new.lastUpdatedMS)
                                )
                            }
                            2
                        }
                        else -> {
                            if (new.lastUpdatedMS > old.lastUpdatedMS) {
                                col.replaceOne(CustomPatreon::name eq new.name, new)
                                sendMessage(srcChannel, "Updated\n$old\nto\n$new", 10_000)
                                3
                            } else {
                                2
                            }
                        }
                    }
                } catch (e: Exception) {
                    srcChannel.createMessage("Failed to Parse ${message.id}")
                    return 4
                }
            }
        }
        return 0
    }
}


