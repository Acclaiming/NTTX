package io.kurumi.ntt.fragment.twitter.auto;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Callback;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.ButtonMarkup;
import io.kurumi.ntt.fragment.twitter.TAuth;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.BotFragment;

public class AutoUI extends Fragment {

    public static Data<AutoSetting> autoData = new Data<AutoSetting>(AutoSetting.class);
	,
    final String POINT_SETTING_ACCEPT = "auto_accept";
    final String POINT_SETTING_FOBACK = "auto_foback";

	public void init(BotFragment origin) {

		super.init(origin);

		registerFunction("auto");

        registerCallback(POINT_SETTING_ACCEPT,POINT_SETTING_FOBACK);

    }

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {

		requestTwitter(user,msg);

	}

    @Override
    public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        AutoSetting setting = autoData.getById(account.id);

        if (setting == null) {

            setting = new AutoSetting();

            setting.id = account.id;

        }

        msg.send("自动处理设置... (按钮UI (❁´▽`❁)").buttons(makeSettings(setting,account.id)).exec();

    }

    ButtonMarkup makeSettings(final AutoSetting setting,final long accountId) {

        return new ButtonMarkup() {{

				// newButtonLine((setting.archive ? "「 关闭" : "「 开启") + " 时间线推文存档 」", POINT_SETTING_AECHIVE, accountId);
				// newButtonLine((setting.accept ? "「 关闭" : "「 开启") + " 自动通过关注请求 」",POINT_SETTING_ACCEPT,accountId);
				newButtonLine((setting.foback ? "「 关闭" : "「 开启") + " 关注新关注者 」",POINT_SETTING_FOBACK,accountId);

				// newButtonLine((setting.foback ? "「 关闭" : "「 开启") + " 取关新取关者 」",POINT,accountId);


			}};

    }

    @Override
    public void onCallback(UserData user,Callback callback,String point,String[] params) {

        long accountId = Long.parseLong(params[0]);

        AutoSetting setting = autoData.containsId(accountId) ? autoData.getById(accountId) : new AutoSetting();

        setting.id = accountId;

        boolean target = true;

        switch (point) {

				// case POINT_SETTING_: target = setting.archive = !setting.archive;break;
		case POINT_SETTING_ACCEPT : 
			target = setting.accept = !setting.accept;
			break;
            case POINT_SETTING_FOBACK:
                target = setting.foback = !setting.foback;
     break;

        }

        if (setting.foback || setting.accept) {

            autoData.setById(accountId,setting);

        } else {

            autoData.deleteById(accountId);

        }

        callback.text("已" + (target ? "开启" : "关闭") + " ~");
        callback.editMarkup(makeSettings(setting,accountId));


    }

    public static class AutoSetting {

        public Long id;


		public boolean accept = false;
        public boolean foback = false;
        public boolean reply = false;

    }


}
