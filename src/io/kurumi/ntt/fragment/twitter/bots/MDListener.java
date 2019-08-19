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

public class MDListener implements StatusListener {

	private TAuth account;
	private Twitter api;
	private String screenName;

	public MDListener(TAuth account) {

		this.account = account;
		this.api = account.createApi();
		this.screenName = account.archive().screenName;

	}

	void mkReply(Status replyTo,String str) throws TwitterException {

		String reply = "@" + replyTo.getUser().getScreenName() + " ";

		api.updateStatus(new StatusUpdate(reply + " " + str).inReplyToStatusId(replyTo.getId()));

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

				StringBuilder urls = new StringBuilder();

				for (MediaEntity entry : medias) {

					MediaEntity.Variant[] varints = entry.getVideoVariants();

					for (MediaEntity.Variant variant : varints) {

						urls.append("\n\n");

						if (variant.getUrl().contains("mp4")) {

							if (variant.getUrl().contains("?")) {

								urls.append("[mp4 ").append(StrUtil.subBetween(variant.getUrl(),"vid/","/")).append("] ");

							} else {

								urls.append("[mp4] ");

							}

						} else {

							urls.append("[").append(StrUtil.subBefore(StrUtil.subAfter(variant.getUrl(),".",true),"?",false)).append("] ");

						}

						urls.append(variant.getUrl());

					}

				}

				if (urls.toString().isEmpty()) {

					mkReply(status,"This tweet has no video/GIF");

					return;

				}


				mkReply(status,"Download Links :\n" + urls.toString());


			} catch (TwitterException e) {
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
