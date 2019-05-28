package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.funcs.abs.*;

import static java.util.Arrays.asList;
import io.kurumi.ntt.db.PointStore.*;
import java.util.*;
import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.request.ForwardMessage;
import io.kurumi.ntt.twitter.*;

public class Notice extends Function {

    public static final Notice INSTANCE = new Notice();

    @Override
    public void functions(LinkedList<String> names) {

        names.add("notice");

    }

    final String POINT_FPRWARD = "n|f";

    @Override
    public void points(LinkedList<String> points) {

        points.add(POINT_FPRWARD);

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (user.developer()) {

            msg.send("现在发送群发内容 :").exec();

            setPoint(user,POINT_FPRWARD,PointStore.Type.Global,ArrayUtil.join(params," "));

        } else msg.send("Permission denied").exec();

    }

    @Override
    public void onPoint(UserData user,Msg msg,PointStore.Point point) {

		String params = point.data.toString();

        boolean mute = params.contains("mute");
        boolean login = params.contains("login");

        clearPoint(user);

		if (!login) {

			long count = UserData.data.collection.countDocuments();

			long success = 0;
			long failed = 0;

			Msg status = msg.reply("正在群发 : 0 / 0 / " + count).send();

			for (UserData userData : UserData.data.collection.find()) {

				
				
				if (userData.contactable == null || userData.contactable) {

					if (login && TAuth.data.countByField("user",userData.id) < 0) {

						continue;

					}
					
					ForwardMessage forward = new ForwardMessage(userData.id,user.id,msg.messageId());

					if (mute) forward.disableNotification(true);

					
					
					if (bot().execute(forward).isOk()) success ++; else {

						failed ++;

						userData.contactable = false;

						UserData.userDataIndex.remove(userData.id);

						userData.data.setById(userData.id,userData);

					}

				} else {

					failed ++;
					
					continue;

				}


				status.edit("正在群发 : " + success + " / " + (success + failed) + " / " + count).exec();

			}

		}

    }

}
