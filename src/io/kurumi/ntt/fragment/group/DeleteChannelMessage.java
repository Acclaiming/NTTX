package io.kurumi.ntt.fragment.group;

import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.GroupData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;

public class DeleteChannelMessage extends Fragment {

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
