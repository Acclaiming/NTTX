package io.kurumi.ntt.fragment.twitter.list;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.request.Keyboard;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.BotFragment;

public class ListExport extends Fragment {

	final String FOLLOWING = "关注中列表";
	final String FOLLOWER = "关注者列表";
	final String BLOCK = "屏蔽列表";
	final String MUTE = "静音列表";
	final String USER = "其他列表";
	
	final String POINT_LIST_EXPORT = "list_export";

	@Override
	public void init(BotFragment origin) {

		super.init(origin);
		
		registerFunction("export");
		
		registerPoints(POINT_LIST_EXPORT);
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		setPrivatePoint(user,POINT_LIST_EXPORT);
		
		msg
			.send("请选择导出的列表 :","将会以 .csv 官方格式导出")
			.keyboard(new Keyboard() {{

					newButtonLine().newButton(FOLLOWING).newButton(FOLLOWER);
					newButtonLine().newButton(BLOCK).newButton(MUTE);

					newButtonLine(USER);

				}})
			.withCancel()
			.exec();
		
	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {
		
		
		
	}
	
}
