package io.kurumi.ntt.fragment.group;

import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;

public class DeleteChannelMessage extends Fragment {
	
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

		GroupData data = GroupData.get(msg.chat());
		
        if (params.length == 1 && "off".equals(params[0])) {

            if (data.delete_channel_msg == null) {

                msg.send("无需重复关闭 ~").exec();

            } else {
				
                data.delete_channel_msg = null;

                msg.send("关闭成功 ~").exec();

            }

        } else {

            if (data.delete_channel_msg != null) {

                msg.send("无须重复开启 ~").exec();

            } else {

                data.delete_channel_msg = true;
				
                msg.send("已开启 ~").exec();

            }

        }

    }

	@Override
	public int checkMsg(UserData user,Msg msg) {

		if (msg.isGroup()) {

			GroupData data = GroupData.get(msg.chat());

			if (data.delete_channel_msg != null && user.id == 777000) {

				msg.delete();

				return PROCESS_REJECT;
				
			}

		}
		
		return PROCESS_ASYNC;

	}


}
