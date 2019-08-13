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
		
		String message = "新关注者 :";
		
		Twitter api = account.createApi();
		
		if (account.fo_new == null) {

			message += "暂时没有";

		} else {
			
			for (Long id : account.fo_new) {
				
				/*
				
				try {

					User follower = api.showUser(id);

					UserArchive archive = UserArchive.save(follower);

					Relationship ship = api.showFriendship(account.id,id);
						
						msg.append(ship.isSourceFollowingTarget() ? "已关注的 " : "").append(archive.urlHtml()).append(" #").append(archive.screenName).append(" 关注了你 :)").append(parseStatus(api,follower));

						if (auth.multiUser()) msg.append("\n\n账号 : #").append(auth.archive().screenName);

						new Send(auth.user,msg.toString()).html().point(0,archive.id);
						
					AutoTask.onNewFollower(auth,api,archive,ship);

				} catch (TwitterException e) {
				}

				*/
				
				
			}
			
		}
		
	}

}
