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

public class BlockedBy extends Fragment {

	public static void onBlocked(TAuth auth,Twitter api,UserArchive archive) {

		if (auth.bbb != null) {
			
			try {
				
				api.createBlock(archive.id);
				
				new Send(auth.user,"屏蔽 {} 成功",archive.urlHtml()).html().async();
				
			} catch (TwitterException e) {
				
				new Send(auth.user,"屏蔽 {} 失败 : \n\n{}",archive.urlHtml(),NTT.parseTwitterException(e)).html().async();
				
			}

		}
		
		if (auth.bbp != null) {

			try {

				Status status = api.updateStatus(formatMessage(auth,archive));

				new Send(auth.user,"被屏蔽已推送 :\n\n{}",StatusArchive.save(status).url()).enableLinkPreview().async();

			} catch (TwitterException e) {

				new Send(auth.user,"被屏蔽推送失败 :\n\n{}",NTT.parseTwitterException(e)).async();

			}

		}

	}

	public static String POINT_BB = "twi_bb";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_BB);
		registerPoint(POINT_BB);

	}

	class BBSet extends PointData {

		Callback origin;
		TAuth account;

		public BBSet(Callback origin,TAuth account) {

			this.origin = origin;
			this.account = account;

		}

		@Override
		public void onFinish() {

			bbMain(origin.from(),origin,account);

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

			bbMain(user,callback,account);

			return;

		}

		String action = params[1];

		if ("bbb".equals(action)) {

			if (account.bbb == null) {

				account.bbb = true;

			} else {

				account.bbb = null;

			}
			
			TAuth.data.setById(account.id,account);
			
			bbMain(user,callback,account);
			
		} else if ("bbp".equals(action)) {

			if (account.bbp == null) {

				account.bbp = true;

			} else {

				account.bbp = null;

			}
			
			TAuth.data.setById(account.id,account);
			
			bbMain(user,callback,account);

		} else if ("temp".equals(action)) {

			setPrivatePoint(user,POINT_BB,new BBSet(callback,account));

			callback.edit("请发送新的消息模板 : ","\n默认模板 : " + Html.code(defaultMessage()),"\n可用变量 : " + HtmlUtil.escape(" <名称> 、 <用户名>")).withCancel().html().async();

		}


	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		if (POINT_BB.equals(point)) {

			BBSet set = (BBSet) data.with(msg);

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

		String message = "被 @<用户名> 屏蔽了, 真可惜。";

		message += "\n\n( 由NTT自动推送 )";

		return message;

	}

	public static String formatMessage(TAuth account,UserArchive target) {

		String message = account.bbp_msg == null ? defaultMessage() : account.bbp_msg;
		message = message.replace("<名称>",target.name);
		message = message.replace("<用户名>",target.screenName);

		return message;

	}

	void bbMain(UserData user,Callback callback,TAuth account) {

		String message = "被屏蔽处理 : [ " + account.archive().name + " ]";

		message += "\n\n推文模板 : ";

		if (account.oup_msg == null) {

			message += "[ 默认 ]";

		}

		message += "\n\n" + Html.code(account.oup_msg == null ? defaultMessage() : account.oup_msg);

		ButtonMarkup buttons = new ButtonMarkup();

		buttons.newButtonLine()
			.newButton("屏蔽对方")
			.newButton(account.bbb != null ? "✅" : "☑",POINT_BB,account.id,"bbb");

		buttons.newButtonLine()
			.newButton("自动推送")
			.newButton(account.bbp != null ? "✅" : "☑",POINT_BB,account.id,"bbp");

		buttons.newButtonLine("设置消息推送模板",POINT_BB,account.id,"temp");

		buttons.newButtonLine("🔙",ExtraMain.POINT_EXTRA,account.id);

		callback.edit(message).buttons(buttons).html().async();

	}



}
