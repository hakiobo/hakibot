package commands.guild

import Hakibot
import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.behavior.edit
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.*
import entities.HakiUser
import entities.UserGuildOwOCount
import kotlinx.coroutines.delay
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import java.awt.Color

object DeleteOwOCount : BotCommand {
    override val name: String
        get() = "resetowo"
    override val description: String
        get() = "Resets the owo count for the specified member in this server"
    override val aliases: List<String>
        get() = listOf("eraseowo", "clearowo")
    override val category: CommandCategory
        get() = CommandCategory.GUILD
    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(
                listOf(Argument(listOf("id", "mention"), ChoiceType.DESCRIPTION)),
                "Resets the specified user's owo count in the server",
                AccessType.OWNER
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        if (mCE.member!!.isOwner() || mCE.message.author!!.id.longValue == Hakibot.HAKIOBO_ID) {
            if (args.size == 1) {
                val id = getUserIdFromString(args.first())
                if (id != null) {
                    val entry = db.getCollection<UserGuildOwOCount>(UserGuildOwOCount.DB_NAME)
                        .findOne(UserGuildOwOCount::_id eq "$id|${mCE.guildId!!.value}")
                    if (entry != null) {
                        val msg = mCE.message.channel.createEmbed {
                            title = "Confirm OwO Count Reset"
                            description =
                                "Are you sure you want to reset the owo count for <@$id> in ${mCE.getGuild()!!.name}?"
                            footer {
                                text = id.toString()
                            }
                            author {
                                name = mCE.message.author!!.id.value
                            }
                            color = Color(0x0000FF)
                        }
                        listOf(true, false).forEach {
                            msg.addReaction(ReactionEmoji.Unicode(Hakibot.getCheckmarkOrCross(it)))
                        }
                        delay(30_000)
                        if (client.rest.channel.getMessage(
                                mCE.message.channelId.value,
                                msg.id.value
                            ).embeds.firstOrNull()?.color == 0x0000FF
                        ) {
                            msg.edit {
                                embed {
                                    msg.embeds[0].apply(this)
                                    color = Color(0)
                                }
                            }
                        }
                    } else {
                        sendMessage(mCE.message.channel, "Could not find any owos for that user")
                    }
                } else {
                    sendMessage(mCE.message.channel, "Invalid User!")
                }
            } else {
                sendMessage(mCE.message.channel, "Invalid Format!")
            }
        } else {
            sendMessage(mCE.message.channel, "Only the server owner can reset someone's owo count")
        }
    }

    suspend fun Hakibot.confirmDeletion(msg: Message, guildId: Snowflake) {
        val userToDelete = msg.embeds[0].footer!!.text
        val deleted = db.getCollection<UserGuildOwOCount>(UserGuildOwOCount.DB_NAME)
            .findOneAndDelete(UserGuildOwOCount::_id eq "$userToDelete|${guildId.value}")
        val userCol = db.getCollection<HakiUser>(HakiUser.DB_NAME)
        val user = userCol.findOne(HakiUser::_id eq userToDelete)
        if (deleted != null && user != null) {
            userCol.replaceOne(
                HakiUser::_id eq userToDelete,
                user.copy(owoCount = user.owoCount.copy(count = user.owoCount.count - deleted.owoCount))
            )
            msg.channel.createEmbed {
                description = "Succesfully reset <@$userToDelete>'s owos!"
            }
            msg.edit {
                embed {
                    msg.embeds[0].apply(this)
                    color = Color(0x00FF00)
                }
            }

        } else if (deleted != null) {
            msg.channel.createEmbed {
                description = "Succesfully reset <@$userToDelete>'s owos!, but something didn't quite go right."
            }
            msg.edit {
                embed {
                    msg.embeds[0].apply(this)
                    color = Color(0xFFFF00)
                }
            }
        } else {
            sendMessage(msg.channel, "Failed to reset their owos", 5000)
            msg.edit {
                embed {
                    msg.embeds[0].apply(this)
                    color = Color(0xFF0000)
                }
            }
        }
    }

    suspend fun Hakibot.cancelDeletion(msg: Message) {
        msg.edit {
            embed {
                msg.embeds[0].apply(this)
                color = Color(0xFF0000)
            }
        }
    }

}