package io.kurumi.ntt.fragment.twitter.ui.extra;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.fragment.twitter.spam.SpamTag;
import java.util.List;
import io.kurumi.ntt.fragment.twitter.ui.ExtraMain;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.model.Msg;
import java.util.HashMap;
import java.util.LinkedList;
import io.kurumi.ntt.Env;

public class SpamMain extends Fragment {

	public static String POINT_SPAM = "twi_spam";
	public static String POINT_SPAM_TAG = "twi_stag";
	public static String POINT_SPAM_SET = "twi_sset";

	public static String POINT_NEW_TAG = "twi_stn";
	public static String POINT_RN_TAG = "twi_srn";
	public static String POINT_RD_TAG = "twi_srd";
	public static String POINT_DEL_TAG = "twi_ssd";
	public static String POINT_CONFIRM_DEL_TAG = "twi_sfd";

	public static String POINT_SHOW_RECORDS = "twi_ssow";
	public static String POINT_ADD_RECORD = "twi_sadd";
	public static String POINT_REMOVE_RECORD = "twi_srem";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(

			POINT_SPAM,
			POINT_NEW_TAG,
			POINT_SPAM_TAG,
			POINT_SPAM_SET,

			POINT_RN_TAG,
			POINT_RD_TAG,
			POINT_DEL_TAG,
			POINT_CONFIRM_DEL_TAG,

			POINT_ADD_RECORD,
			POINT_REMOVE_RECORD

		);

		registerPoint(

			POINT_NEW_TAG,

			POINT_RN_TAG,
			POINT_RD_TAG,
			POINT_DEL_TAG,

			POINT_ADD_RECORD,
			POINT_REMOVE_RECORD


		);

	}

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (params.length == 0 || !NumberUtil.isNumber(params[0])) {

			callback.invalidQuery();

			return;

		}

		long accountId = NumberUtil.parseLong(params[0]);

		TAuth account = TAuth.getById(accountId);

		if (account == null) {

			callback.alert("无效的账号 .");

			callback.delete();

			return;

		}

		if (POINT_SPAM.equals(point)) {

			spamMain(user,callback,account);

		} else if (POINT_NEW_TAG.equals(point)) {

			newTag(user,callback,account);

		} else if (POINT_SPAM_TAG.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			spamTag(user,callback,params[1],account);

		} else if (POINT_SPAM_SET.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			spamSet(user,callback,params[1],account);

		} else if (POINT_RN_TAG.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			rnTag(user,callback,params[1],account);

		} else if (POINT_RD_TAG.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			rdTag(user,callback,params[1],account);

		} else if (POINT_DEL_TAG.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			delTag(user,callback,params[1],account);

		} else if (POINT_CONFIRM_DEL_TAG.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			confirmDelTag(user,callback,params[1],account);

		} else if (POINT_ADD_RECORD.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			addRecord(user,callback,params[1],account);

		} else if (POINT_REMOVE_RECORD.equals(point)) {

			if (params.length < 2) {

				callback.invalidQuery();

				return;

			}

			removeRecord(user,callback,params[1],account);

		}


	}

	class MainPoint extends PointData {

		UserData user;
		Callback origin;
		TAuth auth;

		public MainPoint(UserData user,Callback origin,TAuth auth) {

			this.user = user;
			this.origin = origin;
			this.auth = auth;

		}

		@Override
		public void onFinish() {

			spamMain(user,origin,auth);

			super.onFinish();

		}

	}

	class TagPoint extends PointData {

		UserData user;
		Callback origin;
		TAuth auth;
		String tagName;

		public TagPoint(UserData user,Callback origin,TAuth auth,String tagName) {

			this.user = user;
			this.origin = origin;
			this.auth = auth;
			this.tagName = tagName;

		}

		@Override
		public void onFinish() {

			spamTag(user,origin,tagName,auth);

			super.onFinish();

		}

	}


	void newTag(UserData user,Callback callback,TAuth account) {

		setPrivatePoint(user,POINT_NEW_TAG,new MainPoint(user,callback,account));

		callback.edit("输入新分类名称 :").withCancel().async();

	}

	void rnTag(UserData user,Callback callback,String tagName,TAuth account) {

		setPrivatePoint(user,POINT_RN_TAG,new TagPoint(user,callback,account,tagName));

		callback.edit("输入新分类名 :").withCancel().async();

	}

	void rdTag(UserData user,Callback callback,String tagName,TAuth account) {

		setPrivatePoint(user,POINT_RD_TAG,new TagPoint(user,callback,account,tagName));

		callback.edit("输入新说明 :").withCancel().async();

	}

	void delTag(UserData user,Callback callback,String tagName,TAuth account) {

		String message = "确认删除分类 : " + tagName + " ？";

		ButtonMarkup buttons = new ButtonMarkup();

		buttons.newButtonLine("确认",POINT_CONFIRM_DEL_TAG,account.id,tagName);

		buttons.newButtonLine("🔙",POINT_SPAM_TAG,account.id,tagName);

		callback.edit(message).buttons(buttons).async();

	}

	void confirmDelTag(UserData user,Callback callback,String tagName,TAuth account) {

		SpamTag.data.deleteById(tagName);

		spamMain(user,callback,account);

	}

	void addRecord(UserData user,Callback callback,String tagName,TAuth account) {

		setPrivatePoint(user,POINT_ADD_RECORD,new TagPoint(user,callback,account,tagName));

		callback.edit("输入对方 ID 或 链接:").withCancel().async();

	}

	void removeRecord(UserData user,Callback callback,String tagName,TAuth account) {

		setPrivatePoint(user,POINT_REMOVE_RECORD,new TagPoint(user,callback,account,tagName));

		callback.edit("输入对方 ID 或 链接:").withCancel().async();

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		data.with(msg);

		if (POINT_NEW_TAG.equals(point)) {

			if (!msg.hasText()) {

				clearPrivatePoint(user);

				return;

			}

			if (SpamTag.data.containsId(msg.text())) {

				msg.send("该分类已存在").exec(data);

				return;

			}

			SpamTag newTag = new SpamTag();

			newTag.id = msg.text();

			newTag.description = ":)";

			newTag.records = new HashMap<>();

			newTag.subscribers = new LinkedList<>();

			SpamTag.data.setById(newTag.id,newTag);

			clearPrivatePoint(user);

		} else if (POINT_RN_TAG.equals(point)) {

			TagPoint edit = (TagPoint) data;

			if (!msg.hasText()) {

				clearPrivatePoint(user);

				return;

			}

			if (!SpamTag.data.containsId(edit.tagName)) {

				clearPrivatePoint(user);

				return;

			}

			if (SpamTag.data.containsId(msg.text())) {

				msg.send("该分类已存在").exec(data);

				return;

			}

			SpamTag tag = SpamTag.data.getById(edit.tagName);

			tag.id = msg.text();

			SpamTag.data.deleteById(edit.tagName);

			SpamTag.data.setById(tag.id,tag);

			clearPrivatePoint(user);

		} else if (POINT_RD_TAG.equals(point)) {

			TagPoint edit = (TagPoint) data;

			if (!msg.hasText()) {

				clearPrivatePoint(user);

				return;

			}

			if (!SpamTag.data.containsId(edit.tagName)) {

				clearPrivatePoint(user);

				return;

			}

			SpamTag tag = SpamTag.data.getById(edit.tagName);

			tag.description = msg.text();

			SpamTag.data.setById(tag.id,tag);

			clearPrivatePoint(user);

		} else if (POINT_ADD_RECORD.equals(point)) {

			TagPoint edit = (TagPoint) data;

			if (!msg.hasText()) {

				clearPrivatePoint(user);

				return;

			}

			UserArchive archive;

			if (NumberUtil.isNumber(msg.text())) {

				archive = UserArchive.show(edit.auth.createApi(),NumberUtil.parseLong(msg.text()));


			} else {

				archive = UserArchive.show(edit.auth.createApi(),NTT.parseScreenName(msg.text()));

			}

			if (archive == null) {

				msg.send("查无此人").withCancel().exec(data);

				return;

			}

			if (!SpamTag.data.containsId(edit.tagName)) {

				clearPrivatePoint(user);

				return;

			}

			SpamTag tag = SpamTag.data.getById(edit.tagName);

			Msg record = new Send(Env.SPAM_CHANNEL,"#新增记录 [ " + edit.tagName + " ]\n",archive.formatSimple()).html().send();

			tag.records.put(archive.id.toString(),record.messageId());

			SpamTag.data.setById(tag.id,tag);

			clearPrivatePoint(user);

		} else if (POINT_REMOVE_RECORD.equals(point)) {

			TagPoint edit = (TagPoint) data;

			if (!msg.hasText()) {

				clearPrivatePoint(user);

				return;

			}

			UserArchive archive;

			if (NumberUtil.isNumber(msg.text())) {

				archive = UserArchive.show(edit.auth.createApi(),NumberUtil.parseLong(msg.text()));


			} else {

				archive = UserArchive.show(edit.auth.createApi(),NTT.parseScreenName(msg.text()));

			}

			if (archive == null) {

				msg.send("查无此人").withCancel().exec(data);

				return;

			}

			if (!SpamTag.data.containsId(edit.tagName)) {

				clearPrivatePoint(user);

				return;

			}
			
			new Send(Env.SPAM_CHANNEL,"#移除记录 [ " + edit.tagName + " ]\n",archive.formatSimple()).html().send();

			SpamTag tag = SpamTag.data.getById(edit.tagName);

			tag.records.remove(archive.id.toString());

			SpamTag.data.setById(tag.id,tag);

			clearPrivatePoint(user);

		}

	}

	void spamTag(UserData user,Callback callback,String tagName,TAuth account) {

		SpamTag tag = SpamTag.data.getById(tagName);

		if (tag == null) {

			callback.invalidQuery();

			spamMain(user,callback,account);

			return;

		}

		String message = tag.id + " [ " + account.archive().name + " ]";

		message += "\n\n" + tag.description;

		ButtonMarkup buttons = new ButtonMarkup();

		if (user.admin()) {

			buttons.newButtonLine()
				.newButton("重命名",POINT_RN_TAG,account.id,tag.id)
				.newButton("设置介绍",POINT_RD_TAG,account.id,tag.id);

		}

		boolean subed = tag.subscribers.contains(account.id);

		buttons.newButtonLine()
			.newButton("同步")
			.newButton(subed ? "✅" : "☑",POINT_SPAM_SET,account.id,tag.id);

		// buttons.newButtonLine("查看所有",POINT_SHOW_RECORDS,account.id,tag.id);

		if (user.admin()) {

			buttons.newButtonLine()
				.newButton("添加记录",POINT_ADD_RECORD,account.id,tag.id)
				.newButton("移除记录",POINT_REMOVE_RECORD,account.id,tag.id);

			buttons.newButtonLine("删除分类",POINT_DEL_TAG,account.id,tag.id);

		}

		buttons.newButtonLine("🔙",POINT_SPAM,account.id);

		callback.edit(message).buttons(buttons).async();

	}

	void spamSet(UserData user,Callback callback,String tagName,TAuth account) {

		SpamTag tag = SpamTag.data.getById(tagName);

		if (tag == null) {

			callback.invalidQuery();

			spamMain(user,callback,account);

			return;

		}

		if (tag.subscribers.contains(account.id)) {

			tag.subscribers.remove(account.id);

		} else {

			tag.subscribers.add(account.id);

		}

		SpamTag.data.setById(tag.id,tag);

		spamTag(user,callback,tagName,account);

	}

	void spamMain(UserData user,Callback callback,TAuth account) {

		String message = "Twitter 联合联合封禁分类 [ " + account.archive().name + " ]";

		ButtonMarkup buttons = new ButtonMarkup();

		if (user.admin()) {

			buttons.newButtonLine(">> 新建 <<",POINT_NEW_TAG,account.id);

		}

		List<SpamTag> tags = SpamTag.data.getAll();

		if (tags.isEmpty()) {

			buttons.newButtonLine("暂无分类");

		} else {

			for (SpamTag tag : tags) {

				buttons.newButtonLine(tag.id,POINT_SPAM_TAG,account.id,tag.id);

			}

		}

		buttons.newButtonLine("🔙",ExtraMain.POINT_EXTRA,account.id);

		callback.edit(message).buttons(buttons).async();

	}


}
