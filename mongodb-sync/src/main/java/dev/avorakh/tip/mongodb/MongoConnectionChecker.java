package dev.avorakh.tip.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

public class MongoConnectionChecker {

    private static final Logger LOGGER = Logger.getLogger(MongoConnectionChecker.class.getName());
    private static final String URI_PROPERTY_KEY = "mongodb.uri";

    private final String mongoUri;

    public MongoConnectionChecker(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    public void verifyConnection() {
        try (MongoClient client = createClient()) {
            MongoDatabase adminDb = client.getDatabase("admin");
            adminDb.runCommand(new Document("ping", 1));
            LOGGER.info("✅ Successfully connected to MongoDB at: " + mongoUri);
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "❌ MongoDB connection failed at " + mongoUri, ex);
        }
    }

    private MongoClient createClient() {
        var settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoUri))
                .build();
        return MongoClients.create(settings);
    }

    public static void main(String[] args) {
        var uri = Optional.ofNullable(System.getProperty(URI_PROPERTY_KEY)).orElseThrow();
        var checker = new MongoConnectionChecker(uri);
        checker.verifyConnection();
    }
}
