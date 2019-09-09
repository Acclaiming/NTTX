package io.kurumi.ntt.db;


import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class AbsData<ID, T> {

    static final String FIELD_ID = "_id";
    public MongoCollection<T> collection;

    public AbsData(Class<T> clazz) {

        this(clazz.getSimpleName(), clazz);

    }

    public AbsData(String collectionName, Class<T> clazz) {

        collection = BotDB.db.getCollection(collectionName, clazz);

    }

    public boolean containsId(ID id) {

        synchronized (this) {

            return collection.countDocuments(eq(FIELD_ID, id)) > 0;

        }

    }

    public List<T> getAll() {

        LinkedList<T> all = new LinkedList<>();

        for (T t : collection.find()) {

            all.add(t);

        }

        return all;

    }

    public T getById(ID id) {

        return collection.find(eq(FIELD_ID, id)).first();

    }

    public long countByField(String field, Object value) {

        return collection.countDocuments(eq(field, value));

    }

    public boolean fieldEquals(ID id, String field, Object value) {

        return collection.countDocuments(and(eq(FIELD_ID, id), eq(field, value))) > 0;

    }

    public FindIterable<T> findByField(String field, Object value) {

        return collection.find(eq(field, value));

    }

    public List<T> getAllByField(String field, Object value) {

        LinkedList<T> all = new LinkedList<>();

        for (T t : findByField(field, value)) {

            all.add(t);

        }

        return all;

    }


    public T getByField(String field, Object value) {

        return findByField(field, value).first();

    }

    public T setById(ID id, T object) {

        if (containsId(id)) {

            collection.replaceOne(eq(FIELD_ID, id), object);

        } else {

            synchronized (this) {

                collection.insertOne(object);

            }

        }

        return object;

    }

    public boolean deleteById(ID id) {

        synchronized (this) {

            return collection.deleteOne(eq("_id", id)).getDeletedCount() > 0;

        }

    }

    public boolean deleteByField(String field, Object value) {

        synchronized (this) {

            return collection.deleteOne(eq(field, value)).getDeletedCount() > 0;

        }

    }


}
