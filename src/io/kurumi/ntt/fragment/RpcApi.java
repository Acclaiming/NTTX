package io.kurumi.ntt.fragment;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.RpcApi.RpcKey;
import cn.hutool.core.lang.UUID;
import io.kurumi.ntt.utils.Html;

public class RpcApi extends Fragment {

	public static Data<RpcKey> data = new Data<>(RpcKey.class);
	
	public static class RpcKey {
		
		public Long id;
		
		public String uuid;
		
		public String key() {
			
			return id + "-" + uuid;
			
		}
		
	}
	
	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("api_key","api_key_regenerate","api_key_revoke");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (function.endsWith("revoke")) {
			
			data.deleteById(user.id);
			
			msg.send("已经删除令牌 .").async();
			
			return;
			
		}
		
		RpcKey key = data.getById(user.id);

		if (key == null || function.endsWith("regenerate")) {
			
			key = new RpcKey();
			
			key.id = user.id;
			
			key.uuid = UUID.fastUUID().toString(true);
		
			data.setById(key.id,key);
			
		}
		
		String message = user.userName() + " 你的授权令牌 :";
		
		message += "\n\n" + Html.code(key.key());
		
		message += "\n";
		
		message +="\n重置令牌 : /api_key_regenerate";
		message +="\n删除令牌 : /api_key_revoke";
		
	}
	
	public static JSONObject execute(JSONObject request) {
		
		JSONObject response = new JSONObject();
		
		String apiKey = request.getStr("apiKey");
		
		
		
		return response;
		
	}
	
}
