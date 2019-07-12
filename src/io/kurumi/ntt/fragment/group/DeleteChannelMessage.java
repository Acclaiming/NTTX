package io.kurumi.ntt.fragment.group;

import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;

public class DeleteChannelMessage extends Fragment {

	public static JSONArray enable = GroupData.getJSONArray("data","del_chan_msg",true);

	public static void save() {

        GroupData.setJSONArray("data","del_chan_msg",enable);

    }
	
	@Override
	public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {

		return FUNCTION_GROUP;

	}

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

        registerFunction("dcm");

	}

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (NTT.checkGroupAdmin(msg)) return;

        if (params.length == 1 && "off".equals(params[0])) {

            if (!enable.contains(msg.chatId().longValue())) {

                msg.send("无需重复关闭 ~").exec();

            } else {
				
                enable.remove(msg.chatId().longValue());

                save();

                msg.send("关闭成功 ~").exec();

            }

        } else {

            if (enable.contains(msg.chatId().longValue())) {

                msg.send("没有关闭 ~").exec();

            } else {

                enable.add(msg.chatId().longValue());

                save();

                msg.send("已开启 ~").exec();

            }

        }

    }

	@Override
	public int checkMsg(UserData user,Msg msg) {

		if (msg.isGroup() && enable.contains(msg.chatId().longValue())) {

			if (user.id == 777000) {

				msg.delete();

				return PROCESS_REJECT;

			}

		}
		
		return PROCESS_ASYNC;

	}


}
