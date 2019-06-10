package io.kurumi.ntt.fragment.twitter.action;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Function;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.TwitterException;

public class Jvbao extends Function {

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if (!user.developer()) {

			msg.send("Permission Denied").exec();

			return;

		}

		if (params.length == 0) {

			msg.send("/jvbao <用户ID|用户名|链接>").exec();

			return;

		}

		String target = null;
		long targetL = -1;

		if (NumberUtil.isNumber(params[0])) {

			targetL = NumberUtil.parseLong(params[0]);

		} else {

			target = NTT.parseScreenName(params[0]);

		}

		Msg status = msg.send("正在Jvbao...").send();

		int success = 0;

		int failed = 0;

		for (TAuth auth : TAuth.data.collection.find()) {

			try {

				if (targetL == -1) {

					auth.createApi().reportSpam(target,false);

				} else {

					auth.createApi().reportSpam(targetL,false);

				}
				
				success ++;
				
			} catch (TwitterException e) {
				
				failed ++;
				
			}

			status.edit("正在Jvbao...","成功 : " + success,"失败 : " + failed).exec();

		}

		status.edit("Jvbao完成","成功 : " + success,"失败 : " + failed).exec();
	
		

	}

	@Override
	public void functions(LinkedList<String> names) {

		names.add("jvbao");


	}

}
