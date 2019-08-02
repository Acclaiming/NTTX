package io.kurumi.ntt.fragment.mstd;

import io.kurumi.ntt.db.Data;

public class MstdAuth {
	
	public static Data<MstdAuth> data = new Data<>(MstdAuth.class);
	
	public Long id;
	
	public String appId;
	
	public String accessToken;
	
	public MstdApi createApi() {
		
		return new MstdApi(this);
		
	}
	
}
