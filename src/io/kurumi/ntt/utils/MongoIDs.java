package io.kurumi.ntt.utils;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import io.kurumi.ntt.db.AbsData;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.inc;

public class MongoIDs {

    public static AbsData<String, IdSeq> ids = new AbsData<String, IdSeq>(IdSeq.class);

    public static long getNextId(final String collection) {

        if (ids.containsId(collection)) {

            return ids.collection.findOneAndUpdate(eq("_id", collection), inc("seq", 1L), new FindOneAndUpdateOptions().upsert(true)).seq;

        } else {

            ids.setById(collection, new IdSeq() {{
                id = collection;
                seq = 1L;
            }});

            return 1L;

        }

    }

    public static class IdSeq {

        public String id;
        public Long seq;

    }

}
