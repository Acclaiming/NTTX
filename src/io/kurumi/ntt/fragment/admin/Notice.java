package io.kurumi.ntt.fragment.admin;

import cn.hutool.core.util.ArrayUtil;
import com.pengrad.telegrambot.request.ForwardMessage;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;

import static java.util.Arrays.asList;
import io.kurumi.ntt.db.PointData;
import org.bson.Document;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;
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
	
		PointData data = setPrivatePointData(user,POINT_FPRWARD,ArrayUtil.join(params," "));

		msg.send("现在发送群发内容 :").exec(data);
		
    }

	@Override
	public int checkPoint(UserData user,Msg msg,String point,PointData data) {

		return PROCESS_THREAD;

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		data.context.add(msg);
		
        String params = data.data();

        boolean mute = params.contains("mute");
        boolean login = params.contains("login");
		boolean tryAll = params.contains("try");
		
		if (tryAll) {
			
			UserData.data.collection.updateMany(eq("contactable",false),unset("contactable"));
			
		}
		
        clearPrivatePoint(user);
		
            long count = UserData.data.collection.countDocuments();

            long success = 0;
            long failed = 0;

            Msg status = msg.send("正在群发 : 0 / 0 / " + count).send();

            for (UserData userData : UserData.data.collection.find()) {

                if (userData.contactable == null || userData.contactable) {

                    if (login && TAuth.data.countByField("user",userData.id) == 0) {

						failed ++;
						
                        continue;

                    }

                    ForwardMessage forward = new ForwardMessage(userData.id,msg.chatId(),msg.messageId());

                    if (mute) forward.disableNotification(true);

                    if (bot().execute(forward).isOk()) {
						
						success++;
						
						userData.contactable = true;

                        userData.data.setById(userData.id,userData);

						
                    } else {

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
