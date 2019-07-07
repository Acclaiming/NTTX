package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.request.ForwardMessage;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;

import static java.util.Arrays.asList;

public class Notice extends Fragment {

    final String POINT_FPRWARD = "admin_notice";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerAdminFunction("notice");

		registerPoint(POINT_FPRWARD);

    }

	@Override
	public int checkFunction(UserData user,Msg msg,String function,String[] params) {

		return PROCESS_SYNC;
		
	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		msg.send("现在发送群发内容 :").exec();

		setPrivatePoint(user,POINT_FPRWARD,ArrayUtil.join(params," "));

    }

	@Override
	public int checkPoint(UserData user,Msg msg,String point,Object data) {

		return PROCESS_THREAD;

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {

        String params = data.toString();

        boolean mute = params.contains("mute");
        boolean login = params.contains("login");

        clearPrivatePoint(user);

        if (!login) {

            long count = UserData.data.collection.countDocuments();

            long success = 0;
            long failed = 0;

            Msg status = msg.reply("正在群发 : 0 / 0 / " + count).send();

            for (UserData userData : UserData.data.collection.find()) {


                if (userData.contactable == null || userData.contactable) {

                    if (login && TAuth.data.countByField("user",userData.id) == 0) {

						failed ++;
						
                        continue;

                    }

                    ForwardMessage forward = new ForwardMessage(userData.id,user.id,msg.messageId());

                    if (mute) forward.disableNotification(true);


                    if (bot().execute(forward).isOk()) success++;
                    else {

                        failed++;

                        userData.contactable = false;

                        UserData.userDataIndex.remove(userData.id);

                        userData.data.setById(userData.id,userData);

                    }

                } else {

                    failed++;

                    continue;

                }


                status.edit("正在群发 : " + success + " / " + (success + failed) + " / " + count).exec();

            }

            status.edit("正在群发 : " + success + " / " + (success + failed) + " / " + count).exec();


        }

    }

}
