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
import cn.hutool.core.util.StrUtil;

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

		BotLog.info("回复推文 : " + str);
		
	}

	@Override
	public void onStatus(final Status status) {

		if (status.getText().endsWith(" @" + screenName)) {

			if (status.getInReplyToStatusId() == -1) {
				
				return;
	
			}
			
			try {

				Status replyTo = api.showStatus(status.getInReplyToStatusId());

				MediaEntity[] medias = replyTo.getMediaEntities();

				if (medias.length == 0) {
					
					mkReply(status,"[媒体下载] 这条推文没有视频/GIF");
					
					return;
					
				}

				StringBuilder urls = new StringBuilder();

				for (MediaEntity entry : medias) {

					MediaEntity.Variant[] varints = entry.getVideoVariants();

					for (MediaEntity.Variant variant : varints) {

						urls.append("\n");
						
						if (variant.getUrl().contains("mp4")) {
							
							urls.append("[mp4 ").append(StrUtil.subBetween(variant.getUrl(),"vid/","/")).append("] ");
							
						} else {
							
							urls.append("[").append(StrUtil.subBetween(variant.getUrl(),".","?")).append("] ");
							
						}
						
						urls.append(variant.getUrl());

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
