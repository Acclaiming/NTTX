package io.kurumi.ntt.fragment.sticker;

import io.kurumi.ntt.db.AbsData;

public class PackOwner {
	
	public static AbsData<String,PackOwner> data = new AbsData<String,PackOwner>(PackOwner.class);
	
	public String id;
	public long owner;
	public String title;
	
}
