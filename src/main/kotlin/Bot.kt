import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.WriteConcern
import dev.kord.core.Kord
import org.litote.kmongo.*

val MONGO_CONNECT_URI =
    ConnectionString("mongodb+srv://${System.getenv("db-user")}:${System.getenv("db-pass")}@${System.getenv("db-address")}/Hakibot")


suspend fun main() {
    val discordClient = Kord(System.getenv("hakibot-token"))
    val mongoSettings =
        MongoClientSettings.builder().applyConnectionString(MONGO_CONNECT_URI).writeConcern(WriteConcern.MAJORITY)
            .retryWrites(true).build()
    val mongoClient = KMongo.createClient(mongoSettings)
    val db = mongoClient.getDatabase("Hakibot")
    val botClient = Hakibot(discordClient, db)
    botClient.startUp()
}