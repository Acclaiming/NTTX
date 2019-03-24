package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.*;
import java.math.*;
import cn.hutool.core.util.*;

public class TwitterLikeSearch extends Fragment {

	@Override
	public boolean onMsg(UserData user,Msg msg) {
		
		Twitter api = TAuth.get(user).createApi();

		try {
			
			int count = api.verifyCredentials().getFavouritesCount();

			api.getFavorites();

		} catch (TwitterException e) {}
		
		return false;

	}
	
}
