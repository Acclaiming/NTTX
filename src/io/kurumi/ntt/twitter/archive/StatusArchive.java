
package io.kurumi.ntt.twitter.archive;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.utils.Html;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class StatusArchive {

    public Long id;

	public Long createdAt;

	public String text;

	public Long from;

	public Long inReplyToStatusId;

	public String inReplyToScreenName;

    public Long inReplyToUserId;

	public Long quotedStatusId;

	public LinkedList<String> mediaUrls;

    public Boolean isRetweet;

    public Long retweetedStatus;

    public void read(Status status) {

        createdAt = status.getCreatedAt().getTime();
        text = status.getText();

        from = status.getUser().getId();

        BotDB.saveUser(status.getUser());

        inReplyToStatusId = status.getInReplyToStatusId();

        inReplyToScreenName = status.getInReplyToScreenName();

        inReplyToUserId = status.getInReplyToUserId();

        quotedStatusId = status.getQuotedStatusId();

        if (quotedStatusId != -1 && !BotDB.statusExists(quotedStatusId)) {

            BotDB.saveStatus(status.getQuotedStatus());

        }

        mediaUrls = new LinkedList<>();

        for (MediaEntity media : status.getMediaEntities()) {

            mediaUrls.add(media.getMediaURL());

        }

        isRetweet = status.isRetweet();

        if (isRetweet) {

            retweetedStatus = status.getRetweetedStatus().getId();

            if (!BotDB.statusExists(retweetedStatus)) {

                BotDB.saveStatus(status.getRetweetedStatus());

            }

        } else {

            retweetedStatus = -1L;

        }

	}

    public UserArchive user() {

        return BotDB.getUser(from);

    }

    public String url() {

        return "https://twitter.com/" + user().screenName + "/status/" + id;

    }

    public String htmlURL() {

        return Html.a(StrUtil.padAfter(text,5,"..."),url());

    }

    public transient String split = "\n\n---------------------\n\n";

    public String toHtml() {

        return toHtml(-1);

    }

    public String toHtml(int depth) {

        StringBuilder archive = new StringBuilder();

        if (quotedStatusId != -1) {

            if (depth != 0) {
            
            StatusArchive quotedStatus = BotDB.getStatus(quotedStatusId);

            if (quotedStatus == null) {

                archive.append(quotedStatus.toHtml(depth > 0 ? depth - 1 : depth));

            } else {

                archive.append("不可用的推文");

            }

            archive.append(split);
            
            }
            
            archive.append(user().urlHtml()).append(" 的 ").append(Html.a("回复",url()));

        } else if (inReplyToStatusId != -1) {

            if (depth != 0) {
                
            StatusArchive inReplyTo = BotDB.getStatus(inReplyToStatusId);

            if (inReplyTo != null) {

                archive.append(inReplyTo.toHtml(depth > 0 ? depth - 1 : depth));

            } else {

                archive.append(notAvilableStatus(inReplyToUserId,inReplyToScreenName));

            }

            archive.append(split);
            
            }
            
            archive.append(user().urlHtml()).append(" 的 ").append(Html.a("回复",url()));


        } else if (isRetweet) {

            StatusArchive retweeted = BotDB.getStatus(retweetedStatus);

            archive.append(user().urlHtml()).append(" 转推从 " + retweeted.user().urlHtml()).append(" : ");

            archive.append(retweeted.toHtml(depth > 0 ? depth - 1 : depth));

            return archive.toString();

        } else {

            archive.append(user().urlHtml()).append(" 的 ").append(Html.a("推文",url()));

		}

        String content = text;

        if (!mediaUrls.isEmpty()) {

            content = StrUtil.subBefore(content,"https://t.co",true);

        }


        if (content.startsWith("@")) {

            LinkedList<String> inReplyTo = new LinkedList<>();

            while (content.startsWith("@")) {

                inReplyTo.add(StrUtil.subBefore(content.substring(1)," ",false));

                content = StrUtil.subAfter(content," " ,false);

            }

            Collections.reverse(inReplyTo);

            archive.append(" 给");

            boolean l = false;

            for (String replyTo : inReplyTo) {

                UserArchive user = BotDB.getUser(replyTo);

                archive.append(" ");

                if (l) archive.append("、");

                if (user != null) {

                    archive.append(user.urlHtml());

                } else {

                    archive.append(Html.a("@" + replyTo,"https://twitter.com/" + replyTo));

                } 

                l = true;

            }

        }

        archive.append("\n\n").append(content);

        if (!mediaUrls.isEmpty()) {

            archive.append("\n\n媒体文件 :");

            for (String url : mediaUrls) {

                archive.append(Html.a(" 媒体文件",url));

            }

        }

        archive.append("\n\n在 ");

        Calendar date = Calendar.getInstance();

        date.setTimeInMillis(createdAt);

        archive.append(date.get(Calendar.YEAR) - 2000).append("年").append(date.get(Calendar.MONTH)).append("月").append(date.get(Calendar.DAY_OF_MONTH)).append("日");

        archive.append(", ").append(date.get(Calendar.AM_PM) == 0 ? "上午" : "下午").append(" ").append(date.get(Calendar.HOUR)).append(":").append(date.get(Calendar.MINUTE));

        return archive.toString();

    }

    String notAvilableStatus(Long id,String screenName) {

        if (BotDB.userExists(id)) {

            return BotDB.getUser(id).urlHtml() + " 的 不可用的推文";

        } else {

            return Html.a("@" + screenName,"https://twitter.com/" + screenName) + " 的 不可用的推文";

        }

    }

    public StatusArchive loop(Twitter api) {

        String content = text;

        if (content.startsWith("@")) {

            while (content.startsWith("@")) {

                String screenName = StrUtil.subBefore(content.substring(1)," ",false);

                content = StrUtil.subAfter(content," ",false);

                if (!BotDB.userExists(screenName)) {

                    try {

                        BotDB.saveUser(api.showUser(screenName));

                    } catch (TwitterException ex) {} 

                }

            }

        }


        try {

            if (inReplyToStatusId != -1) {

                if (BotDB.statusExists(inReplyToStatusId)) {

                    BotDB.getStatus(inReplyToStatusId).loop(api);

                } else {

                    Status status = api.showStatus(inReplyToStatusId);

                    StatusArchive inReplyTo = BotDB.saveStatus(status);

                    inReplyTo.loop(api);

                }

            }

            if (quotedStatusId != -1) {

                if (BotDB.statusExists(quotedStatusId)) {

                    BotDB.getStatus(quotedStatusId).loop(api);

                } else {

                    Status status = api.showStatus(quotedStatusId);

                    StatusArchive quoted = BotDB.saveStatus(status);

                    quoted.loop(api);

                }

            }

        } catch (TwitterException ex) {}

        return this;

    }


}
