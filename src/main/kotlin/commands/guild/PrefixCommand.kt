package commands.guild

import entities.HakiGuild
import Hakibot
import commands.utils.*
import dev.kord.common.entity.Permission
import dev.kord.core.event.message.MessageCreateEvent
import org.litote.kmongo.eq
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
        val guildID = mCE.guildId!!.asString
        when (args.size) {
            0 -> sendMessage(mCE.message.channel, "Current prefix is ${getGuildInfo(mCE.guildId!!).prefix}")
            1 -> {
                if (mCE.member?.getPermissions()
                        ?.contains(Permission.Administrator) == true || mCE.member?.id?.value == Hakibot.HAKIOBO_ID
                ) {
                    val col = db.getCollection<HakiGuild>("guilds")
                    val prefix = args.first().toLowerCase()
//                    if (col.find(entities.HakiGuild::_id eq guildID).none()) {
//                        col.insertOne(entities.HakiGuild(guildID, args.first()))
//                    } else {
                    col.updateOne(HakiGuild::_id eq guildID, setValue(HakiGuild::prefix, prefix))
//                    }
                    sendMessage(mCE.message.channel, "prefix changed to $prefix")
                } else {
                    sendMessage(mCE.message.channel, "Only servers admins can change the prefix")
                }
            }
            else -> sendMessage(mCE.message.channel, "Prefixes cannot have whitespace")
        }
    }

}