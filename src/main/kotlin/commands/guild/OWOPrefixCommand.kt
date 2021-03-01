package commands.guild

import entities.HakiGuild
import Hakibot
import commands.utils.*
import dev.kord.common.entity.Permission
import dev.kord.core.event.message.MessageCreateEvent
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

object OWOPrefixCommand : BotCommand {

    override val name: String
        get() = "owoprefix"

    override val description: String
        get() = "Gets the current owobot prefix or changes it"

    override val aliases: List<String>
        get() = listOf("owopre", "owo")

    override val category: CommandCategory
        get() = CommandCategory.GUILD

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(emptyList(), "Gets the current owobot Prefix for this server"),
            CommandUsage(
                listOf(Argument("new prefix", ArgumentType.PARAMETER)),
                "Sets the owobot prefix for this server",
                AccessType.ADMIN
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        val guildID = mCE.guildId!!.asString
        when (args.size) {
            0 -> sendMessage(mCE.message.channel, "Current owo prefix is ${getGuildInfo(mCE.guildId!!).owoPrefix}")
            1 -> {
                if (mCE.member?.getPermissions()
                        ?.contains(Permission.Administrator) == true || mCE.member?.id?.value == Hakibot.HAKIOBO_ID
                ) {
                    val col = db.getCollection<HakiGuild>("guilds")

//                    if (col.find(entities.HakiGuild::_id eq guildID).none()) {
//                        col.insertOne(entities.HakiGuild(guildID, owoPrefix = args.first()))
//                    } else {
                    col.updateOne(HakiGuild::_id eq guildID, setValue(HakiGuild::owoPrefix, args.first()))
//                    }
                    sendMessage(mCE.message.channel, "owo prefix set to ${args.first()}")
                } else {
                    sendMessage(mCE.message.channel, "Only servers admins can change the owo prefix")
                }
            }
            else -> sendMessage(mCE.message.channel, "Prefixes cannot have whitespace")
        }
    }

}