package entities

data class HakiUser(
    val _id: String,
    val username: String? = null,
    val owoSettings: OWOSettings = OWOSettings(),
    val owoCount: UserOWOCount = UserOWOCount()
){
    companion object {
        const val DB_NAME = "users"
    }
}

data class UserOWOCount(val count: Int = 0, val lastOwO: Long = 0)


data class OWOSettings(
    val huntRemind: Boolean = false,
    val huntCD: Boolean = false,
    val lastHunt: Long = 0L,
    val prayRemind: Boolean = false,
    val prayCD: Boolean = false,
    val lastPray: Long = 0L,
)