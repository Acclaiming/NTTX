package io.kurumi.ntt.fragment.twitter.ui.extra;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.ui.AccountMain;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import cn.hutool.core.date.DateUtil;
import java.text.DateFormat;
import io.kurumi.ntt.fragment.twitter.ui.ExtraMain;
import io.kurumi.ntt.db.PointData;
import io.kurumi.ntt.fragment.twitter.ui.extra.OWUnfoPublish.OupSet;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.model.Msg;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HtmlUtil;

public class OWUnfoPublish extends Fragment {

	public static String POINT_OUP = "twi_oup";
	public static String POINT_OUP_SET = "twi_oup_set";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);

		registerCallback(POINT_OUP,POINT_OUP_SET);

		registerPoint(POINT_OUP_SET);
		
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

		if (POINT_OUP.equals(point)) {

			oupMain(user,callback,account);

		} else {

			params = ArrayUtil.remove(params,0);
			
			oupConfig(user,callback,params,account);

		}

	}

	public static String defaultMessage() {

		String message = "大家好，我被 '<名称>' ( @<用户名> ) 单向取关了，今天你们被单向取关了吗？";

		message += "\n\n自动推送由 @NTT_X";

		return message;

	}

	public static String formatMessage(TAuth account,UserArchive target) {

		String message = account.oup_msg == null ? defaultMessage() : account.oup_msg;

		message = message.replace("<名称>",target.name);
		message = message.replace("<用户名>",target.screenName);

		return message;

	}

	void oupMain(UserData user,Callback callback,TAuth account) {

		String message = "被单向取关自动推文推送 : [ " + account.archive().name + " ]";
		
		message += "\n\n推文模板 : ";

		if (account.oup_msg == null) {

			message += "[默认]";

		}

		message += "\n\n" + Html.code(account.oup_msg == null ? defaultMessage() : account.oup_msg);

		ButtonMarkup buttons = new ButtonMarkup();

		buttons.newButtonLine()
			.newButton("开启")
			.newButton(account.oup != null ? "✅" : "☑",POINT_OUP_SET,account.id);

		buttons.newButtonLine("设置消息推送模板",POINT_OUP_SET,account.id,"temp");

		buttons.newButtonLine("🔙",ExtraMain.POINT_EXTRA,account.id);

		callback.edit(message).buttons(buttons).html().async();

	}

	class OupSet extends PointData {

		Callback origin;
		TAuth account;
		String targte;

		public OupSet(Callback origin,TAuth account,String targte) {

			this.origin = origin;
			this.account = account;
			this.targte = targte;

		}

		@Override
		public void onFinish() {

			oupMain(origin.from(),origin,account);

			super.onFinish();

		}


	}

	void oupConfig(UserData user,Callback callback,String[] params,TAuth account) {

		if (params.length == 0) {

			if (account.oup == null) {

				account.oup = true;

			} else {

				account.oup = null;

			}
			
			oupMain(user,callback,account);

			TAuth.data.setById(account.id,account);
			
		} else if ("temp".equals(params[0])) {

			setPrivatePoint(user,POINT_OUP_SET,new OupSet(callback,account,"temp"));
			
			callback.edit("请发送新的消息模板 : ","\n默认模板 : " + Html.code(defaultMessage()),"\n可用变量 : " + HtmlUtil.escape(" <名称> 、 <用户名>")).withCancel().html().async();
			
		}

	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {

		if (POINT_OUP_SET.equals(point)) {

			OupSet set = (OupSet) data.with(msg);
			
			if (!msg.hasText()) {

				clearPrivatePoint(user);

				return;

			}

			set.account.oup_msg = msg.text().trim().equals(defaultMessage()) ? null : msg.text();
			
			clearPrivatePoint(user);
			
			TAuth.data.setById(set.account.id,set.account);
			

		}

	}

}
