package io.kurumi.ntt.twitter;

import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import twitter4j.*;

import cn.hutool.json.JSONObject;
import twitter4j.conf.*;
import java.util.*;

public class TAuth extends Fragment {

	public static Rec get(UserData user) {
		
		JSONObject obj =  user.ext.getJSONObject("tauth");
		
		if (obj == null) return null;
		
		return new Rec(obj);

	}
	
	public static class Rec extends JSONObject {

		public String conToken;
		public String conSec;
		public String accToken;
		public String accSec;

		public void load() {

			conToken = getStr("consumer_key");
			conSec = getStr("consumer_secret");
			accToken = getStr("access_token");
			accSec = getStr("access_secret");

		}

		public void save() {

			put("consumer_key",conToken);
			put("consumer_secret",conSec);
			put("access_token",accToken);
			put("access_secret",accSec);

		}
		
		public Twitter api() {

			return new TwitterFactory(new ConfigurationBuilder()
									  .setOAuthAccessToken(accToken)
									  .setOAuthAccessTokenSecret(accSec)
									  .setOAuthConsumerKey(conToken)
									  .setOAuthConsumerSecret(conSec).build()).getInstance();

		}

		public Rec() {}
		public Rec(String json) { super(json);load(); }
		public Rec(JSONObject obj) { super(obj);load(); }

	}

	@Override
	public boolean onPrivMsg(UserData user,Msg msg) {

		if (!msg.isCommand()) return false;

		switch (msg.commandName()) {

			case "tauth" : tauth(user,msg);break;
			
			default : return false;

		}

		return true;

	}

	void tauth(UserData user,Msg msg) {

		if (msg.commandParms().length != 4) {

			msg.send("illegal argument : /tauth <ConsumerKey> <ConsumerSecret> <AccessToken> <AccessSecret>").exec();

			return;
			
		}
		
		Rec rec = new Rec();
	
		rec.conToken = msg.commandParms()[0];
		rec.conSec = msg.commandParms()[1];
		rec.accToken = msg.commandParms()[2];
		rec.accSec = msg.commandParms()[3];

		try {
			
			if (rec.api().verifyCredentials() == null) {
				
				msg.send("invaild token").exec();
				
				return;
				
			}
			
		} catch (TwitterException e) {
			
			msg.send("invaild token : " + e.getMessage()).exec();
			
		}
		
		user.ext.put("tauth",rec);
		
		msg.send("auth successful").exec();

	}
}
