package dev.avorakh.tip.mongodb;

import static com.mongodb.client.model.Accumulators.avg;
import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Sorts.descending;
import static com.mongodb.client.model.Sorts.orderBy;
import static java.util.Arrays.asList;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

public class MongoConnectionAggregator {

    private static final Logger LOGGER = Logger.getLogger(MongoConnectionAggregator.class.getName());
    private static final String URI_PROPERTY_KEY = "mongodb.uri";

    private final String mongoUri;

    public MongoConnectionAggregator(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    public void matchStage() {
        try (MongoClient client = createClient()) {
            MongoDatabase db = client.getDatabase("bank");
            MongoCollection<Document> accounts = db.getCollection("accounts");
            matchStage(accounts);
            LOGGER.info("✅ Successfully aggregated to MongoDB at: " + mongoUri);
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "❌ MongoDB connection failed at " + mongoUri, ex);
        }
    }

    public void matchAndGroupStages() {
        try (MongoClient client = createClient()) {
            MongoDatabase db = client.getDatabase("bank");
            MongoCollection<Document> accounts = db.getCollection("accounts");
            matchAndGroupStages(accounts);
            LOGGER.info("✅ Successfully aggregated (match and group) to MongoDB at: " + mongoUri);
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "❌ MongoDB connection failed at " + mongoUri, ex);
        }
    }

    public void showAccountTypeSummary() {
        try (MongoClient client = createClient()) {
            MongoDatabase db = client.getDatabase("bank");
            MongoCollection<Document> accounts = db.getCollection("accounts");
            showAccountTypeSummary(accounts);
            LOGGER.info("✅ Successfully aggregated (match and group) to MongoDB at: " + mongoUri);
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "❌ MongoDB connection failed at " + mongoUri, ex);
        }
    }

    public void matchSortAndProjectStages() {
        try (MongoClient client = createClient()) {
            MongoDatabase db = client.getDatabase("bank");
            MongoCollection<Document> accounts = db.getCollection("accounts");
            matchSortAndProjectStages(accounts);
            LOGGER.info("✅ Successfully aggregated (match sort and project) to MongoDB at: " + mongoUri);
        } catch (MongoException ex) {
            LOGGER.log(Level.SEVERE, "❌ MongoDB connection failed at " + mongoUri, ex);
        }
    }

    public void showGBPBalancesForCheckingAccounts() {
        try (MongoClient client = createClient()) {
            MongoDatabase db = client.getDatabase("bank");
            MongoCollection<Document> accounts = db.getCollection("accounts");
            showGBPBalancesForCheckingAccounts(accounts);
            LOGGER.info("✅ Successfully aggregated (match sort and project) to MongoDB at: " + mongoUri);
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
        var aggregator = new MongoConnectionAggregator(uri);
        aggregator.matchStage();
        aggregator.matchAndGroupStages();
        aggregator.showAccountTypeSummary();
        aggregator.matchSortAndProjectStages();
        aggregator.showGBPBalancesForCheckingAccounts();
    }

    private static void matchStage(MongoCollection<Document> accounts) {
        Bson matchStage = Aggregates.match(eq("account_id", "MDB79101843"));
        LOGGER.info("Display aggregation results");
        accounts.aggregate(asList(matchStage)).forEach(document -> LOGGER.info(document.toJson()));
        LOGGER.info("End aggregation results");
    }

    private static void matchAndGroupStages(MongoCollection<Document> accounts) {
        Bson matchStage = Aggregates.match(eq("account_id", "MDB79101843"));
        Bson groupStage =
                Aggregates.group("$account_type", sum("total_balance", "$balance"), avg("average_balance", "$balance"));
        LOGGER.info("Display aggregation results");
        accounts.aggregate(asList(matchStage, groupStage)).forEach(document -> {
            LOGGER.info(document.toJson());
        });
        LOGGER.info("End aggregation results");
    }

    public void showAccountTypeSummary(MongoCollection<Document> accounts) {
        Bson matchStage = Aggregates.match(gt("balance", 1000));
        Bson groupStage =
                Aggregates.group("$account_type", sum("total_balance", "$balance"), avg("average_balance", "$balance"));
        LOGGER.info("Display aggregation results");
        accounts.aggregate(asList(matchStage, groupStage)).forEach(document -> LOGGER.info(document.toJson()));
        LOGGER.info("End aggregation results");
    }

    private static void matchSortAndProjectStages(MongoCollection<Document> accounts) {
        Bson matchStage = Aggregates.match(and(Filters.gt("balance", 1500), eq("account_type", "checking")));
        Bson sortStage = Aggregates.sort(orderBy(descending("balance")));
        Bson projectStage = Aggregates.project(fields(
                include("account_id", "account_type", "balance"),
                Projections.computed("euro_balance", new Document("$divide", asList("$balance", 1.20F))),
                excludeId()));
        LOGGER.info("Display aggregation results");
        accounts.aggregate(asList(matchStage, sortStage, projectStage))
                .forEach(document -> LOGGER.info(document.toJson()));
        LOGGER.info("End aggregation results");
    }

    public void showGBPBalancesForCheckingAccounts(MongoCollection<Document> accounts) {
        Bson matchStage = Aggregates.match(and(eq("account_type", "checking"), gt("balance", 1500)));
        Bson sortStage = Aggregates.sort(orderBy(descending("balance")));
        Bson projectStage = Aggregates.project(fields(include("account_id", "account_type", "balance"), excludeId()));
        LOGGER.info("Display aggregation results");
        accounts.aggregate(asList(matchStage, sortStage, projectStage))
                .forEach(document -> LOGGER.info(document.toJson()));
        LOGGER.info("End aggregation results");
    }
}
