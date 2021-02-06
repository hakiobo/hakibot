package commands

import entities.CreationInfo
import entities.CustomPatreon
import Hakibot
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.mongodb.client.MongoCollection
import commands.utils.*
import math.count
import org.bson.conversions.Bson
import org.litote.kmongo.*

object CPCommand : BotCommand {

    override val name: String
        get() = "cp"

    override val aliases: List<String>
        get() = listOf("custompatreon", "patreon", "pet", "pets", "cps")

    override val description: String
        get() = "Get or Query cp stats"

    override val usages: List<CommandUsage>
        get() = listOf(
            CommandUsage(listOf(), "Shows the total number of pets stored"),
            CommandUsage(
                listOf(
                    Argument(listOf("dex", "d", "get", "g")),
                    Argument(listOf("CP Name", "Alias"), ChoiceType.DESCRIPTION)
                ),
                "Gets the stats of a specific CP"
            ),
            CommandUsage(
                listOf(Argument(listOf("CP Name", "Alias"), ChoiceType.DESCRIPTION)),
                "Gets the stats of a specific CP"
            ),
            CommandUsage(
                listOf(
                    Argument(listOf("query", "q", "search", "s")),
                    Argument("hp"),
                    Argument("att"),
                    Argument("pr"),
                    Argument("wp"),
                    Argument("mag"),
                    Argument("mr")
                ),
                "Queries for cps that match the given stats. * means any stat"
            ),
            CommandUsage(
                listOf(Argument(listOf("year", "qyear", "qy")), Argument("year")),
                "Gets a Count of cps made in the given year"
            ),
            CommandUsage(
                listOf(
                    Argument(listOf("date", "qdate")),
                    Argument(listOf("month name", "month number"), ChoiceType.DESCRIPTION),
                    Argument("year")
                ), "Gets a list of all cps made in a specific month"
            )
        )

    override suspend fun Hakibot.cmd(mCE: MessageCreateEvent, args: List<String>) {
        val cpCol = db.getCollection<CustomPatreon>("cp")
        if (args.isEmpty()) {
            mCE.message.channel.createMessage("${cpCol.countDocuments()} total pets stored")
            return
        }
        val author = mCE.message.author!!
        when (val cmd = args.first().toLowerCase()) {
            "add", "a" -> {
                if (author.id.longValue in getCPAdders()) {
                    if (args.size != 8) {
                        mCE.message.channel.createMessage("wrong format for adding cp, expecting `h! cp $cmd <name> <hp> <str> <pr> <wp> <mag> <mr>`")
                        return
                    }
                    val name = args[1].toLowerCase()
                    val stats = args.drop(2).map { it.toIntOrNull() ?: -1 }
                    if (stats.any { it < 0 }) {
                        mCE.message.channel.createMessage("wrong format for adding cp, expecting `h! cp $cmd <name> <hp> <str> <pr> <wp> <mag> <mr>`")
                        return
                    }

                    val cp = CustomPatreon(name, stats[0], stats[1], stats[2], stats[3], stats[4], stats[5])
                    if (cpCol.find(CustomPatreon::name eq cp.name).none()) {
                        cpCol.insertOne(cp)
                        mCE.message.channel.createMessage("Successfully added $name")
                    } else {
                        mCE.message.channel.createMessage("CP already in database")
                    }
                } else {
                    mCE.message.channel.createMessage("You do not have permission to add to the cp database")
                }
            }
            "get", "dex", "d" -> {
                if (args.size == 2) {
                    val cp = getCP(args[1].toLowerCase(), cpCol)
                    if (cp == null) {
                        sendMessage(mCE.message.channel, "could not find cp ${args[1]}", 10_000)
                    } else {
//                        mCE.message.channel.createMessage(cp.toString())
                        mCE.message.channel.createEmbed {
                            cp.toEmbed(this)
                        }
                    }
                } else if (args.size > 2) {
                    val cps = getCPs(args, cpCol)
                    mCE.message.channel.createMessage(cps.joinToString("\n") {
                        it?.simpleString() ?: "CP Not Found"
                    })
                } else {
                    mCE.message.channel.createMessage("Invalid syntax! expecting `h!cp $cmd <cp names>`")
                }
            }

            "ga", "getall", "da", "reg" -> {
                if (args.size == 2) {
                    val cps = getCPsRegex(args[1].filter { it.isLetterOrDigit() || it == '_' }.toLowerCase(), cpCol)
                    val msg = "Found Cps\n${
                        cps.map(CustomPatreon::name).sorted().take(20).ifEmpty { listOf("No Matches") }
                            .joinToString("\n")
                    }${if (cps.size > 20) "\n(${cps.size - 20} more)" else ""}"

                    sendMessage(
                        mCE.message.channel,
                        if (msg.length <= 2000) msg else "Result Message too long (>2000 characters)"
                    )
                } else {
                    sendMessage(mCE.message.channel, "Invalid syntax! expecting `h!cp $cmd <partial cp name>`", 5_000)
                }
            }

            "delete", "del" -> {
                if (mCE.message.author?.id?.longValue == Hakibot.HAKIOBO_ID) {
                    if (args.size != 2) {
                        mCE.message.channel.createMessage("wrong format for deleting cp entry, expecting `h!cp $cmd <name>`")
                    } else {
                        val search = cpCol.findOneAndDelete(CustomPatreon::name eq args[1])
                        if (search == null) {
                            mCE.message.channel.createMessage("Could not find CP ${args[1]}")
                        } else {
                            mCE.message.channel.createMessage("Successfully deleted ${search.name}")
                        }
                    }
                } else {
                    mCE.message.channel.createMessage("Only Haki can delete cps")
                }
            }
            "search", "s", "query", "q" -> {
                val filters = mutableListOf<Bson>()
                suspend fun badFormat() {
                    mCE.message.channel.createMessage("Correct format is `h!cp $cmd <hp> <att> <pr> <wp> <mag> <mr>` where each stat is a number or * for any value")
                }
                if (args.size == 7) {
                    val props = arrayOf(
                        CustomPatreon::hp,
                        CustomPatreon::str,
                        CustomPatreon::pr,
                        CustomPatreon::wp,
                        CustomPatreon::mag,
                        CustomPatreon::mr
                    )
                    for (x in 0 until 6) {
                        if (args[x + 1] == "*") continue
                        if (args[x + 1].count('-') == 1) {
                            val (a, b) = args[x + 1].split('-')
                            if (a == "") {
                                val high = b.toIntOrNull()
                                if (high == null) {
                                    badFormat()
                                    return
                                }
                                filters.add(props[x] lte high)
                            } else if (b == "") {
                                val low = a.toIntOrNull()
                                if (low == null) {
                                    badFormat()
                                    return
                                }
                                filters.add(props[x] gte low)
                            } else {
                                val low = a.toIntOrNull()
                                val high = b.toIntOrNull()
                                if(low == null || high == null || high <= low){
                                    badFormat()
                                    return
                                }
                                filters.add(props[x] gte low)
                                filters.add(props[x] lte high)
                            }
                        } else {
                            val num = args[x + 1].toIntOrNull()
                            if (num == null) {
                                badFormat()
                                return
                            } else {
                                filters.add(props[x] eq num)
                            }
                        }

                    }
                    val query = cpCol.find(and(filters)).sort(ascending(CustomPatreon::name))
                    if (query.none()) {
                        mCE.message.channel.createMessage("Found no cps with those stats")
                    } else {
                        val sbMessage = StringBuilder("Cps matching query: ${query.count()}\n")
                        query.forEach { cp ->
                            sbMessage.append("    ").append(cp.name).append("\n")
                        }
                        if (sbMessage.length > 2000) {
                            mCE.message.channel.createMessage("${query.count()} Cps. Result too long to fit in single message. I'll eventually add pagination")
                        } else {
                            mCE.message.channel.createMessage(sbMessage.toString())
                        }
                    }
                } else {
                    badFormat()
                }
            }

            "y", "qy", "year", "qyear" -> {
                if (args.size == 2) {
                    val year = args.last().toIntOrNull()
                    mCE.message.channel.createMessage(
                        cpCol.find(CustomPatreon::creationInfo / CreationInfo::year eq year).count().toString()
                    )
                } else {
                    sendMessage(mCE.message.channel, "Correct format is `h!cp $cmd <year>`", 5_000)
                }
            }

            "qdate", "date" -> {
                if (args.size == 3) {
                    val year = args[2].toIntOrNull()
                    val month = args[1].toIntOrNull() ?: CreationInfo.getMonthNum(args[1])
                    val cInfo = if (year == null || month == null) null else CreationInfo(
                        month,
                        if (year < 100) 2000 + year else year
                    )
                    val search = cpCol.find(CustomPatreon::creationInfo eq cInfo).sort(ascending(CustomPatreon::name))
                        .map { it.name }
                    mCE.message.channel.createMessage(
                        "${search.joinToString("\n")}\n${search.count()}"
                    )
                } else {
                    sendMessage(mCE.message.channel, "Correct format is `h!cp $cmd <month> <year>`", 5_000)
                }
            }
            else -> {
                if (args.size == 1) {
                    val cp = getCP(cmd, cpCol)
                    if (cp == null) {
                        sendMessage(mCE.message.channel, "could not find cp $cmd", 10_000)
                    } else {
                        mCE.message.channel.createEmbed {
                            cp.toEmbed(this)
                        }
                    }
                } else {
                    val cps = getCPs(args, cpCol)
                    mCE.message.channel.createMessage(cps.joinToString("\n") {
                        it?.simpleString() ?: "CP Not Found"
                    })
                }
            }
        }
    }

    private fun Hakibot.getCPsRegex(
        name: String,
        cpCol: MongoCollection<CustomPatreon> = db.getCollection<CustomPatreon>("cp")
    ): List<CustomPatreon> {
        return cpCol.find(or(CustomPatreon::name regex name, CustomPatreon::aliases regex name)).toList()
    }

    private fun Hakibot.getCPs(
        names: List<String>,
        cpCol: MongoCollection<CustomPatreon> = db.getCollection<CustomPatreon>("cp")
    ): List<CustomPatreon?> {
        return List(names.size) { idx ->
            getCP(names[idx], cpCol)
        }
    }

    private fun Hakibot.getCP(
        name: String,
        cpCol: MongoCollection<CustomPatreon> = db.getCollection<CustomPatreon>("cp")
    ): CustomPatreon? {
        return cpCol.findOne(or(CustomPatreon::name eq name, CustomPatreon::aliases contains name))
    }
}
