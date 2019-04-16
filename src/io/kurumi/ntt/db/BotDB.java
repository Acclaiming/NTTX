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
import java.util.HashMap;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.FindIterable;
import io.kurumi.ntt.Launcher;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.response.GetChatResponse;

public class BotDB {

    public static MongoClient client;
    public static MongoDatabase db;

    public static void init(String address,int port) throws MongoException {

        client = new MongoClient(address,port);

        CodecRegistry registry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        db = client.getDatabase("NTTools").withCodecRegistry(registry);

        userDataCollection = db.getCollection("UserData",UserData.class);

        statusArchiveCollection = db.getCollection("StatusArchive",StatusArchive.class);

        userArchiveCollection = db.getCollection("UserArchive",UserArchive.class);

        //  statusArchiveCollection.createIndex(Indexes.compoundIndex();

    }

    public static MongoCollection<UserData> userDataCollection;

    public static HashMap<Long,UserData> userDataIndex = new HashMap<>();

    public static FindIterable<UserData> userDataIterable() {

        return userDataCollection.find();

    }

    public static UserData getUserData(String userName) {

        if (userDataCollection.countDocuments(eq("userName",userName)) > 0) {

            return userDataCollection.find(eq("userName",userName)).first();

        }

        return null;

    }

    public static UserData getUserData(long userId) {

        if (userDataIndex.containsKey(userId)) return userDataIndex.get(userId);

        synchronized (userDataIndex) {

            if (userDataIndex.containsKey(userId)) return userDataIndex.get(userId);

            if (userDataCollection.countDocuments(eq("_id",userId)) > 0) {

                UserData user =  userDataCollection.find(eq("_id",userId)).first();

                userDataIndex.put(userId,user);

            }

        }

        return null;

    }

    public static UserData getUserData(com.pengrad.telegrambot.model.User user) {

        if (user == null) return null;

        UserData userData = getUserData(user.id().longValue());

        if (userData == null) {

            synchronized (userDataIndex) {

                if (userDataIndex.containsKey(user.id().longValue())) return userDataIndex.get(user.id().longValue());

                userData = new UserData();
                userData.id = user.id().longValue();
                userData.read(user);

                userDataIndex.put(user.id().longValue(),userData);

                userDataCollection.insertOne(userData);

            }

        } else {

            userData.read(user);

        }

        return userData;

    }

    public static MongoCollection<StatusArchive> statusArchiveCollection;

    public static boolean statusExists(long id) {

        return statusArchiveCollection.countDocuments(eq("_id",id)) == 1;

    }

    public static StatusArchive getStatus(long id) {

        if (!statusExists(id)) return null;

        return statusArchiveCollection.find(eq("_id",id)).first();

    }

    public static synchronized StatusArchive saveStatus(Status status) {

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

    public static synchronized UserArchive saveUser(User user) {

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

