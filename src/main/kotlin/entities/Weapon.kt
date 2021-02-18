package entities

data class Weapon(val _id: String, val type: String) {
    companion object{
        const val DB_NAME = "weapons"
    }
}
