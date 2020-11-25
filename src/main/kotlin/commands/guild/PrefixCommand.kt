package commands.guild

import entities.HakiGuild
import Hakibot
import com.gitlab.kordlib.common.entity.Permission
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import commands.utils.*
import org.litote.kmongo.eq
import org.litote.kmongo.getCollection
import org.litote.kmongo.setValue

object PrefixCommand : BotCommand {

    override val name: String
        get() = "prefix"

    override val description: String
        get() = "Gets the current Hakibot prefix or changes it"

    override val aliases: List<String>
        get() = listOf("pre", "hpre", "hakipre", "hprefix", "hp", "hakiprefix")

    override val category: CommandCategory
        get() = CommandCategory.GUILD

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(emptyList(), "Gets the current Hakibot Prefix for this server"),
            CommandUsage(
                listOf(Argument("new prefix", ArgumentType.PARAMETER)),
                "Sets the Hakibot prefix for this server",
                AccessType.ADMIN
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        val guildID = mCE.guildId!!.value
        when (args.size) {
            0 -> mCE.message.channel.createMessage("Current prefix is ${getGuildInfo(mCE.guildId!!).prefix}")
            1 -> {
                if (mCE.member?.getPermissions()
                        ?.contains(Permission.Administrator) == true || mCE.member?.id?.longValue == Hakibot.HAKIOBO_ID
                ) {
                    val col = db.getCollection<HakiGuild>("guilds")
                    val prefix = args.first().toLowerCase()
//                    if (col.find(entities.HakiGuild::_id eq guildID).none()) {
//                        col.insertOne(entities.HakiGuild(guildID, args.first()))
//                    } else {
                    col.updateOne(HakiGuild::_id eq guildID, setValue(HakiGuild::prefix, prefix))
//                    }
                    mCE.message.channel.createMessage("prefix changed to $prefix")
                } else {
                    mCE.message.channel.createMessage("Only servers admins can change the prefix")
                }
            }
            else -> mCE.message.channel.createMessage("Prefixes cannot have whitespace")
        }
    }

}