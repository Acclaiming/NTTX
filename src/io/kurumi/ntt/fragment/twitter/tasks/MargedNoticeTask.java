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

					if (ship.isSourceFollowedByTarget()) {

						continue;

					}

					message += "\n" + archive.urlHtml() + " #" + archive.screenName;
					
					if (ship.isSourceFollowingTarget()) message += " [ 单向取关 ]";
					else if (archive.isProtected) message += " [ 锁推 ]";
					
				} catch (TwitterException e) {

					UserArchive archive = UserArchive.get(id);

					if (archive.isDisappeared) return;

					UserArchive.saveDisappeared(id);

					if (!notice && auth.fo_marge == null) return;

					StringBuilder msg = new StringBuilder(archive != null ? archive.urlHtml() : "无记录的用户 : (" + id + ")").append(" 取关了你\n\n状态异常 : ").append(NTT.parseTwitterException(e));

					if (auth.multiUser()) msg.append("\n\n账号 : #").append(auth.archive().screenName);

					new Send(auth.user,msg.toString()).html().point(0,id);

				}
				
			}
			
		}
		
	}

}
