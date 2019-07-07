package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.LeaveChat;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.Send;

public class Actions extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("do");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		BaseRequest request;

		if (params.length == 0) {

			msg.send("invalid method name").exec();

			return;

		}

		String method = params[0];

		params = (String[]) ArrayUtil.sub(params,1,params.length);

		if ("send".equals(method)) {

			if (params.length < 2) {

				msg.send("empty id and content").exec();

				return;

			}

			request = new Send(NumberUtil.parseLong(params[0]),ArrayUtil.sub(params,1,params.length)).request();

		} else if ("exit".equals(method)) {

			if (params.length < 1) {

				msg.send("empty id").exec();

				return;

			}

			request = new LeaveChat(NumberUtil.parseLong(params[0]));

		} else {

			msg.send("invalid method").exec();

			return;

		}

		BaseResponse response = bot().execute(request);

		msg.send(response.toString()).exec();

	}

}
