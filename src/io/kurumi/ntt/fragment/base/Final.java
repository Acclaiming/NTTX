package io.kurumi.ntt.fragment.base;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;

public class Final extends Fragment {

	@Override
	public void onMsg(UserData user,Msg msg) {
		
		if (msg.isPrivate()) {
			
			origin.onFinalMsg(user,msg);
			
		}
		
	}

}
