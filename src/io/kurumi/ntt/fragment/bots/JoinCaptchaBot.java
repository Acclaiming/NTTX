package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.fragment.group.GroupFunction;
import io.kurumi.ntt.fragment.group.GroupOptions;
import io.kurumi.ntt.fragment.group.JoinCaptcha;

public class JoinCaptchaBot extends UserBotFragment {

		@Override
		public void reload() {
				
				super.reload();
				
				addFragment(new GroupOptions());
				addFragment(new GroupFunction());
				addFragment(new GroupAdmin());
				addFragment(new JoinCaptcha());
				
		}
		
		

}
