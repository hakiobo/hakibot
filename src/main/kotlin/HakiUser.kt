data class HakiUser(val _id: String, val owoSettings: OWOSettings = OWOSettings()) {
}


data class OWOSettings(val huntRemind: Boolean = false, val huntCD: Boolean = false, val prayRemind: Boolean = false, val prayCD: Boolean = false)