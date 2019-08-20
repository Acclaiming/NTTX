package io.kurumi.ntt.fragment.sorry;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.request.SendAnimation;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;
import java.io.File;

public class MakeGif extends Fragment {

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("mkgif");

		registerPoint(POINT_MAKE_GIF);

	}

	final String POINT_MAKE_GIF = "make_gif";

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		setPrivatePoint(user,msg,POINT_MAKE_GIF);

		msg.send("请选择模板 :").withCancel().keyboardVertical(SorryApi.templates.keySet().toArray()).async();

	}

	@Override
	public int checkPoint(UserData user,Msg msg,String point,PointData data) {

		return PROCESS_ASYNC;

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		if (data.step == 0) {

			if (!SorryApi.templates.containsKey(msg.text())) {

				clearPrivatePoint(user);

				msg.send("没有这个模板 :(").async();

				return;

			}

			SorryApi temp = SorryApi.templates.get(msg.text());

			data.step = 1;
			data.data = temp;

			msg.send("请输入文字，一行一句。 默认 : \n\n" + Html.code(ArrayUtil.join(temp.hint,"\n"))).html().withCancel().async();

			return;

		} else if (data.step == 1) {

			clearPrivatePoint(user);

			Msg status = msg.send("正在制作....").send();

			SorryApi api = data.data();

			File file = api.make(msg.hasText() ? msg.text().split("\n") : new String[0]);

			if (file == null) {

				status.edit("服务器繁忙 请重试....").async();

				return;

			}

			status.delete();
			
			msg.sendUpdatingVideo();

			executeAsync(new SendAnimation(msg.chatId(),file));

		}

	}


}
