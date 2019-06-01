package io.kurumi.ntt.funcs.admin;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import java.security.acl.*;
import java.util.*;

public class DelMsg extends Fragment {

	@Override
	public boolean onGroup(UserData user,Msg msg) {
		
		if (!"del".equals(msg.command())) return false;
		
		if (!user.developer()) return false;
		
		msg.delete();
		
		if (msg.isReply()) {
			
			msg.replyTo().delete();
			
		}
		
		return true;
		
	}
	
	
}
