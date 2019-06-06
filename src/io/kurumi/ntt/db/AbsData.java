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
public class AbsData<ID,T> {

	public MongoCollection<T> collection;

    static final String FIELD_ID = "_id";

    public AbsData(Class<T> clazz) {

        this(clazz.getSimpleName(),clazz);

    }

    public AbsData(String collectionName,Class<T> clazz) {

        collection = BotDB.db.getCollection(collectionName,clazz);

    }

    public boolean containsId(ID id) {

		synchronized (this) {

			return collection.countDocuments(eq(FIELD_ID,id)) > 0;

		}

    }

    public T getById(ID id) {

        return collection.find(eq(FIELD_ID,id)).first();

    }

    public long countByField(String field,Object value) {

        return collection.countDocuments(eq(field,value));

    }

	public boolean fieldEquals(ID id,String field,Object value) {

        return collection.countDocuments(and(eq(FIELD_ID,id),eq(field,value))) > 0;

    }

    public FindIterable<T> findByField(String field,Object value) {

        return collection.find(eq(field,value));

    }

    public T getByField(String field,Object value) {

        return findByField(field,value).first();

    }

    public T setById(ID id,T object) {

        if (containsId(id)) {

            collection.replaceOne(eq(FIELD_ID,id),object);

        } else {

			synchronized (this) {

				collection.insertOne(object);

			}

        }

        return object;

    }

    public boolean deleteById(ID id) {

		synchronized (this) {

			return collection.deleteOne(eq("_id",id)).getDeletedCount() > 0;

		}

    }

	public boolean deleteByField(String field,Object value) {

		synchronized (this) {

			return collection.deleteOne(eq(field,value)).getDeletedCount() > 1;

		}

    }


}
