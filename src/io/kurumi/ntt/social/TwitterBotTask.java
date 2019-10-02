package io.kurumi.ntt.social;

import io.kurumi.ntt.fragment.twitter.TAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class TwitterBotTask {
	
	public TAuth account;
	public Twitter api;

	public TwitterBotTask(TAuth account) {
		
		this.account = account;
		this.api = account.createApi();
		
	}
	
	public int status = 0;
	
	public void run() throws TwitterException {
		
		User user = api.verifyCredentials();

		if (user.getFriendsCount() - user.getFollowersCount() > 1000) {
			
			status = -2;
			
		} else if (user.getFriendsCount() - user.getFollowersCount() > 500) {

			status = -1;

		} else if (user.getFriendsCount() - user.getFollowersCount() < 100) {

			status = 0;

		} else if (user.getFollowersCount() - user.getFriendsCount() > 0) {

			status = 1;

		}
		
		
	}
	
}
