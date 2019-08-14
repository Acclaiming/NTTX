package io.kurumi.ntt.fragment.twitter.tasks;

import io.kurumi.ntt.fragment.twitter.TAuth;
import java.util.HashMap;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.utils.BotLog;

public class DeviceNotificationFilter {
	
	public static HashMap<Long,HashMap<Long,Boolean>> allShips = new HashMap<>();
	public static HashMap<Long,HashMap<Long,Long>> allLastUpdates = new HashMap<>();
	
	public static boolean isDeviceNotificationEnabled(TAuth auth,Twitter api,Long target) {

		HashMap<Long, Boolean> ships = allShips.get(auth.id);
		HashMap<Long, Long> lastUpdates = allLastUpdates.get(auth.id);

		if (ships == null) {
			
			ships = new HashMap<>();
			
			allShips.put(auth.id,ships);
			
		}
		
		if (lastUpdates == null) {
			
			lastUpdates = new HashMap<>();
			
			allLastUpdates.put(auth.id,lastUpdates);
			
		}
		
		if (!ships.containsKey(target) || !lastUpdates.containsKey(target) || System.currentTimeMillis() -  lastUpdates.get(target) > 30 * 60 * 1000L) {

			try {

				Relationship ship = api.showFriendship(auth.id,target);

				boolean enabled = ship.isSourceNotificationsEnabled();

				ships.put(target,enabled);

				lastUpdates.put(target,System.currentTimeMillis());

				return enabled;
				
			} catch (TwitterException e) {
				
				BotLog.info("DNF",e);
				
				if (!ships.containsKey(target)) return false;

			}

		}

		return ships.get(target);

	}

}
