package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.twitter.*;

public class Users extends Fragment {

	@Override
	public boolean onMsg(UserData user,Msg msg) {
		
		if (!user.developer() || !"users".equals(msg.command())) return false;
		
		StringBuilder export = new StringBuilder();
		
		int count = 0;
		
		for (TAuth auth : TAuth.data.collection.find()) {
			
			count ++;
			
			export.append(UserData.get(auth.user).userName()).append(" -> ").append(auth.archive().urlHtml()).append("\n");
			
			if (count == 10) {
				
				msg.send(export.toString()).html().exec();
				
				export = new StringBuilder();
				
				count = 0;
				
			}
			
		}
		
		if (count > 0) {
			
			msg.send(export.toString()).html().exec();
			
		}
		
		return true;
		
	}
	
}
