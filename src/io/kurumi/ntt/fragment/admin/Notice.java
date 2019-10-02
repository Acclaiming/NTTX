package io.kurumi.ntt.fragment.admin;

import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.GroupData;


public class Notice extends Fragment {

    final String POINT_FPRWARD = "admin_notice";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("notice");

        registerPoint(POINT_FPRWARD);

    }


    @Override
    public int checkPoint(UserData user, Msg msg, String point, PointData data) {

        return PROCESS_ASYNC;

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        PointData data = setPrivatePointData(user, msg, POINT_FPRWARD, msg.param());

        msg.send("现在发送群发内容 :").exec(data);

    }

    @Override
    public void onPoint(UserData user, Msg msg, String point, PointData data) {

        String params = data.data();

        boolean mute = params.contains("mute");
        boolean login = params.contains("login");
        boolean tryAll = params.contains("try");

        if (tryAll) {

            //UserData.data.collection.updateMany(eq("contactable",false),unset("contactable"));

        }

        clearPrivatePoint(user);

        long count = UserData.data.collection.countDocuments();

        long success = 0;
        long failed = 0;

        Msg status = msg.send("正在群发 : 0 / 0 / " + count).send();

		for (GroupData group : GroupData.data.getAll()) {
			
			ForwardMessage forward = new ForwardMessage(group.id, msg.chatId(), msg.messageId());
			
			if (mute) forward.disableNotification(true);

			execute(forward);

			
		}
		
        for (UserData userData : UserData.data.collection.find()) {

            if (tryAll || userData.contactable == null || userData.contactable) {

                if (login && TAuth.data.countByField("user", userData.id) == 0) {

                    failed++;

                    continue;

                }

                ForwardMessage forward = new ForwardMessage(userData.id, msg.chatId(), msg.messageId());

                if (mute) forward.disableNotification(true);

                SendResponse resp = bot().execute(forward);

                if (resp.isOk()) {

                    success++;

                    userData.contactable = true;

                    UserData.data.setById(userData.id, userData);


                } else {

                    failed++;

                    userData.contactable = false;

                    UserData.userDataIndex.remove(userData.id);

                    UserData.data.setById(userData.id, userData);

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
