package io.kurumi.ntt.fragment.twitter.bots;

import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.BotLog;
import twitter4j.MediaEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserMentionEntity;

public class VideoDownloadListener implements StatusListener {

	private TAuth account;
	private Twitter api;
	private String screenName;

	public VideoDownloadListener(TAuth account) {

		this.account = account;
		this.api = account.createApi();
		this.screenName = account.archive().screenName;

	}

	void mkReply(Status replyTo,String str) throws TwitterException {

		String reply = "@" + replyTo.getUser().getScreenName() + " ";

		if (replyTo.getUserMentionEntities().length != 0) {

			for (UserMentionEntity mention : replyTo.getUserMentionEntities()) {

				if (!account.id.equals(mention)) {

					reply = reply + "@" + mention.getScreenName() + " ";

				}

			}

		}

		api.updateStatus(new StatusUpdate(reply + " " + str).inReplyToStatusId(replyTo.getId()));

	}

	@Override
	public void onStatus(final Status status) {

		BotFragment.asyncPool.execute(new Runnable() {

				@Override
				public void run() {
					
					process(status);
					
				}
				
			});
		
	}
	
	public void process(Status status) {
		
		if (status.getInReplyToStatusId() != -1 && status.getText().contains("@" + screenName)) {

			try {

				Status replyTo = api.showStatus(status.getInReplyToStatusId());

				MediaEntity[] medias = replyTo.getMediaEntities();

				if (medias.length == 0) {
					
					mkReply(status,"[媒体下载] 对视频/GIF 回复 @" + screenName + " BOT会回复所有下载链接");
					
					return;
					
				}

				StringBuilder urls = new StringBuilder();

				for (MediaEntity entry : medias) {

					MediaEntity.Variant[] varints = entry.getVideoVariants();

					for (MediaEntity.Variant variant : varints) {

						urls.append("\n").append(variant.getUrl());

					}

				}

				mkReply(status,"[媒体下载] 所有下载链接 :\n" + urls.toString());


			} catch (TwitterException e) {

				BotLog.error("[VDB]",e);

			}

		}

	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	}

	@Override
	public void onScrubGeo(long userId,long upToStatusId) {
	}

	@Override
	public void onStallWarning(StallWarning warning) {
	}

	@Override
	public void onException(Exception ex) {
	}




}
