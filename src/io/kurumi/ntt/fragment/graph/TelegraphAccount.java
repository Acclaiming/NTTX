package io.kurumi.ntt.fragment.graph;

import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.telegraph.Telegraph;
import io.kurumi.telegraph.model.Account;

public class TelegraphAccount {
		
		public static Data<TelegraphAccount> data = new Data<TelegraphAccount>(TelegraphAccount.class);
		
		static { data.collection.drop(); }
	
		public static TelegraphAccount get(UserData user) {
				
				if (!data.containsId(user.id)) {
						
						Account account = Telegraph.createAccount(user.userName == null ? user.id.toString() : user.userName,user.name(),"https://t.me/NTT_X");

						if (account == null) return null;
						
						TelegraphAccount auth = new TelegraphAccount();
						
						auth.id = user.id;
						
						auth.access_token = account.access_token;
						
						auth.short_name = account.short_name;
						
						auth.author_name = account.author_name;
						
						auth.author_url = account.author_url;
						
						data.setById(auth.id,auth);
						
						return auth;
						
				}
				
				TelegraphAccount account = data.getById(user.id);
				
			
				return account;
				
		}
		
		public Long id;
		
		public String short_name;

		public String author_name;
		
		public String author_url;

		public String access_token;
		
}
