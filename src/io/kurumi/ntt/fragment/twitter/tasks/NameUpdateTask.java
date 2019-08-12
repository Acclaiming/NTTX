package io.kurumi.ntt.fragment.twitter.tasks;

import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.NTT;
import java.util.Date;
import java.util.Random;
import java.util.TimerTask;
import twitter4j.TwitterException;

public class NameUpdateTask extends TimerTask {

	public static void start() {
		
		BotFragment.mainTimer.schedule(new NameUpdateTask(),new Date(),15 * 60 * 1000L);
		
	}
	
	@Override
	public void run() {
		
		for (TAuth account : TAuth.data.getAll()) {
			
			if (account.anu == null) continue;
			
			try {
				
				UserArchive archive = UserArchive.save(account.createApi().updateProfile(randomString(14,true,true,true),null,null,null));

				// new Send(account.user,"更改到 : " + archive.name).async();
				
			} catch (TwitterException e) {
				
				new Send(account.user,NTT.parseTwitterException(e)).async();
				
			}

		}
		
	}
	
	private static Random random = new Random();
	
	public static String randomString(int length,boolean lowEnglish,boolean upperEnglish,boolean number) {

        String baseString = "";

        if (lowEnglish) baseString += "abcdefghijklmnopqrstuvwxyz";

		if (upperEnglish) baseString += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

		if (number) baseString += "0123456789";

        StringBuilder sb = new StringBuilder();

		for (int i = 0; i < length; i++) {

            sb.append(baseString.charAt(random.nextInt(baseString.length())));

        }

        return sb.toString();

    }

}
