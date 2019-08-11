package io.kurumi.ntt.fragment.twitter.spam;

import java.util.List;
import java.util.HashMap;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.AbsData;

public class SpamTag {
	
	public static AbsData<String,SpamTag> data = new AbsData<>(SpamTag.class);
	
	public String id;
	
	public String description;
	
	public HashMap<String,Integer> records;
	
	public List<Long> subscribers;
	
}
