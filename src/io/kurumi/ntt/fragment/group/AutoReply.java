package io.kurumi.ntt.fragment.group;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONArray;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.utils.TentcentNlp;

public class AutoReply extends Fragment {

    public static JSONArray disable = LocalData.getJSONArray("data","disable_action",true);

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);

		registerFunction("reply");


    }

	@Override
	public int checkFunction() {
		
		return FUNCTION_GROUP;
		
	}
	
    @Override
    public void onGroup(UserData user,Msg msg) {

        if (!msg.hasText() || msg.isCommand() || disable.contains(msg.chatId().longValue())) return;

        String text = msg.text();

        if (msg.text().contains("@NTToolsBot") || (text.toLowerCase().contains("ntt") && RandomUtil.randomInt(0,4) == 2) || (msg.isReply() && msg.replyTo().from().id.equals(origin.me.id())) || RandomUtil.randomInt(0,51) == 9) {

            msg.sendTyping();

            String reply = TentcentNlp.nlpTextchat(((Long) (user.id < 0 ? user.id * -1 : user.id)).toString(),text);

            if (reply == null) return;

            msg.reply(reply).exec();

        }

        return;

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        if (NTT.checkGroupAdmin(msg)) return;

        if (params.length == 1 && "off".equals(params[0])) {

            if (disable.contains(msg.chatId().longValue())) {

                msg.send("无需重复关闭 ~").exec();

            } else {

                disable.add(msg.chatId().longValue());

                LocalData.setJSONArray("data","disable_action",disable);

                msg.send("关闭成功 ~").exec();

            }

        } else {

            if (!disable.contains(msg.chatId().longValue())) {

                msg.send("没有关闭 ~").exec();

            } else {

                disable.remove(msg.chatId().longValue());

                LocalData.setJSONArray("data","disable_action",disable);

                msg.send("已开启 ~").exec();

            }

        }


    }


}
