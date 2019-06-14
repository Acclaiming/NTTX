package io.kurumi.ntt.db;

import com.mongodb.client.*;

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

import java.util.*;

public class Data<T> extends AbsData<Long, T> {

    public Data(Class<T> clazz) {

        super(clazz);

    }

    public Data(String collectionName, Class<T> clazz) {

        super(collectionName, clazz);

    }

}
