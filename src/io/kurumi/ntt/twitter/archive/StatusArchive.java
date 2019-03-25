package io.kurumi.ntt.twitter.archive;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.model.data.IdDataModel;
import java.util.LinkedList;
import twitter4j.Status;

public class StatusArchive extends IdDataModel {

	public Long createdAt;

	public String text;
	
	public Long from;
	
	public Long inReplyToStatusId;
	
	public Long quotedToStatusId;
	
	public LinkedList<String> mediaUrls;
    
    public Boolean isRetweet;
    
    public Long retweetedStatusId;
    
	@Override
	protected void init() {

		new Status(),

	}

	@Override
	protected void load(JSONObject obj) {
		// TODO: Implement this method
	}

	@Override
	protected void save(JSONObject obj) {
		// TODO: Implement this method
	}


	public StatusArchive(JSONObject obj) {}

}
