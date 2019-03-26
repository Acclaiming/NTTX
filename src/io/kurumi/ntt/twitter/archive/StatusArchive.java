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

public class StatusArchive extends IdDataModel {

    public static Factory<StatusArchive> INSTANCE = new Factory<StatusArchive>(StatusArchive.class,"twitter_archives/statuses");


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
        
        UserArchive user = UserArchive.INSTANCE.getOrNew(from);
        user.read(status.getUser());
        user.save();

        inReplyToStatusId = status.getInReplyToStatusId();

        inReplyToScreenName = status.getInReplyToScreenName();

        inReplyToUserId = status.getInReplyToUserId();

        quotedStatusId = status.getQuotedStatusId();

        if (quotedStatusId != -1 && !INSTANCE.exists(quotedStatusId)) {

            StatusArchive quotedStatus = INSTANCE.getOrNew(quotedStatusId);

            quotedStatus.read(status.getQuotedStatus());

            quotedStatus.save();

        }

        mediaUrls.clear();

        for (MediaEntity media : status.getMediaEntities()) {

            mediaUrls.add(media.getMediaURL());

        }

        isRetweet = status.isRetweet();

        if (isRetweet) {

            retweetedStatusId = status.getRetweetedStatus().getId();

            if (!INSTANCE.exists(retweetedStatusId)) {

                StatusArchive retweetedStatus = INSTANCE.getOrNew(retweetedStatusId);

                retweetedStatus.read(status.getRetweetedStatus());

                retweetedStatus.save();

            }

        } else {

            retweetedStatusId = -1L;

        }


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

        return "https://twitter.com/" + getUser().screenName + " /status/" + idStr;

    }

    public String getMarkdownURL() {

        return "[" + StrUtil.padAfter(text,5,"...") + "](" + getURL() + ")";

    }


    public String toMarkdown() {
        
        StringBuilder archive = new StringBuilder(getUser().getMarkdownURL());

        if (isRetweet) {

            StatusArchive retweetedStatus = INSTANCE.get(retweetedStatusId);

            archive.append(" 转推从 " + retweetedStatus.getUser().getMarkdownURL());

        } else if (inReplyToStatusId != -1) {

            if (UserArchive.INSTANCE.exists(inReplyToUserId)) {

                UserArchive replyToUser = UserArchive.INSTANCE.get(inReplyToUserId);

                archive.append(" 回复给 " + replyToUser.getMarkdownURL());

            } else {

                archive.append(" 回复给 [@" + inReplyToScreenName + "](https://twitter.com/" + inReplyToScreenName + ")");

            }

            archive.append(" 的 [推文](https://twitter.com/").append(inReplyToScreenName).append("/status/").append(inReplyToStatusId).append(")");

        } 


        Date date = new Date(createdAt);

        archive.append(" 在 ").append(NumberChineseFormater.format(date.getYear() - 2000,false)).append("年").append(NumberChineseFormater.format(date.getMonth(),false)).append("月").append(NumberChineseFormater.format(date.getDate(),false)).append("日");

        archive.append(" ").append(date.getHours() > 12 ? "下午" : "上午");
        archive.append(date.getHours()).append(":").append(date.getMinutes());

        if (quotedStatusId != null) {

            StatusArchive quotedStatus = INSTANCE.get(quotedStatusId);

            archive.append(" 对推文 : \n\n\n 「 -----------\n").append(quotedStatus.toMarkdown()).append("\n\n ----------」的回复 :\n\n");

        }

        archive.append(text);

        if (!mediaUrls.isEmpty()) {

            archive.append("\n\n媒体文件 : ");

            for (String url : mediaUrls) {

                archive.append("[媒体文件](").append(url).append(")");

            }

        }

        return archive.toString();

    }


}
