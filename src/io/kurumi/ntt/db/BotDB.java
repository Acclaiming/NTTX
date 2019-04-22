package io.kurumi.ntt.db;

import com.mongodb.*;
import com.mongodb.client.*;
import io.kurumi.ntt.twitter.archive.*;
import java.util.*;
import org.bson.codecs.configuration.*;
import org.bson.codecs.pojo.*;
import twitter4j.*;

import com.mongodb.MongoClient;

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
import io.kurumi.ntt.db.BotDB.*;

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

        followersCollection = db.getCollection("Followers",IdsList.class);
        friendsCollection = db.getCollection("Friends",IdsList.class);
        
        // statusArchiveCollection.createIndex(Indexes.compoundIndex();

    }

    public static MongoCollection<UserData> userDataCollection;

    public static HashMap<Long,UserData> userDataIndex = new HashMap<>();

    public static FindIterable<UserData> userDataIterable() {

        return userDataCollection.find();

    }

    public static UserData getUserData(String userName) {

        return userDataCollection.find(eq("userName",userName)).iterator().tryNext();

    }

    public static UserData getUserData(long userId) {

        if (userDataIndex.containsKey(userId)) return userDataIndex.get(userId);

        synchronized (userDataIndex) {

            if (userDataIndex.containsKey(userId)) return userDataIndex.get(userId);

            UserData user =  userDataCollection.find(eq("_id",userId)).iterator().tryNext();

            if (user != null) return null;

            userDataIndex.put(userId,user);

            return user;

        }
        
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

        return statusArchiveCollection.find(eq("_id",id)).iterator().tryNext();

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

        return userArchiveCollection.find(eq("_id",id)).iterator().tryNext();

    }

    public static UserArchive getUser(String screenName) {

        return userArchiveCollection.find(eq("screenName",screenName)).iterator().tryNext();

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

    public static class IdsList {

        public Long id;
        public List<Long> ids;

        public IdsList() {}

        public IdsList(Long id,List<Long> ids) {

            this.id = id;
            this.ids = ids;

        }

    }

    public static MongoCollection<IdsList> followersCollection;
    public static MongoCollection<IdsList> friendsCollection;

    public static boolean isFollower(long accountId,Long target) {

        return followersCollection.count(and(eq("_id",accountId),eq("ids",target))) > 1;

    }
    
    public static List<Long> getOriginFollowers(long accountId) {

        FindIterable<IdsList> followers = friendsCollection.find(eq("ids",accountId));

        LinkedList<Long> ids = new LinkedList<>();

        for (IdsList follower : followers) {

            ids.add(follower.id);

        }

        return ids;

    }
    

    public static List<Long> getFollowers(long accountId) {

        IdsList index = followersCollection.find(eq("_id",accountId)).iterator().tryNext();

        if (index == null) return null;
        
        return index.ids;
        
    }

    public static void saveFollowers(long id,List<Long> ids) {

        if (followersCollection.count(eq("_id",id)) > 0) {

            followersCollection.replaceOne(eq("_id",id),new IdsList(id,ids));

        } else {

            followersCollection.insertOne(new IdsList(id,ids));

        }

    }

    public static boolean isFriend(long accountId,Long target) {

        return friendsCollection.count(and(eq("_id",accountId),eq("ids",target))) > 1;

    }
    
    public static List<Long> getOriginFriends(long accountId) {

        FindIterable<IdsList> friends = followersCollection.find(eq("ids",accountId));

        LinkedList<Long> ids = new LinkedList<>();
        
        for (IdsList following : friends) {
            
            ids.add(following.id);
            
        }
        
        return ids;
        
    }
    
    public static List<Long> getFriends(long accountId) {

        IdsList index = friendsCollection.find(eq("_id",accountId)).iterator().tryNext();

        if (index == null) return null;

        return index.ids;

    }

    public static void saveFriends(long id,List<Long> ids) {

        if (friendsCollection.count(eq("_id",id)) > 0) {

            friendsCollection.replaceOne(eq("_id",id),new IdsList(id,ids));

        } else {

            friendsCollection.insertOne(new IdsList(id,ids));

        }

    }

}

