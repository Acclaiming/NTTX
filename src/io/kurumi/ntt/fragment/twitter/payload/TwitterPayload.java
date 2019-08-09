package io.kurumi.ntt.fragment.twitter.payload;

import java.util.List;
import twitter4j.Status;
import cn.hutool.json.JSONObject;
import twitter4j.api.TweetsResources;
import java.util.LinkedList;
import twitter4j.StatusJSONImpl;

public class TwitterPayload {
	
	public Long forUserId;
	
	public List<Status> tweetCreateEvents;
	
	public TwitterPayload(String json) {
		
		JSONObject obj = new JSONObject(json);
		
		forUserId = obj.getLong("for_user_id");
		
		if (obj.containsKey("tweet_create_events")) {
			
			tweetCreateEvents = new LinkedList<>();
			
			
		}
		
	}
	
}
