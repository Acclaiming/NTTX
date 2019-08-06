package io.kurumi.ntt.fragment.twitter.ui;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.tasks.MentionTask;
import io.kurumi.ntt.fragment.twitter.tasks.TimelineTask;
import io.kurumi.ntt.model.Callback;
import io.kurumi.ntt.model.request.ButtonMarkup;
import java.util.Date;
import java.util.Timer;

public class TimelineMain extends Fragment {

	public static Timer tlTimer = new Timer();
	
	public static void start() {

		tlTimer.scheduleAtFixedRate(new MentionTask(),new Date(),30 * 1000);
        tlTimer.scheduleAtFixedRate(new TimelineTask(),new Date(),3 * 60 * 1000);

	}
	
	public static void stop() {
		
		tlTimer.cancel();
		
	}

	public static final String POINT_TL = "twi_tlui";

    final String POINT_SETTING_TIMELINE = "twi_tl";
	final String POINT_SETTING_TL_CONF = "twi_tc";
    final String POINT_SETTING_MENTION = "twi_mention";
	final String POINT_SETTING_MDB = "twi_mdb";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(
		
			POINT_TL,
			
			POINT_SETTING_TIMELINE,
			POINT_SETTING_TL_CONF,
			
			POINT_SETTING_MENTION,
			
			POINT_SETTING_MDB);

    }

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {

		if (params.length == 0 || !NumberUtil.isNumber(params[0])) return;

		long accountId = NumberUtil.parseLong(params[0]);

		TAuth account = TAuth.getById(accountId);

		if (account == null) {

			callback.alert("无效的账号 .");

			callback.delete();

			return;

		}

		if (POINT_TL.equals(point)) {

			tlMain(user,callback,account);

		} else {

			setConfig(user,callback,point,account);

		}

	}

	void tlMain(UserData user,Callback callback,TAuth account) {

		String message = "时间流与回复流选单 : [ " + account.archive().name + " ]";

		ButtonMarkup config = new ButtonMarkup();

		config.newButtonLine()
			.newButton("时间流")
			.newButton(account.tl != null ? "✅" : "☑",POINT_SETTING_TIMELINE,account.id);

		config.newButtonLine("时间流设定 >>",POINT_SETTING_TL_CONF,account.id);
		
		config.newButtonLine()
			.newButton("回复流")
			.newButton(account.mention != null ? "✅" : "☑",POINT_SETTING_MENTION,account.id);

		if (user.admin()) {

			config.newButtonLine()
				.newButton("下载机器人")
				.newButton(account.mdb != null ? "✅" : "☑",POINT_SETTING_MDB,account.id);

		}

		config.newButtonLine("🔙",AccountMain.POINT_ACCOUNT,account.id);

		callback.edit(message).buttons(config).async();

	}

	void setConfig(UserData user,Callback callback,String point,TAuth account) {

		if (POINT_SETTING_TIMELINE.equals(point)) {

			if (account.tl == null) {

				account.tl = true;

				callback.text("✅ 已开启");

			} else {

				account.tl = null;
				account.tl_offset = null;

				callback.text("✅ 已关闭");

			}

		} else if (POINT_SETTING_MENTION.equals(point)) {

			if (account.mention == null) {

				account.mention = true;

				callback.text("✅ 已开启");

			} else {

				account.mention = null;
				account.mention_offset = null;
				account.rt_offset = null;

				callback.text("✅ 已关闭");

			}

		} else if (POINT_SETTING_MDB.equals(point)) {

			if (account.mdb == null) {

				account.mdb = true;

				callback.text("✅ 已开启");

			} else {

				account.mdb = null;

				callback.text("✅ 已关闭");

			}

		}

		TAuth.data.setById(account.id,account);

		tlMain(user,callback,account);

	}

}
