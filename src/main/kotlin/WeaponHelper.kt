import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.ReactionEmoji
import java.util.regex.Pattern

val qualityEmojis = listOf(
    ReactionEmoji.Unicode("\u0030\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0031\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0032\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0033\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0034\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0035\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0036\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0037\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0038\ufe0f\u20e3"),
    ReactionEmoji.Unicode("\u0039\ufe0f\u20e3")
)
val extraEmoji = ReactionEmoji.Unicode("\u2b06\ufe0f")
val lorb = ReactionEmoji.Custom(Snowflake(764335693907361802), "lorb", false)


fun sword(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 250 - split.first().toInt()
    val dmgPart = split[4].dropLast(3).drop(2).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val statSum = cost + (dmg - 350) / 2 + 100
    val pcts = getBestPct(statSum, 3)
    val reactions = mutableListOf(judgeWeapon(statSum, 3), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun hStaff(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = ((200 - split.first().toInt()) * 4 + 2) / 3
    val dmgPart = split[4].dropLast(3).drop(2).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val statSum = cost + (dmg - 1000) / 5 + 100

    val r1 = judgeWeapon(statSum, 3)
    val q1 = getBestPct(statSum, 3)
    val reactions = mutableListOf(r1, q1.first)
    if (q1.first != q1.second) {
        reactions += q1.second
    }
    if (cost != 100 && cost % 4 == 0) {
        val q2 = getBestPct(statSum + 1, 3)
        if (q1 != q2) {
            reactions += extraEmoji
        }
    }
    return reactions
}

fun bow(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 220 - split.first().toInt()
    val dmgPart = split[4].dropLast(3).drop(2).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val statSum = cost + (dmg - 1100) / 5 + 100
    val pcts = getBestPct(statSum, 3)
    val reactions = mutableListOf(judgeWeapon(statSum, 3), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun rune(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val increasePart = split[7].dropLast(2).split(".")
    val increase = increasePart.first().toInt() * 10 + if (increasePart.size == 2) increasePart[1].toInt() else 0
    val statSum = increase - 50
    val pcts = getBestPct(statSum, 1)
    val reactions = mutableListOf(judgeWeapon(statSum, 1), pcts.first)
    if (pcts.first != pcts.second) {
        reactions += pcts.second
    }
    return reactions
}

fun shield(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 250 - split.first().toInt()
    val tauntPart = split[32].dropLast(3).drop(2).split(".")
    val taunt = tauntPart.first().toInt() * 10 + if (tauntPart.size == 2) tauntPart[1].toInt() else 0
    val statSum = cost + (taunt - 300) / 2 + 100
    val pcts = getBestPct(statSum, 3)
    val reactions = mutableListOf(judgeWeapon(statSum, 3), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun orb(desc: String): List<ReactionEmoji> {
    return listOf(lorb)
}

fun vStaff(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 200 - split.first().toInt()
    val dmgPart = split[4].dropLast(3).drop(2).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val statSum = cost + (dmg - 250) / 2 + 100
    val pcts = getBestPct(statSum, 3)
    val reactions = mutableListOf(judgeWeapon(statSum, 3), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun dagger(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 200 - split.first().toInt()
    val strPart = split[4].drop(2).dropLast(3).split(".")
    val str = strPart.first().toInt() * 10 + if (strPart.size == 2) strPart[1].toInt() else 0
    val poisonPart = split[23].drop(2).dropLast(3).split(".")
    val poison = poisonPart.first()
        .toInt() * 100 + if (poisonPart.size == 2) if (poisonPart[1].length == 1) poisonPart[1].toInt() * 10 else poisonPart[1].toInt() else 0
    val statSum = cost + (str - 700) / 3 + (poison - 4000) / 25 + 100
    val r1 = judgeWeapon(statSum, 4)
    val pcts = getBestPct(statSum, 4)
    val reactions = mutableListOf(r1, pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    val r2 = judgeWeapon(statSum - (str - 700) / 3, 3)
    if (r1 != r2) reactions += r2
    return reactions
}

fun wand(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 250 - split.first().toInt()
    val dmgPart = split[4].drop(2).dropLast(3).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val absorbPart = split[21].drop(2).dropLast(3).split(".")
    val absorb = absorbPart.first().toInt() * 10 + if (absorbPart.size == 2) absorbPart[1].toInt() else 0
    val statSum = cost + (dmg - 800) / 2 + (absorb - 200) / 2 + 100

    val pcts = getBestPct(statSum, 4)
    val reactions = mutableListOf(judgeWeapon(statSum, 4), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun fStaff(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 200 - split.first().toInt()
    val dmgPart = split[4].drop(2).dropLast(3).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val burnPart = split[23].drop(2).dropLast(3).split(".")
    val burn = burnPart.first().toInt() * 10 + if (burnPart.size == 2) burnPart[1].toInt() else 0
    val explodePart = split[47].drop(2).dropLast(3).split(".")
    val explode = explodePart.first().toInt() * 10 + if (explodePart.size == 2) explodePart[1].toInt() else 0
    val statSum = cost + (dmg - 600) / 2 + (burn - 200) / 2 + (explode - 400) / 2 + 100
    val pcts = getBestPct(statSum, 5)
    val reactions = mutableListOf(judgeWeapon(statSum, 5), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun eStaff(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 200 - split.first().toInt()
    val dmgPart = split[10].drop(2).dropLast(3).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val statSum = cost + (dmg - 350) / 3 + 100
    val pcts = getBestPct(statSum, 3)
    val reactions = mutableListOf(judgeWeapon(statSum, 3), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun sStaff(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 225 - split.first().toInt()
    val healPart = split[7].drop(2).dropLast(3).split(".")
    val heal = healPart.first().toInt() * 10 + if (healPart.size == 2) healPart[1].toInt() else 0
    val dfUpPart = split[26].drop(2).dropLast(4).split(".")
    val dfUp = dfUpPart.first().toInt() * 10 + if (dfUpPart.size == 2) dfUpPart[1].toInt() else 0
    val statSum = cost + (heal - 300) / 2 + (dfUp - 200) + 100

    val pcts = getBestPct(statSum, 4)
    val reactions = mutableListOf(judgeWeapon(statSum, 4), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun scepter(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = ((200 - split.first().toInt()) * 4 + 2) / 3
    val restorePart = split[4].drop(2).dropLast(3).split(".")
    val restore = restorePart.first().toInt() * 10 + if (restorePart.size == 2) restorePart[1].toInt() else 0
    val statSum = cost + (restore - 400) / 3 + 100

    val r1 = judgeWeapon(statSum, 3)
    val q1 = getBestPct(statSum, 3)
    val reactions = mutableListOf(r1, q1.first)
    if (q1.first != q1.second) reactions += q1.second

    if (cost != 100 && cost % 4 == 0) {
        val q2 = getBestPct(statSum + 1, 3)
        if (q1 != q2) {
            reactions += extraEmoji
        }
    }
    return reactions
}

fun rStaff(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 400 - split.first().toInt()
    val healPart = split[11].drop(2).dropLast(3).split(".")
    val heal = healPart.first().toInt() * 10 + if (healPart.size == 2) healPart[1].toInt() else 0
    val statSum = cost + (heal - 500) / 3 + 100

    val pcts = getBestPct(statSum, 3)
    val reactions = mutableListOf(judgeWeapon(statSum, 3), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun axe(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 220 - split.first().toInt()
    val dmgPart = split[4].drop(2).dropLast(3).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val statSum = cost + (dmg - 500) / 3 + 100
    val pcts = getBestPct(statSum, 3)
    val reactions = mutableListOf(judgeWeapon(statSum, 3), pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
    return reactions
}

fun banner(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = (300 - split.first().toInt()) * 2
    val atk1Part = split[39].drop(2).dropLast(4).split(".")
    val atk1 = atk1Part.first().toInt() * 10 + if (atk1Part.size == 2) atk1Part[1].toInt() else 0
    val atk2Part = split[55].drop(2).dropLast(4).split(".")
    val atk2 = atk2Part.first().toInt() * 10 + if (atk2Part.size == 2) atk2Part[1].toInt() else 0
    val atk3Part = split[71].drop(2).dropLast(4).split(".")
    val atk3 = atk3Part.first().toInt() * 10 + if (atk3Part.size == 2) atk3Part[1].toInt() else 0
    val statSum = cost + (atk1 - 100) + (atk2 - 200) + (atk3 - 300) + 100
    val r1 = judgeWeapon(statSum, 5)
    val q1 = getBestPct(statSum, 5)
    val reactions = mutableListOf(r1, q1.first)
    if (q1.first != q1.second) reactions += q1.second
    if (cost != 100) {
        val q2 = getBestPct(statSum + 1, 5)
        if (q1 != q2) reactions += extraEmoji
    }
    return reactions
}

fun scythe(desc: String): List<ReactionEmoji> {
    val split = desc.split("\n**WP Cost:** ").last().split(Pattern.compile("\\s+"))
    val cost = 200 - split.first().toInt()
    val dmgPart = split[4].drop(2).dropLast(3).split(".")
    val dmg = dmgPart.first().toInt() * 10 + if (dmgPart.size == 2) dmgPart[1].toInt() else 0
    val mortPart = split[29].drop(2).dropLast(3).split(".")
    val mort = mortPart.first().toInt() * 10 + if (mortPart.size == 2) mortPart[1].toInt() else 0
    val statSum = cost + (dmg - 700) / 3 + (mort - 300) / 3 + 100
    val r1 = judgeWeapon(statSum, 4)
    val pcts = getBestPct(statSum, 4)
    val reactions = mutableListOf(r1, pcts.first)
    if (pcts.first != pcts.second) reactions += pcts.second
//    val r2 = judgeWeapon(cost + (mort - 300) / 3 + 100, 3)
//    if (r1 != r2) reactions += r2
    return reactions
}

fun judgeWeapon(statSum: Int, numStats: Int): ReactionEmoji {
    return when (statSum / numStats) {
        100 -> ReactionEmoji.Custom(Snowflake(760023282878513161), "Fabled", true)
        in 95 until 100 -> ReactionEmoji.Custom(Snowflake(760023267326820372), "Legendary", true)
        in 81 until 95 -> ReactionEmoji.Custom(Snowflake(760023247399288843), "mythic", false)
        in 61 until 81 -> ReactionEmoji.Custom(Snowflake(760023233814069288), "epic", false)
        in 41 until 61 -> ReactionEmoji.Custom(Snowflake(760023210166583327), "rare", false)
        in 21 until 41 -> ReactionEmoji.Custom(Snowflake(760023196484239420), "uncommon", false)
        else -> ReactionEmoji.Custom(Snowflake(760023183067316255), "common", false)
    }
//    return when (statSum / numStats) {
//        100 -> ReactionEmoji.Unicode("\ud83c\uddeb")
//        in 95 until 100 -> ReactionEmoji.Unicode("\ud83c\uddf1")
//        in 90 until 95 -> ReactionEmoji.Unicode("\u24c2\ufe0f")
//        in 81 until 90 -> ReactionEmoji.Unicode("\ud83c\uddf2")
//        in 61 until 81 -> ReactionEmoji.Unicode("\ud83c\uddea")
//        in 41 until 61 -> ReactionEmoji.Unicode("\ud83c\uddf7")
//        in 21 until 41 -> ReactionEmoji.Unicode("\ud83c\uddfa")
//        else -> ReactionEmoji.Unicode("\ud83c\udde8")
//    }
}

fun getBestPct(statSum: Int, numStats: Int): Pair<ReactionEmoji, ReactionEmoji> {
    val stat = statSum / numStats
    return if (stat == 100) {
        Pair(ReactionEmoji.Unicode("\ud83d\udcaf"), ReactionEmoji.Unicode("\ud83d\udcaf"))
    } else {
        val tens = qualityEmojis[stat / 10]
        val ones = qualityEmojis[stat % 10]
        if (tens == ones) {
            Pair(tens, ReactionEmoji.Unicode("\u002a\ufe0f\u20e3"))
        } else {
            Pair(tens, ones)
        }
    }
}

fun readShopWeapon(desc: String): List<ReactionEmoji> {
    return when (desc.split("\n").first().drop(10)) {
        "Great Sword" -> sword(desc)
        "Healing Staff" -> hStaff(desc)
        "Bow" -> bow(desc)
        //no rune
        "Defender's Aegis" -> shield(desc)
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
        "Vanguard's Banner" -> banner(desc)
        "Culling Scythe" -> scythe(desc)
        else -> emptyList()
    }
}


