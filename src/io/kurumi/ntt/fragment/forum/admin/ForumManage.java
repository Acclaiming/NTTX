package io.kurumi.ntt.fragment.forum.admin;

import io.kurumi.ntt.funcs.abs.Function;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.forum.ForumE;

public class ForumManage extends Function {

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("forum");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		long count = ForumE.data.countByField("owner",user.id);

		if (params.length > 0) {
			
			if ("init".equals(params[0])) {
				
				if (count == 0) {
					
					createForum(user,msg);
					
				} else {
					
					msg.send(
					
				}
				
			}
			
		}
		
	}
	
	void createForum(UserData user,Msg msg) {
			
		
		
	}
	
}
