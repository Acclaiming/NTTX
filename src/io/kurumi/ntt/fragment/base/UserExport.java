package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;
import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.model.request.ButtonMarkup;

public class UserExport extends Fragment {

	final String POINT_SHOW_USER = "show_user";
	
    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("user");

        registerPayload("user");

    }

	@Override
	public void onPayload(UserData user,Msg msg,String payload,String[] params) {
		
		onFunction(user,msg,payload,params);
		
	}
	
    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

		if (params.length == 0 || !NumberUtil.isNumber(params[0])) {
			
			msg.invalidParams("userId").async();
			
			return;
			
		}
		
		String message = "点击按钮打开用户 :)";
		
		ButtonMarkup showUser = new ButtonMarkup();
		
		showUser.newButtonLine("打开",POINT_SHOW_USER,NumberUtil.parseInt(params[0]));
		
		msg.send(message).buttons(showUser).async();
		
    }

	@Override
	public void onCallback(UserData user,Callback callback,String point,String[] params) {
		
		if (params.length == 0 || !NumberUtil.isNumber(params[0])) {
			
			callback.invalidQuery();
			
			return;
			
		}
	
		callback.url("tg://user?id=" + params[0]);
		
	}

}
