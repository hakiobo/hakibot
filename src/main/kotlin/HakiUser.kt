data class HakiUser(val _id: String, val owoSettings: OWOSettings = OWOSettings(), val owoCount: UserOWOCount = UserOWOCount()) {
}

data class UserOWOCount(val count: Int = 0, val lastOwO: Long = 0)


data class OWOSettings(val huntRemind: Boolean = false, val huntCD: Boolean = false, val prayRemind: Boolean = false, val prayCD: Boolean = false)