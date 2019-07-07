package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;

public class Final extends Fragment {

	@Override
	public int checkMsg(UserData user,Msg msg) {
		
		return PROCESS_ASYNC;
		
	}
	
	@Override
	public void onMsg(UserData user,Msg msg) {
		
		if (msg.isPrivate()) {
			
			origin.onFinalMsg(user,msg);
			
		}
		
	}

}
