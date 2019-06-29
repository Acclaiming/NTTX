package io.kurumi.ntt.fragment.group;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.LocalData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.fragment.BotFragment;

public class BanSetickerSet extends Fragment {

    public static BanSetickerSet INSTANCE = new BanSetickerSet();
    public static JSONObject bans = LocalData.getJSON("data","ban_sticker_set",true);

    public static void save() {

        LocalData.setJSON("data","ban_sticker_set",bans);

    }

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("banss","unbanss");

	}

	@Override
	public int checkFunction() {

		return FUNCTION_GROUP;

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		if ("banss".equals(function)) {


			if (NTT.checkGroupAdmin(msg)) return;

			String setName = null;

			if (msg.replyTo() == null || msg.replyTo().message().sticker() == null) {

				if (msg.params().length == 0) {

					msg.send("/banss <贴纸集名称> 或者对sticker使用啦...").publicFailed();

					return;

				}

				setName = msg.params()[0];

			} else {

				setName = msg.replyTo().message().sticker().setName();

			}

			JSONArray rules = bans.getJSONArray(msg.chatId().toString());

			if (rules == null) rules = new JSONArray();

			if (rules.contains(setName)) {

				msg.send("这个贴纸包已经被屏蔽了 :)").publicFailed();

				return;

			}

			rules.add(setName);

			bans.put(msg.chatId().toString(),rules);

			save();

			msg.send("屏蔽成功 ~").exec();

		} else if ("unbanss".equals(function)) {

			if (NTT.checkGroupAdmin(msg)) return;

			String setName = null;

			if (msg.replyTo() == null || msg.replyTo().message().sticker() == null) {

				if (msg.params().length == 0) {

					msg.send("/unbanss <贴纸集名称> 或者对sticker使用啦...").publicFailed();

					return;

				}

				setName = msg.params()[0];

			} else {

				setName = msg.replyTo().message().sticker().setName();

			}

			JSONArray rules = bans.getJSONArray(msg.chatId().toString());

			if (rules == null) rules = new JSONArray();

			if (!rules.contains(setName)) {

				msg.send("这个贴纸包没有被屏蔽 :)").publicFailed();

				return;

			}

			rules.remove(setName);

			bans.put(msg.chatId().toString(),rules);

			save();

			msg.send("取消屏蔽成功 ~").exec();

		}

    }

	@Override
	public void onGroup(UserData user,Msg msg) {

        if (msg.message().sticker() != null && bans.containsKey(msg.chatId().toString())) {

            if (bans.getJSONArray(msg.chatId().toString()).contains(msg.message().sticker().setName())) {

                msg.delete();

            }

        }

    }

}


