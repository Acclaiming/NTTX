package io.kurumi.ntt.fragment.forum;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.forum.ForumTag;

import java.util.List;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

public class ForumTag {

    public static Data<ForumTag> data = new Data<ForumTag>(ForumTag.class);
    public long id;
    public long forumId;
    public String name;
    public String description;

    public static boolean tagExists(long forumId, String tagName) {

        return data.collection.countDocuments(and(eq("forumId", forumId), eq("name", tagName))) > 0;

    }

}
