package io.kurumi.ntt.twitter.archive;

import cn.hutool.core.convert.NumberChineseFormater;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.model.data.IdDataModel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import twitter4j.MediaEntity;
import twitter4j.Status;
import cn.hutool.core.convert.impl.CalendarConverter;
import java.util.Calendar;
import java.util.*;
import java.time.*;
import io.kurumi.ntt.utils.*;
import cn.hutool.core.collection.CollectionUtil;

public class StatusArchive extends IdDataModel {

    public static Factory<StatusArchive> INSTANCE = new Factory<StatusArchive>(StatusArchive.class,"twitter_archives/statuses");

    public static void saveCache(Status status) {

        StatusArchive archive = INSTANCE.getOrNew(status.getId());

        archive.read(status);

        INSTANCE.saveObj(archive);

    }

    public StatusArchive(String dirName,long id) { super(dirName,id); }

	public Long createdAt;

	public String text;

	public Long from;

	public Long inReplyToStatusId;

	public String inReplyToScreenName;

    public Long inReplyToUserId;

	public Long quotedStatusId;

	public LinkedList<String> mediaUrls;

    public Boolean isRetweet;

    public Long retweetedStatusId;

	@Override
	protected void init() {
	}

    public void read(Status status) {

        createdAt = status.getCreatedAt().getTime();
        text = status.getText();

        from = status.getUser().getId();

        UserArchive.saveCache(status.getUser());

        inReplyToStatusId = status.getInReplyToStatusId();

        inReplyToScreenName = status.getInReplyToScreenName();

        inReplyToUserId = status.getInReplyToUserId();

        quotedStatusId = status.getQuotedStatusId();

        if (quotedStatusId != -1 && !INSTANCE.exists(quotedStatusId)) {

            StatusArchive quotedStatus = INSTANCE.getOrNew(quotedStatusId);

            quotedStatus.read(status.getQuotedStatus());

            INSTANCE.saveObj(quotedStatus);

        }

        mediaUrls = new LinkedList<>();

        for (MediaEntity media : status.getMediaEntities()) {

            mediaUrls.add(media.getMediaURL());

        }

        isRetweet = status.isRetweet();

        if (isRetweet) {

            retweetedStatusId = status.getRetweetedStatus().getId();

            if (!INSTANCE.exists(retweetedStatusId)) {

                StatusArchive retweetedStatus = INSTANCE.getOrNew(retweetedStatusId);

                retweetedStatus.read(status.getRetweetedStatus());

                INSTANCE.saveObj(retweetedStatus);

            }

        } else {

            retweetedStatusId = -1L;

        }

        INSTANCE.saveObj(this);

    }

	@Override
	protected void load(JSONObject obj) {

        createdAt = obj.getLong("created_at");
        text = obj.getStr("text");
        from = obj.getLong("from");
        inReplyToStatusId = obj.getLong("in_reply_to_status_id");
        inReplyToUserId = obj.getLong("in_reply_to_user_id");
        inReplyToScreenName = obj.getStr("in_reply_to_screen_name");
        quotedStatusId = obj.getLong("quoted_status_id");

        if (!obj.isNull("media_urls")) {

            mediaUrls = new LinkedList<String>((List<String>)((Object)obj.getJSONArray("media_urls")));

        }

        isRetweet = obj.getBool("is_retweet");
        retweetedStatusId = obj.getLong("retweeted_status_id");

	}

	@Override
	protected void save(JSONObject obj) {

        obj.put("created_at",createdAt);
        obj.put("text",text);
        obj.put("from",from);
        obj.put("in_reply_to_status_id",inReplyToStatusId);
        obj.put("in_reply_to_user_id",inReplyToUserId);
        obj.put("in_reply_to_screen_name",inReplyToScreenName);
        obj.put("quoted_status_id",quotedStatusId);
        obj.put("media_urls",mediaUrls);
        obj.put("is_retweet",isRetweet);
        obj.put("retweeted_status_id",retweetedStatusId);

	}

    public UserArchive getUser() {

        return UserArchive.INSTANCE.get(from);

    }

    public String getURL() {

        return "https://twitter.com/" + getUser().screenName + "/status/" + idStr;

    }

    public String getHtmlURL() {

        return Html.a(StrUtil.padAfter(text,5,"..."),getURL());

    }

    public String split = "\n\n---------------------\n\n";

    public String toHtml() {

        StringBuilder archive = new StringBuilder();

        if (quotedStatusId != -1) {

            StatusArchive quotedStatus = INSTANCE.get(quotedStatusId);

            if (quotedStatus == null) {

                archive.append(quotedStatus.toHtml());

            } else {

                archive.append("不可用的推文");

            }

            archive.append(split).append(getUser().getHtmlURL()).append(" 的 ").append(Html.a("回复",getURL()));

        } else if (inReplyToStatusId != -1) {

            StatusArchive inReplyTo = INSTANCE.get(inReplyToStatusId);

            if (inReplyTo != null) {

                archive.append(inReplyTo.toHtml());

            } else {

                archive.append(notAvilableStatus(inReplyToUserId,inReplyToScreenName));

            }

            archive.append(split).append(getUser().getHtmlURL()).append(" 的 ").append(Html.a("回复",getURL()));


        } else if (isRetweet) {

            StatusArchive retweetedStatus = INSTANCE.get(retweetedStatusId);

            archive.append(getUser().getHtmlURL()).append(" 转推从 " + retweetedStatus.getUser().getHtmlURL()).append(" : ");

            archive.append(retweetedStatus.toHtml());

            return archive.toString();

        } else {

            archive.append(getUser().getHtmlURL()).append(" 的 ").append(Html.a("推文",getURL()));

		}
        
        String content = text;

        if (content.startsWith("@")) {

            LinkedList<String> inReplyTo = new LinkedList<>();

            while (content.startsWith("@")) {

                inReplyTo.add(StrUtil.subBefore(content.substring(1)," ",false));

                content = StrUtil.subAfter(content," " ,false);

            }

            Collections.reverse(inReplyTo);

            archive.append(" 给");

            for (String replyTo : inReplyTo) {

                archive.append(" ").append(Html.a("@" + replyTo,"https://twitter.com/" + replyTo));

            }

        }

        archive.append("\n\n").append(content);

        if (!mediaUrls.isEmpty()) {
            
            archive.append("\n\n媒体文件 :");

            for (String url : mediaUrls) {

                archive.append(Html.a(" 媒体文件",url));

            }

        }

        archive.append("\n\n 在 ").append(new Date(createdAt).toLocaleString());


        return archive.toString();

    }

    String notAvilableStatus(Long id,String screenName) {

        if (UserArchive.INSTANCE.exists(id)) {

            return UserArchive.INSTANCE.get(id).getHtmlURL() + " 的 不可用的推文";

        } else {

            return Html.a("@" + screenName,"https://twitter.com/" + screenName) + " 的 不可用的推文";

        }

    }


}
