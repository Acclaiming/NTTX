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

        followersCollection = db.getCollection("Followers",IdsList.class);
        friendsCollection = db.getCollection("Friends",IdsList.class);

        // statusArchiveCollection.createIndex(Indexes.compoundIndex();

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

        IdsList index = followersCollection.find(eq("_id",accountId)).first();

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

        IdsList index = friendsCollection.find(eq("_id",accountId)).first();

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

