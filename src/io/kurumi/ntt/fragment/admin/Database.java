package io.kurumi.ntt.fragment.admin;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.db.BotDB;

public class Database extends Fragment {

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		BotDB.client.fsyncAndLock();
		
	}
	
}
