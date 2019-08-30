package io.kurumi.ntt.fragment.twitter.ui.extra;

import twitter4j.*;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.http.HtmlUtil;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.fragment.twitter.ui.ExtraMain;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;

public class FollowedBy extends Fragment {

	public static void onFollowed(TAuth auth,Twitter api,UserArchive archive) {

		if (auth.fb != null) {
			
			try {
				
				api.createFriendship(archive.id);
				
				String message = "关注 {} 成功{}";
				
				if (auth.multiUser()) {
					
					message += "\n\n账号 : " + auth.archive().bName();
					
				}
				
				new Send(auth.user,message,archive.urlHtml()).html().async();
				
			} catch (TwitterException e) {
				
				String message = "关注 {} 失败 : \n\n{}";
				
				if (auth.multiUser()) {

					message += "\n\n账号 : " + auth.archive().bName();

				}
				
				new Send(auth.user,message,archive.urlHtml(),NTT.parseTwitterException(e)).html().async();
				
			}

		}
		
		if (auth.bbp != null) {

			try {

				Status status = api.updateStatus(formatMessage(auth,archive));

				String message = "新关注者已推送 :\n\n{}";
				
				if (auth.multiUser()) {

					message += "\n\n账号 : " + auth.archive().bName();

				}
				
				new Send(auth.user,message,StatusArchive.save(status).url()).enableLinkPreview().async();

			} catch (TwitterException e) {

				String message = "新关注者推送失败 :\n\n{}";
				
				if (auth.multiUser()) {

					message += "\n\n账号 : " + auth.archive().bName();

				}
				
				new Send(auth.user,message,NTT.parseTwitterException(e)).async();

			}

		}

	}

	public static String POINT_FB = "twi_fb";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_FB);
		registerPoint(POINT_FB);

	}

	class FBSet extends PointData {

		Callback origin;
		TAuth account;

		public FBSet(Callback origin,TAuth account) {

			this.origin = origin;
			this.account = account;

		}

		@Override
		public void onFinish() {

			fbMain(origin.from(),origin,account);

			super.onFinish();

		}


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

		if (params.length == 1) {

			fbMain(user,callback,account);

			return;

		}

		String action = params[1];

		if ("fb".equals(action)) {

			if (account.fb == null) {

				account.fb = true;

			} else {

				account.fb = null;

			}
			
			TAuth.data.setById(account.id,account);
			
			fbMain(user,callback,account);
			
		} else if ("fbp".equals(action)) {

			if (account.fbp == null) {

				account.fbp = true;

			} else {

				account.fbp = null;

			}
			
			TAuth.data.setById(account.id,account);
			
			fbMain(user,callback,account);
			
		} else if ("fbi".equals(action)) {

			if (account.fbi == null) {

				account.fbi = true;

			} else {

				account.fbi = null;

			}

			TAuth.data.setById(account.id,account);

			fbMain(user,callback,account);
			
		} else if ("temp".equals(action)) {

			setPrivatePoint(user,POINT_FB,new FBSet(callback,account));

			callback.edit("请发送新的消息模板 : ","\n默认模板 : " + Html.code(defaultMessage()),"\n可用变量 : " + HtmlUtil.escape(" <名称> 、 <用户名>")).withCancel().html().async();

		}


	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		if (POINT_FB.equals(point)) {

			FBSet set = (FBSet) data.with(msg);

			if (!msg.hasText()) {

				clearPrivatePoint(user);

				return;

			}

			set.account.bbp_msg = msg.text().trim().equals(defaultMessage()) ? null : msg.text();

			clearPrivatePoint(user);

			TAuth.data.setById(set.account.id,set.account);


		}

	}


	public static String defaultMessage() {

		String message = "@<用户名> 感谢你的关注 👋";

		return message;

	}

	public static String formatMessage(TAuth account,UserArchive target) {

		String message = account.bbp_msg == null ? defaultMessage() : account.bbp_msg;
		message = message.replace("<名称>",target.name);
		message = message.replace("<用户名>",target.screenName);

		return message;

	}

	void fbMain(UserData user,Callback callback,TAuth account) {

		String message = "被关注处理 : [ " + account.archive().name + " ]";

		message += "\n\n推文模板 : ";

		if (account.oup_msg == null) {

			message += "[ 默认 ]";

		}

		message += "\n\n" + Html.code(account.oup_msg == null ? defaultMessage() : account.oup_msg);

		ButtonMarkup buttons = new ButtonMarkup();

		buttons.newButtonLine()
		.newButton("忽略关注中")
			.newButton(account.fbi != null ? "✅" : "☑",POINT_FB,account.id,"fbi");
			
		buttons.newButtonLine()
			.newButton("关注对方")
			.newButton(account.fb != null ? "✅" : "☑",POINT_FB,account.id,"fb");

		buttons.newButtonLine()
			.newButton("自动推送")
			.newButton(account.fbp != null ? "✅" : "☑",POINT_FB,account.id,"fbp");

		buttons.newButtonLine("设置消息推送模板",POINT_FB,account.id,"temp");

		buttons.newButtonLine("🔙",ExtraMain.POINT_EXTRA,account.id);

		callback.edit(message).buttons(buttons).html().async();

	}



}
