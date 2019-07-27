package io.kurumi.ntt.fragment.wechet;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.wechet.WeAuth;
import me.xuxiaoxiao.chatapi.wechat.WeChatApi;

public class WeAuth {
	
	public static Data<WeAuth> data = new Data<WeAuth>(WeAuth.class);
	
	public long id; // userId
	
	public String host;
    public String uin;
    public String sid;
    public String dataTicket;
    public String skey;
    public String passticket;
	
	public WeChatApi createApi() {
		
		WeChatApi api = new WeChatApi();

		api.host = this.host;
		api.uin = this.uin;
		api.sid = this.sid;
		api.dataTicket = this.dataTicket;
		api.skey = this.skey;
		api.passticket = this.passticket;
		
		return api;
		
	}
	
}
