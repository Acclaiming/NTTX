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

    final String POINT_TIMELINE = "twi_tl";

	final String POINT_TL_CONF = "twi_tc";

	final String POINT_TL_DN = "twi_dn";
	final String POINT_TL_NS = "twi_ns";
	final String POINT_TL_NA = "twi_na";
	final String POINT_TL_NR = "twi_nr";
	final String POINT_TL_NT = "twi_nt";
	final String POINT_TL_NESU = "twi_nesu";

    final String POINT_SETTING_MENTION = "twi_mention";
	final String POINT_SETTING_MDB = "twi_mdb";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerCallback(

			POINT_TL,

			POINT_TIMELINE,
			POINT_TL_CONF,
			
			POINT_TL_DN,
			POINT_TL_NS,
			POINT_TL_NA,
			POINT_TL_NR,
			POINT_TL_NT,
			POINT_TL_NESU,

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

		} else if (POINT_TL_CONF.equals(point)) {

			tlConf(user,callback,account);

		} else {

			setConfig(user,callback,point,account);

		}

	}

	void tlMain(UserData user,Callback callback,TAuth account) {

		String message = "时间流与回复流选单 : [ " + account.archive().name + " ]";

		ButtonMarkup config = new ButtonMarkup();

		config.newButtonLine()
			.newButton("时间流")
			.newButton(account.tl != null ? "✅" : "☑",POINT_TIMELINE,account.id);

		config.newButtonLine("时间流设定 >>",POINT_TL_CONF,account.id);

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

	void tlConf(UserData user,Callback callback,TAuth account) {

		String message = "时间流内容不通知设置 : [ " + account.archive().name + " ]";

		ButtonMarkup config = new ButtonMarkup();

		config.newButtonLine()
			.newButton("只看特别关注")
			.newButton(account.tl_dn != null ? "✅" : "☑",POINT_TL_DN,account.id);
		
		config.newButtonLine()
			.newButton("不看推文")
			.newButton(account.tl_ns != null ? "✅" : "☑",POINT_TL_NS,account.id);

		config.newButtonLine()
			.newButton("不看回复")
			.newButton(account.tl_nr != null ? "✅" : "☑",POINT_TL_NR,account.id);

		config.newButtonLine()
			.newButton("不看转推")
			.newButton(account.tl_nt != null ? "✅" : "☑",POINT_TL_NT,account.id);

		config.newButtonLine()
			.newButton("不看自动推文")
			.newButton(account.tl_na != null ? "✅" : "☑",POINT_TL_NA,account.id);
		
		config.newButtonLine()
			.newButton("不看烂↑俗↓")
			.newButton(account.tl_nesu != null ? "✅" : "☑",POINT_TL_NESU,account.id);

		config.newButtonLine("🔙",POINT_TL,account.id);

		callback.edit(message).buttons(config).async();
		

	}

	void setConfig(UserData user,Callback callback,String point,TAuth account) {

		if (POINT_TIMELINE.equals(point)) {

			if (account.tl == null) {

				account.tl = true;

			} else {

				account.tl = null;
				account.tl_offset = null;

			}
			
			tlMain(user,callback,account);

		} else if (POINT_SETTING_MENTION.equals(point)) {

			if (account.mention == null) {

				account.mention = true;

			} else {

				account.mention = null;
				account.mention_offset = null;
				account.rt_offset = null;

			}
			
			tlMain(user,callback,account);

		} else if (POINT_SETTING_MDB.equals(point)) {

			if (account.mdb == null) {

				account.mdb = true;

			} else {

				account.mdb = null;

			}
			
			tlMain(user,callback,account);

		} else if (POINT_TL_DN.equals(point)) {

			if (account.tl_dn == null) {

				account.tl_dn = true;

			} else {

				account.tl_dn = null;

			}

			tlConf(user,callback,account);
			
		} else if (POINT_TL_NS.equals(point)) {

			if (account.tl_ns == null) {

				account.tl_ns = true;

			} else {

				account.tl_ns = null;

			}
			
			tlConf(user,callback,account);

		} else if (POINT_TL_NA.equals(point)) {

			if (account.tl_na == null) {

				account.tl_na = true;

			} else {

				account.tl_na = null;

			}

			tlConf(user,callback,account);
			
		} else if (POINT_TL_NR.equals(point)) {

			if (account.tl_nr == null) {

				account.tl_nr = true;

			} else {

				account.tl_nr = null;

			}
			
			tlConf(user,callback,account);

		} else if (POINT_TL_NT.equals(point)) {

			if (account.tl_nt == null) {

				account.tl_nt = true;

			} else {

				account.tl_nt = null;

			}
			
			tlConf(user,callback,account);

		} else if (POINT_TL_NESU.equals(point)) {

			if (account.tl_nesu == null) {

				account.tl_nesu = true;

			} else {

				account.tl_nesu = null;

			}
			
			tlConf(user,callback,account);

		}

		TAuth.data.setById(account.id,account);

	}

}
