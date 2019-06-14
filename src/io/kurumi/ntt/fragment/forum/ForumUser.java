package io.kurumi.ntt.fragment.forum;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.forum.ForumUser;

public class ForumUser {

    public static Data<ForumUser> data = new Data<ForumUser>(ForumUser.class);

    public long id;
    public long forumId;

    public long experience = 0;

    public boolean banned = false;

}
