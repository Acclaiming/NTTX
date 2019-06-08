package io.kurumi.ntt.fragment.forum;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.forum.ForumTag;
import java.util.List;

public class ForumTag {
	
	public static Data<ForumTag> data = new Data<ForumTag>(ForumTag.class);
	
	public long id;
	
	public long forumId;
	
	public String name;
	
	public String description;
	
	public List<Long> admins;
	
}
