import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.WriteConcern
import dev.kord.core.Kord
import dev.kord.core.builder.kord.Shards
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import kotlin.time.ExperimentalTime

val MONGO_CONNECT_URI =
    ConnectionString("mongodb+srv://${System.getenv("db-user")}:${System.getenv("db-pass")}@${System.getenv("db-address")}/Hakibot")


suspend fun main() {
    val discordClient = Kord(System.getenv("hikabot-token")) {
//        this.sharding { recommended ->
//            println(recommended)
//            Shards(2)
//        }
    }
    val mongoSettings =
        MongoClientSettings.builder().applyConnectionString(MONGO_CONNECT_URI).writeConcern(WriteConcern.MAJORITY)
            .retryWrites(true).build()
    val mongoClient = KMongo.createClient(mongoSettings).coroutine
    val db = mongoClient.getDatabase("Hakibot")
    val botClient = Hakibot(discordClient, db)
    botClient.startUp()
}