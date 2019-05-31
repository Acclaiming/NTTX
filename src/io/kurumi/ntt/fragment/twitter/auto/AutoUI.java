package io.kurumi.ntt.fragment.twitter.auto;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.model.request.*;

public class AutoUI extends TwitterFunction {

	public static class AutoSetting {

		public Long id;

		public boolean like = false;
		
		public boolean foback = false;

	}

	final String POINT_SETTING_LIKE = "auto|like";
	final String POINT_SETTING_FOBACK= "auto|foback";

	@Override
	public int target() {
		
		return Private;
		
	}

	@Override
	public void points(LinkedList<String> points) {
		
		super.points(points);
		
		points.add(POINT_SETTING_LIKE);
		points.add(POINT_SETTING_FOBACK);
		
	}
	
	public static Data<AutoSetting> autoData = new Data<AutoSetting>(AutoSetting.class);

	@Override
	public void functions(LinkedList<String> names) {

		names.add("auto");

	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		AutoSetting setting = autoData.getById(account.id);

		if (setting == null) {
			
			setting = new AutoSetting();
			
			setting.id = account.id;
			
		}

        msg.send("自动处理设置... (按钮UI (❁´▽`❁)").buttons(makeSettings(setting,account.id)).exec();

    }

    ButtonMarkup makeSettings(final AutoSetting setting,final long accountId) {

        return new ButtonMarkup() {{

                newButtonLine((setting.like ? "「 关闭" : "「 开启") + " 时间线打心 」",POINT_SETTING_LIKE,accountId);
                newButtonLine((setting.foback ? "「 关闭" : "「 开启") + " 自动回Fo 」",POINT_SETTING_FOBACK,accountId);

            }};

    }

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {
		
		long accountId = Long.parseLong(params[0]);
		
		AutoSetting setting = autoData.containsId(accountId) ? autoData.getById(accountId) : new AutoSetting();
		
		setting.id = accountId;
		
		boolean target = true;
		
		switch (point) {
			
			case POINT_SETTING_LIKE : target = setting.like = !setting.like;break;
			case POINT_SETTING_FOBACK : target = setting.foback = !setting.foback;break;
			
		}
		
		if (setting.like || setting.foback) {
			
			autoData.setById(accountId,setting);
			
		} else {
			
			autoData.deleteById(accountId);
			
		}
		
		callback.text("已" + (target ? "开启" : "关闭") + " ~");
		callback.editMarkup(makeSettings(setting,accountId));
		
		
	}


}
