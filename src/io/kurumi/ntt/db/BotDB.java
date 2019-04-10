package io.kurumi.ntt.db;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.kurumi.ntt.twitter.archive.StatusArchive;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import twitter4j.Status;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import java.util.LinkedList;
import com.mongodb.client.model.FindOptions;
import io.kurumi.ntt.twitter.archive.UserArchive;
import twitter4j.User;

public class BotDB {

    public static MongoClient client;
    public static MongoDatabase db;

    public static void init(String address,int port) throws MongoException {

        client = new MongoClient(address,port);

        CodecRegistry registry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        db = client.getDatabase("NTTools").withCodecRegistry(registry);

        statusArchiveCollection = db.getCollection("STATUS",StatusArchive.class);
       
        userArchiveCollection = db.getCollection("USER",UserArchive.class);
        
        //  statusArchiveCollection.createIndex(Indexes.compoundIndex();

    }

    public static MongoCollection<StatusArchive> statusArchiveCollection;

    public static boolean statusExists(long id) {

        return statusArchiveCollection.countDocuments(eq("_id",id)) == 1;

    }

    public static StatusArchive getStatus(long id) {

        if (!statusExists(id)) return null;

        return statusArchiveCollection.find(eq("_id",id)).first();

    }

    public static StatusArchive saveStatus(Status status) {

        if (statusExists(status.getId())) return getStatus(status.getId());

        StatusArchive archive = new StatusArchive();

        archive.id = status.getId();

        archive.read(status);

        statusArchiveCollection.insertOne(archive);

        return archive;

    }

    public static MongoCollection<UserArchive> userArchiveCollection;

    public static boolean userExists(long id) {

        return userArchiveCollection.countDocuments(eq("_id",id)) > 0;

    }
    
    public static boolean userExists(String screenName) {

        return userArchiveCollection.countDocuments(eq("screenName",screenName)) > 0;

    }

    public static UserArchive getUser(long id) {

        if (!userExists(id)) return null;

        return userArchiveCollection.find(eq("_id",id)).first();

    }
    
    public static UserArchive getUser(String screenName) {

        if (!userExists(screenName)) return null;

        return userArchiveCollection.find(eq("screenName",screenName)).first();

    }

    public static UserArchive saveUser(User user) {

        UserArchive archive;

        if (userExists(user.getId())) {

            archive = getUser(user.getId());

            if (archive.read(user)) {

                userArchiveCollection.replaceOne(eq("_id",user.getId()),archive);

            }

        } else {

            archive = new UserArchive();
            
            archive.isDisappeared = false;

            archive.id = user.getId();

            archive.read(user);

            userArchiveCollection.insertOne(archive);

        }

        return archive;

    }

    public static void saveUserDisappeared(Long da) {

        UserArchive user = getUser(da);

        if (user != null) {

            user.isDisappeared = true;

            userArchiveCollection.replaceOne(eq("_id",da),user);

        }

    }

}

