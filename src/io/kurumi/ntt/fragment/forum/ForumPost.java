package io.kurumi.ntt.fragment.forum;

import io.kurumi.ntt.db.Data;
import java.util.List;

public class ForumPost {
	
	public static Data<ForumPost> data = new Data<ForumPost>(ForumPost.class);
	
	public long id;
	public long forumId;
	public long tagId;
	
	public long createAt;
	
	public long quoted;
	
	public long from;
	
	public String title;
	public String text;
	
	public boolean essential = false;
	
	public int mediaType = 0;
	public List<String> medias;
	
}
