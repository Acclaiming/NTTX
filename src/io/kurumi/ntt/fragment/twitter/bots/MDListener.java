package io.kurumi.ntt.fragment.twitter.bots;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.NTT;
import twitter4j.*;

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

                    mkReply(status,"这条推文没有包含 视频 / GIF");

                    return;

                }


                mkReply(status,"媒体下载链接 :\n" + urls.toString());


            } catch (TwitterException e) {

				try {

					if (e.getErrorCode() == 136) {

						mkReply(status,StrUtil.format("對不起，無法查看 @{} 的推文，因為被封鎖了。",status.getInReplyToScreenName()));

						
					} else {

						mkReply(status,"对不起，推文无法取得，因为 : " + NTT.parseTwitterException(e));

					}

                } catch (TwitterException ex) {
                }

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
