package io.kurumi.ntt.fragment.twitter.list;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.db.PointData;

public class ListEdit extends Fragment {

	final String POINT_OPEN = "list_open";
	
	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("csv");
		
		registerPoint(POINT_OPEN);
		
	}
	
	class EditList extends PointData {
		
		int mode = 0;
		int type = 0;
		long[] array;
		
	}
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		if (params.length > 0) {
			
			
		}
		
		EditList edit = new EditList();

		setPrivatePoint(user,POINT_OPEN,edit);
		
		msg.send("现在发送需要编辑的文件 :").exec(edit);
		
	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,PointData data) {
	
		EditList edit = (EditList) data;
		
		if (edit.type == 0) {
			
			if (msg.doc() == null || !msg.doc().fileName().endsWith(".csv")) {
				
				msg.send("请发送需要打开的 .csv 文件").exec();
				
			}
			
		}
		
	}
	
}
