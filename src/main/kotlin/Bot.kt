import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.WriteConcern
import dev.kord.common.ratelimit.BucketRateLimiter
import dev.kord.core.Kord
import dev.kord.gateway.DefaultGateway
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.litote.kmongo.*
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

val MONGO_CONNECT_URI =
    ConnectionString("mongodb+srv://${System.getenv("db-user")}:${System.getenv("db-pass")}@${System.getenv("db-address")}/Hakibot")


@ExperimentalTime
@ObsoleteCoroutinesApi
suspend fun main() {
    val discordClient = Kord(System.getenv("hakibot-token")) {
        this.sharding { recommended ->
            println(recommended)
            0..0
        }
    }
    val mongoSettings =
        MongoClientSettings.builder().applyConnectionString(MONGO_CONNECT_URI).writeConcern(WriteConcern.MAJORITY)
            .retryWrites(true).build()
    val mongoClient = KMongo.createClient(mongoSettings)
    val db = mongoClient.getDatabase("Hakibot")
    val botClient = Hakibot(discordClient, db)
    botClient.startUp()
}