package io.kurumi.ntt.fragment.debug;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.request.GetUserProfilePhotos;
import com.pengrad.telegrambot.response.GetUserProfilePhotosResponse;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

public class DebugUF extends Fragment {


	@Override
	public void init(BotFragment origin) {

		super.init(origin);

        registerAdminFunction("get_uf");

    }

	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

		return FUNCTION_PUBLIC;

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (params.length < 1) {

			msg.invalidParams("userId").exec();

			return;

		}
		
		GetUserProfilePhotosResponse resp = execute(new GetUserProfilePhotos((int)NumberUtil.parseLong(params[0])));

		if (resp.json.length() < 1024) {

			msg.send(Html.json(resp.json)).html().removeKeyboard().exec();

		} else {

			msg.send(new JSONObject(resp.json).toStringPretty()).exec();

		}
		
	}

}
