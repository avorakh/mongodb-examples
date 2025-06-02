package dev.avorakh.tip.mongodb.crud;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class MongoCrudOps {
    private static final Logger LOGGER = Logger.getLogger(MongoCrudOps.class.getName());
    private static final String URI_PROPERTY_KEY = "mongodb.uri";

    private static final String BANK_DB_NAME = "bank";
    private static final String COLLECTION_NAME = "accounts";

    private final MongoCollection<Document> collection;

    public MongoCrudOps(MongoClient client, String dbName, String collectionName) {
        MongoDatabase database = client.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

    public ObjectId crete(Document document) {
        InsertOneResult result = collection.insertOne(document);
        return result.getInsertedId().asObjectId().getValue();
    }

    public List<ObjectId> create(List<Document> documents) {
        var result = collection.insertMany(documents);
        var insertedIds = new ArrayList<ObjectId>();
        result.getInsertedIds()
                .forEach((key, value) -> insertedIds.add(value.asObjectId().getValue()));
        return insertedIds;
    }

    public List<Document> findAll(Bson query) {
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            List<Document> foundDocuments = new ArrayList<>(cursor.available());
            while (cursor.hasNext()) {
                foundDocuments.add(cursor.next());
            }
            return foundDocuments;
        }
    }

    public Optional<Document> find(Bson query) {
        return Optional.ofNullable(collection.find(query).first());
    }

    public boolean update(Bson query, Bson updates) {
        UpdateResult upResult = collection.updateOne(query, updates);
        long modifiedCount = upResult.getModifiedCount();
        return modifiedCount == 1;
    }

    public long updateAll(Bson query, Bson updates) {
        UpdateResult upResult = collection.updateMany(query, updates);
        return upResult.getModifiedCount();
    }

    public boolean delete(Bson query) {
        DeleteResult delResult = collection.deleteOne(query);
        long deletedCount = delResult.getDeletedCount();
        return deletedCount == 1;
    }

    public long deleteAll(Bson query) {
        DeleteResult delResult = collection.deleteOne(query);
        return delResult.getDeletedCount();
    }

    public static void main(String[] args) {
        try (var client = MongoClients.create(
                Optional.ofNullable(System.getProperty(URI_PROPERTY_KEY)).orElseThrow())) {

            // Insert a Document
            var sampleTrainingCrudOps = new MongoCrudOps(client, "sample_training", "inspections");

            var inspection = new Document("_id", new ObjectId())
                    .append("id", "10021-2015-ENFO")
                    .append("certificate_number", 9278806)
                    .append("business_name", "ATLIXCO DELI GROCERY INC.")
                    .append(
                            "date",
                            Date.from(LocalDate.of(2025, 2, 20)
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()))
                    .append("result", "No Violation Issued")
                    .append("sector", "Cigarette Retail Dealer - 127")
                    .append(
                            "address",
                            new Document()
                                    .append("city", "RIDGEWOOD")
                                    .append("zip", 11385)
                                    .append("street", "MENAHAN ST")
                                    .append("number", 1712));
            var inspectionId = sampleTrainingCrudOps.crete(inspection);

            LOGGER.info("✅ Inserted inspection: " + inspectionId);

            // Insert Multiple Documents
            var bankAccountsCrudOps = new MongoCrudOps(client, BANK_DB_NAME, COLLECTION_NAME);

            var doc1 = new Document()
                    .append("account_holder", "john doe")
                    .append("account_id", "MDB99115881")
                    .append("balance", 1785)
                    .append("account_type", "checking");

            var doc2 = new Document()
                    .append("account_holder", "jane doe")
                    .append("account_id", "MDB79101843")
                    .append("balance", 1468)
                    .append("account_type", "checking");

            var newAccounts = List.of(doc1, doc2);
            var createdAccountIds = bankAccountsCrudOps.create(newAccounts);
            LOGGER.info("✅ Inserted accounts: " + createdAccountIds);

            // Querying a MongoDB Collection
            Bson filter = and(gte("balance", 1000), eq("account_type", "checking"));
            var foundAccounts = bankAccountsCrudOps.findAll(filter);
            LOGGER.info("✅ Found accounts: " + foundAccounts);

            var foundFirstAccount = bankAccountsCrudOps.find(filter);
            LOGGER.info("✅ Found first accounts: "
                    + (foundFirstAccount.isPresent() ? foundFirstAccount.get() : "NOT FOUND"));

            // Uodates
            Bson updateQuery = Filters.eq("account_id", "MDB79101843");
            Bson updatesforOne = Updates.combine(Updates.set("account_status", "active"), Updates.inc("balance", 100));

            boolean isUpdatedForOne = bankAccountsCrudOps.update(updateQuery, updatesforOne);
            LOGGER.info("✅ Account: " + updateQuery + " was updated - " + isUpdatedForOne);

            Bson queryForMany = Filters.eq("account_type", "savings");
            Bson updatesForMany = Updates.combine(Updates.set("minimum_balance", 100));
            long updatesCount = bankAccountsCrudOps.updateAll(queryForMany, updatesForMany);
            LOGGER.info("✅ Updated accounts: " + updatesCount);

            // Deleting Documents
            // Using deleteOne()
            Bson deleteQuery = Filters.eq("account_holder", "john doe");
            boolean isDeletedForOne = bankAccountsCrudOps.delete(deleteQuery);
            LOGGER.info("✅ Account: " + deleteQuery + " was deleted - " + isDeletedForOne);

            // Using deleteMany() with a Query Object
            Bson queryManyForQueryObject = eq("account_status", "dormant");
            long deletedCount = bankAccountsCrudOps.deleteAll(queryManyForQueryObject);
            LOGGER.info("✅ Deleted accounts  with a Query Object: " + deletedCount);

            // Using deleteMany() with a Query Filter
            long deletedCountForFilter = bankAccountsCrudOps.deleteAll(Filters.eq("account_status", "dormant"));
            LOGGER.info("✅ Deleted accounts with a Query Filter: " + deletedCountForFilter);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "❌ MongoDB operation failed", e);
        }
    }
}
