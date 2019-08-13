package io.kurumi.ntt.fragment.twitter.tasks;

import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.request.Send;
import java.util.TimerTask;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class MargedNoticeTask extends TimerTask {

	@Override
	public void run() {
		
		new Thread("Marged Followers Notice Thread") {

				@Override
				public void run() {
					
					for (TAuth account : TAuth.data.getAll()) {

						if (account.fo == null || account.fo_marge != null) continue;
						
						doNotice(account);

					}
					
				}
				
			}.start();
			
			
		
	}
	
	void doNotice(TAuth account) {
		
		if (account.fo_new == null && account.fo_lost == null) return;
		
		String message = "新关注者 :";
		
		Twitter api = account.createApi();
		
		if (account.fo_new == null) {

			message += "暂时没有";

		} else {
			
			message += "\n";
			
			for (Long id : account.fo_new) {

				try {

					User follower = api.showUser(id);

					UserArchive archive = UserArchive.save(follower);

					Relationship ship = api.showFriendship(account.id,id);
						
					message += "\n" + archive.urlHtml() + " #" + archive.screenName;
					
					if (ship.isSourceFollowingTarget()) message += " [ 互相关注 ]";
					else if (archive.isProtected) message += " [ 锁推 ]";
					
					AutoTask.onNewFollower(account,api,archive,ship);
					
				} catch (TwitterException e) {
				}
				
			}
			
		}
		
		message += "\n失去关注者 :";
		
		if (account.fo_new == null) {

			message += "暂时没有";

		} else {

			message += "\n";

			for (Long id : account.fo_new) {

				try {

					User follower = api.showUser(id);

					UserArchive archive = UserArchive.save(follower);

					Relationship ship = api.showFriendship(account.id,id);

					message += "\n" + archive.urlHtml() + " #" + archive.screenName;

					if (ship.isSourceFollowingTarget()) message += " [ 互相关注 ]";
					else if (archive.isProtected) message += " [ 锁推 ]";

					AutoTask.onNewFollower(account,api,archive,ship);

				} catch (TwitterException e) {
				}

			}
			
		}
		
	}

}
