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

public class Data<T> {

    public MongoCollection<T> collection;

    static final String FIELD_ID = "_id";

    public Data(Class<T> clazz) {

        this(clazz.getSimpleName(),clazz);

    }
    
    public Data(String collectionName,Class<T> clazz) {

        collection = BotDB.db.getCollection(collectionName,clazz);

    }

    public boolean containsId(Long id) {

        return collection.count(eq(FIELD_ID,id)) > 0;

    }

    public T getById(Long id) {

        return collection.find(eq(FIELD_ID,id)).first();

    }
    
    public long countByField(String field,Object value) {
        
        return collection.countDocuments(eq(field,value));
        
    }

    public T getByField(String field,Object value) {

        return collection.find(eq(field,value)).first();

    }

    public T setById(Long id,T object) {

        if (containsId(id)) {

            collection.replaceOne(eq(FIELD_ID,id),object);

        } else {

            collection.insertOne(object);

        }

        return object;

    }

}
