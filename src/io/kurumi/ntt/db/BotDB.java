package io.kurumi.ntt.db;

import com.mongodb.*;
import com.mongodb.client.*;

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

    public static void init(String address, int port) throws MongoException {

        client = new MongoClient(address, port);

        CodecRegistry registry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        db = client.getDatabase("NTTools").withCodecRegistry(registry);


    }


}

