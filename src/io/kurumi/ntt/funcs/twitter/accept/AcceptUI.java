package io.kurumi.ntt.funcs.twitter.accept;

import io.kurumi.ntt.funcs.abs.*;
import java.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import com.pengrad.telegrambot.response.*;

public class AcceptUI extends Function {

    public static Data<AcceptSetting> data = new Data<AcceptSetting>(AcceptSetting.class);

    public static class AcceptSetting {

        public long id;

        public boolean accept = false;
		public boolean reject = false;
        public boolean followback = false;
        public boolean unfollowback = false;
		public boolean blockback = false;
		
    }
	
	final String POINT_SETTING_ACCEPT = "a|a";
	final String POINT_SETTING_REJECT = "a|a";
	
	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("accept");
		
	}

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
		
	}

}
