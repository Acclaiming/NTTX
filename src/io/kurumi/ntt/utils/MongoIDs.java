package io.kurumi.ntt.utils;

import io.kurumi.ntt.db.*;

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
import com.mongodb.operation.*;
import com.mongodb.client.model.*;
import com.mongodb.*;
import org.bson.*;

public class MongoIDs {

	public static class IdSeq {

		public String id;
		public Long seq;

	}

	public static AbsData<String,IdSeq> ids = new AbsData<String,IdSeq>(IdSeq.class);

	public static long getNextId(final String collection) {

		if (ids.containsId(collection)) {

			return ids.collection.findOneAndUpdate(eq("_id",collection),inc("seq",1L),new FindOneAndUpdateOptions().upsert(true)).seq;

		} else {

			ids.setById(collection,new IdSeq() {{ id = collection; seq = 1L; }});

			return 1L;

		}

	}

}
