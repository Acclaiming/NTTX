package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import java.util.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.db.*;
import twitter4j.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.utils.*;

public class DebugStatus extends TwitterFunction {

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		if (params.length == 0) {
			
			msg.send("invailed status id").exec();
			
			return;
			
		}
		
		try {
			
			Status status = account.createApi().showStatus(NumberUtil.parseLong(params[0]));

			msg.send(status.toString()).exec();
			
		} catch (TwitterException e) {
			
			msg.send(NTT.parseTwitterException(e)).exec();
			
		}

	}

	@Override
	public void functions(LinkedList<String> names) {
		
		names.add("get_status");
		
	}

}
