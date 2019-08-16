package io.kurumi.ntt.fragment;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.RpcApi.RpcKey;
import cn.hutool.core.lang.UUID;
import io.kurumi.ntt.utils.Html;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.model.request.Send;
import com.pengrad.telegrambot.response.SendResponse;

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

		message += "\n重置令牌 : /api_key_regenerate";
		message += "\n删除令牌 : /api_key_revoke";

		msg.send(message).html().async();

	}

	public static JSONObject execute(JSONObject request) {

		String apiKey = request.getStr("apiKey");

		Long user = getAuth(apiKey);

		if (user == -1L) {

			return makeError("unauthorized.");

		}

		String methodName = request.getStr("method");

		if ("send".equals(methodName)) {

			String text = request.getStr("text");

			if (text == null) {

				return makeError("empty text.");

			}

			Send send = new Send(user,text);

			String parseMode = request.getStr("parseMode","plainText");

			if ("html".equals(parseMode.toLowerCase())) {

				send.html();

			} else if ("markdown".equals(parseMode.toLowerCase())) {

				send.markdown();

			}

			return makeResult(new JSONObject(send.exec().json).toStringPretty());

		} else {

			return makeError("method not found.");

		}

	}

	static JSONObject makeResult(String content) {

		JSONObject response = new JSONObject();

		response.put("ok",true);
		response.put("result",content);

		return response;

	}


	static JSONObject makeError(String content) {

		JSONObject response = new JSONObject();

		response.put("ok",false);
		response.put("description",content);

		return response;

	}

	static Long getAuth(String apiKey) {

		if (StrUtil.isBlank(apiKey) || !apiKey.contains("-")) return -1L;

		String userIdStr = StrUtil.subBefore(apiKey,"-",false);

		if (!NumberUtil.isNumber(userIdStr)) return -1L;

		Long userId = NumberUtil.parseLong(userIdStr);

		RpcKey key = data.getById(userId);

		if (key == null && !key.key().equals(apiKey)) return -1L;

		return userId;

	}

}
