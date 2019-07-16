package io.kurumi.ntt.fragment.group;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;

public class RemoveKeyboard extends Fragment {

		@Override
		public void init(BotFragment origin) {
				
				super.init(origin);
				
				registerFunction("remove_keyboard");
				
		}

		@Override
		public int checkFunctionContext(UserData user,Msg msg,String function,String[] params) {
				
				return FUNCTION_GROUP;
				
	 }

		@Override
		public void onFunction(UserData user,Msg msg,String function,String[] params) {
				
				msg.send("已经移除键盘 :)").failedWith();
				
   }
		
}
