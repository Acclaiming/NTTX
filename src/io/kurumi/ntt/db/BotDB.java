package io.kurumi.ntt.db;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

public class BotDB {

    public static MongoClient client;
    public static MongoDatabase db;

    public static void init(String address, int port) throws MongoException {

        client = MongoClients.create("mongodb://" + address + ":" + port);

        CodecRegistry registry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        db = client.getDatabase("NTTools").withCodecRegistry(registry);

        db.listCollectionNames();

    }


}

