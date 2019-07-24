package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.twitter.archive.*;
import java.util.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.utils.*;

public class Disappeared extends Fragment {

	@Override
	public void init(BotFragment origin) {
	
		super.init(origin);
		
		registerAdminFunction("disappeared");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		LinkedList<String> list = new LinkedList<>();
		
		for (UserArchive archive : UserArchive.data.findByField("isDisappeared",true)) {
			
			if (list.size() == 20) {
				
				msg.send(ArrayUtil.join(list.toArray(),"\n\n")).html().async();
				
				list.clear();
				
			}
			
			list.add(archive.name + " : @" + archive.screenName);
			
			if (!StrUtil.isBlank(archive.bio)) {
			
			list.add(Html.code(archive.bio));
			
			}
			
		}
		
		if (!list.isEmpty()) msg.send(ArrayUtil.join(list.toArray(),"\n\n")).html().async();
		
		
	}
	
}
