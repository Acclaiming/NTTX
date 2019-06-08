package io.kurumi.ntt.fragment.forum;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.forum.ForumE;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;

public class ForumE {
	
	public static Data<ForumE> data = new Data<ForumE>(ForumE.class);
	
	public long id;
	public long owner;
	
	// 记录
	
	public String token;
	public long channel;
	
	public int titleId = -1;
	
	public String error;
	
	// 信息
	
	public String name;
	
	public String description = "一个电报论坛";
	
	// 设置
	
	public long postLevel = 0;
	public long replyLevel = 0;
	
	public List<Long> admins = new LinkedList<>();
	public boolean antiEsu = false;
	
}
