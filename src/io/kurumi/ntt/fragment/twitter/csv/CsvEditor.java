package io.kurumi.ntt.fragment.twitter.csv;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;

public class CsvEditor extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerFunction("csv");
		
	}

	@Override
	public int checkFunction() {
		
		return FUNCTION_PRIVATE;
		
	}

	@Override
	public int checkFunction(UserData user,Msg msg,String function,String[] params) {
		
		return PROCESS_SYNC;
		
	}

	final String POINT_INPUT_ORIGIN = "csv_open";
	
	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		setPrivatePoint(user,POINT_INPUT_ORIGIN);
		
		msg.send("欢迎使用CSV工具 现在发送 .csv 文件。").exec();
		
	}

	@Override
	public void onPoint(UserData user,Msg msg,String point,Object data) {
		
		if (POINT_INPUT_ORIGIN.equals(point)) {
			
			
			
		}
		
	}
	
	
}
