package io.kurumi.ntt.fragment.mstd;

import io.kurumi.ntt.db.AbsData;

public class MstdApp {
	
	public static AbsData<String,MstdApp> data = new AbsData<>(MstdApp.class);
	
	public String id;
	
	public long appId;
	
	public String clientId;
	
	public String clientSecret;
	
	public MstdApi createApi() {

		return new MstdApi(this);

	}
	
}
