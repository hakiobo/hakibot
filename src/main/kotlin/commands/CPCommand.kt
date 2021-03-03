package commands

import entities.CreationInfo
import entities.CustomPatreon
import Hakibot
import commands.utils.*
import dev.kord.core.event.message.MessageCreateEvent
import math.count
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection

object CPCommand : BotCommand {

    override val name: String
        get() = "cp"

    override val aliases: List<String>
        get() = listOf("custompatreon", "pet", "pets", "cps")

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
            sendMessage(mCE.message.channel, "${cpCol.countDocuments()} total pets stored")
            return
        }
        val author = mCE.message.author!!
        when (val cmd = args.first().toLowerCase()) {
            "add", "a" -> {
                if (author.id.value in getCPAdders()) {
                    if (args.size != 8) {
                        sendMessage(
                            mCE.message.channel,
                            "wrong format for adding cp, expecting `h! cp $cmd <name> <hp> <str> <pr> <wp> <mag> <mr>`"
                        )
                        return
                    }
                    val name = args[1].toLowerCase()
                    val stats = args.drop(2).map { it.toIntOrNull() ?: -1 }
                    if (stats.any { it < 0 }) {
                        sendMessage(
                            mCE.message.channel,
                            "wrong format for adding cp, expecting `h! cp $cmd <name> <hp> <str> <pr> <wp> <mag> <mr>`"
                        )
                        return
                    }

                    val cp = CustomPatreon(name, stats[0], stats[1], stats[2], stats[3], stats[4], stats[5])
                    if (cpCol.find(CustomPatreon::name eq cp.name).toList().none()) {
                        cpCol.insertOne(cp)
                        sendMessage(mCE.message.channel, "Successfully added $name")
                    } else {
                        sendMessage(mCE.message.channel, "CP already in database")
                    }
                } else {
                    sendMessage(mCE.message.channel, "You do not have permission to add to the cp database")
                }
            }
            "get", "dex", "d" -> {
                if (args.size == 2) {
                    val cp = getCP(args[1].toLowerCase(), cpCol)
                    if (cp == null) {
                        sendMessage(mCE.message.channel, "could not find cp ${args[1]}", 10_000)
                    } else {
//                        sendMessage(mCE.message.channel, cp.toString())
                        sendMessage(mCE.message.channel) {
                            cp.toEmbed(this)
                        }
                    }
                } else if (args.size > 2) {
                    val cps = getCPs(args, cpCol)
                    sendMessage(mCE.message.channel, cps.joinToString("\n") {
                        it?.simpleString() ?: "CP Not Found"
                    })
                } else {
                    sendMessage(mCE.message.channel, "Invalid syntax! expecting `h!cp $cmd <cp names>`")
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
                if (mCE.message.author?.id?.value == Hakibot.HAKIOBO_ID) {
                    if (args.size != 2) {
                        sendMessage(
                            mCE.message.channel,
                            "wrong format for deleting cp entry, expecting `h!cp $cmd <name>`"
                        )
                    } else {
                        val search = cpCol.findOneAndDelete(CustomPatreon::name eq args[1])
                        if (search == null) {
                            sendMessage(mCE.message.channel, "Could not find CP ${args[1]}")
                        } else {
                            sendMessage(mCE.message.channel, "Successfully deleted ${search.name}")
                        }
                    }
                } else {
                    sendMessage(mCE.message.channel, "Only Haki can delete cps")
                }
            }
            "search", "s", "query", "q" -> {
                val filters = mutableListOf<Bson>()
                suspend fun badFormat() {
                    sendMessage(
                        mCE.message.channel,
                        "Correct format is `h!cp $cmd <hp> <att> <pr> <wp> <mag> <mr>` where each stat is a number or * for any value"
                    )
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
                                if (low == null || high == null || high <= low) {
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
                    val query = cpCol.find(and(filters)).sort(ascending(CustomPatreon::name)).toList()
                    if (query.none()) {
                        sendMessage(mCE.message.channel, "Found no cps with those stats")
                    } else {
                        val sbMessage = StringBuilder("Cps matching query: ${query.count()}\n")
                        for (cp in query) {
                            sbMessage.append("    ").append(cp.name).append("\n")
                            if (sbMessage.length > 2000) break
                        }

                        if (sbMessage.length > 2000) {
                            sendMessage(
                                mCE.message.channel,
                                "${query.count()} Cps. Result too long to fit in single message. I'll eventually add pagination"
                            )
                        } else {
                            sendMessage(mCE.message.channel, sbMessage.toString())
                        }
                    }
                } else {
                    badFormat()
                }
            }

            "y", "qy", "year", "qyear" -> {
                if (args.size == 2) {
                    val year = args.last().toIntOrNull()
                    sendMessage(
                        mCE.message.channel,
                        cpCol.find(CustomPatreon::creationInfo / CreationInfo::year eq year).toList().count().toString()
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
                    val search = cpCol.find(CustomPatreon::creationInfo eq cInfo).sort(ascending(CustomPatreon::name)).toList()
                        .map { it.name }
                    sendMessage(mCE.message.channel, "${search.joinToString("\n")}\n${search.count()}")
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
                        sendMessage(mCE.message.channel) {
                            cp.toEmbed(this)
                        }
                    }
                } else {
                    val cps = getCPs(args, cpCol)
                    sendMessage(mCE.message.channel, cps.joinToString("\n") {
                        it?.simpleString() ?: "CP Not Found"
                    })
                }
            }
        }
    }

    private suspend fun Hakibot.getCPsRegex(
        name: String,
        cpCol: CoroutineCollection<CustomPatreon> = db.getCollection<CustomPatreon>("cp")
    ): List<CustomPatreon> {
        return cpCol.find(or(CustomPatreon::name regex name, CustomPatreon::aliases regex name)).toList()
    }

    private suspend fun Hakibot.getCPs(
        names: List<String>,
        cpCol: CoroutineCollection<CustomPatreon> = db.getCollection<CustomPatreon>("cp")
    ): List<CustomPatreon?> {
        return List(names.size) { idx ->
            getCP(names[idx], cpCol)
        }
    }

    private suspend fun Hakibot.getCP(
        name: String,
        cpCol: CoroutineCollection<CustomPatreon> = db.getCollection<CustomPatreon>("cp")
    ): CustomPatreon? {
        return cpCol.findOne(or(CustomPatreon::name eq name, CustomPatreon::aliases contains name))
    }
}
