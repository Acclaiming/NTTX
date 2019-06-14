package io.kurumi.ntt.fragment.forum;

import io.kurumi.ntt.db.Data;

public class ForumReply {

    public static Data<ForumReply> data = new Data<ForumReply>(ForumReply.class);

    public long id;

    public long createAt;

    public long from;

    public long postId;

    public long inReplyTo;

    public String text;

}
