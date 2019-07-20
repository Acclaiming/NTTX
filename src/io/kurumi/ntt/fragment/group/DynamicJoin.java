package io.kurumi.ntt.fragment.group;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;

public class DynamicJoin extends Fragment {

		final String PAYLOAD_JOIN = "join";
		
		@Override
		public void init(BotFragment origin) {
				
				super.init(origin);
				
				registerPayload(PAYLOAD_JOIN);
				
		}

		@Override
		public void onPayload(UserData user,Msg msg,String payload,String[] params) {
		}
		
}
