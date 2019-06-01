package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.*;

public class Alias extends Fragment {

	@Override
	public boolean onPrivate(UserData user,final Msg msg) {
		
		if (!"alias".equals(msg.command())) return false;
		
		if (msg.params().length != 1) {
			
			msg.send("/alias <botToken>").exec();
			
			return true;
			
		}
		
		new Launcher() {
		
			@Override
			public String getToken() { return msg.params()[0]; }
		
		}.silentStart();
		
		
		msg.send("started").exec();
		
		return true;
		
	}
	
}
