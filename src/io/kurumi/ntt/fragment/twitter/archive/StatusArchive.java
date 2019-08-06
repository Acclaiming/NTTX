package io.kurumi.ntt.fragment.twitter.archive;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.status.MessagePoint;
import io.kurumi.ntt.fragment.twitter.status.StatusAction;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;
import io.netty.util.AsciiString;

public class StatusArchive {

    public static Data<StatusArchive> data = new Data<StatusArchive>(StatusArchive.class);
    public static transient String split_tiny = "---------------";
    public static transient String split = "\n\n" + split_tiny + split_tiny + "\n\n";
    public Long id;
    public Long createdAt;
    public String text;
    public Long from;
    public Long inReplyToStatusId;
    public String inReplyToScreenName;
    public Long inReplyToUserId;
    public Long quotedStatusId;
    public String quotedScreenName;
    public Long quotedUserId;
    public LinkedList<String> mediaUrls;
    public Boolean isRetweet;
    public Long retweetedStatus;
    public LinkedList<Long> userMentions;

    public static StatusArchive get(Long id) {
        return data.getById(id);
    }

    public static boolean contains(Long id) {
        return data.containsId(id);
    }

    public static StatusArchive save(Status status) {


        StatusArchive archive = data.getById(status.getId());

        if (archive == null) {

            archive = new StatusArchive();

            archive.id = status.getId();

        }

        archive.read(status);

        data.setById(archive.id,archive);

        return archive;

    }

    public void sendTo(long chatId,int depth,TAuth auth,Status status) {

        LinkedList<File> photo = new LinkedList<>();

        for (String url : mediaUrls) {

            String suffix = StrUtil.subAfter(url,".com/",false);

            File cache = new File(Env.CACHE_DIR,suffix);

            if (!cache.isFile()) {

                try {

                    HttpUtil.downloadFile(url,cache);

                } catch (Exception ex) {
                }


            }

            if (cache.isFile()) {

                photo.add(cache);

            }

        }

        String html = toHtml(depth);

        if (html.length() < 1024 && photo.size() == 1) {

            SendPhoto send = new SendPhoto(chatId,photo.get(0)).caption(html).parseMode(ParseMode.HTML);

            if (status != null) {

                send.replyMarkup(StatusAction.createMarkup(id,from.equals(auth.id),depth() <= depth,status.isRetweetedByMe(),status.isFavorited()).markup());

            }

            SendResponse resp = Launcher.INSTANCE.bot().execute(send);

            if (resp != null && resp.isOk() && resp.message().chat().type() == Chat.Type.Private) {

                MessagePoint.set(resp.message().messageId(),1,id);

            } else if (resp != null && !resp.isOk()) {

                Send sendN = new Send(chatId,toHtml(depth)).html();

                if (status != null) {

                    sendN.buttons(StatusAction.createMarkup(id,from.equals(auth.id),depth == -1 || depth() <= depth,status.isRetweetedByMe(),status.isFavorited()));

                }

                sendN.point(1,id);

            }

        } else {

            Send send = new Send(chatId,toHtml(depth)).html();

            if (status != null) {

                send.buttons(StatusAction.createMarkup(id,from.equals(auth.id),depth == -1 || depth() <= depth,status.isRetweetedByMe(),status.isFavorited()));

            }

            SendResponse msg = send.point(1,id);

            if (photo.size() > 0 && msg.isOk()) {

                if (photo.size() == 1) {

                    Launcher.INSTANCE.bot().execute(new SendPhoto(chatId,photo.get(0)).replyToMessageId(msg.message().messageId()));

                } else {

                    InputMediaPhoto[] input = new InputMediaPhoto[photo.size()];

                    for (int index = 0; index < photo.size(); index++) {

                        input[index] = new InputMediaPhoto(photo.get(index));

                    }

                    Launcher.INSTANCE.bot().execute(new SendMediaGroup(chatId,input).replyToMessageId(msg.message().messageId()));

                }

            }

        }

    }

    public void read(Status status) {

        createdAt = status.getCreatedAt().getTime();

        text = status.getText();

        userMentions = new LinkedList<>();

        from = UserArchive.save(status.getUser()).id;

        inReplyToStatusId = status.getInReplyToStatusId();

        inReplyToScreenName = status.getInReplyToScreenName();

        inReplyToUserId = status.getInReplyToUserId();

        quotedStatusId = status.getQuotedStatusId();

        if (inReplyToStatusId != -1) {

            for (UserMentionEntity mention : status.getUserMentionEntities()) {

                if (text.startsWith("@" + mention.getScreenName() + " ")) {

                    userMentions.add(mention.getId());

                    text = StrUtil.subAfter(text,"@" + mention.getScreenName() + " ",false);

                }

            }

        }

        for (URLEntity url : status.getURLEntities()) {

            if (text.endsWith(url.getURL()) && quotedStatusId != -1) {

                // 引用推文

                text = StrUtil.subBefore(text,url.getURL(),true);

                quotedScreenName = NTT.parseScreenName(url.getExpandedURL());

            } else {

                text = text.replace(url.getURL(),url.getExpandedURL());

            }

        }

        if (status.getQuotedStatus() != null) {

            quotedUserId = status.getQuotedStatus().getUser().getId();

        } else if (quotedScreenName != null && UserArchive.contains(quotedScreenName)) {

            quotedUserId = UserArchive.get(quotedScreenName).id;

        } else {

            quotedUserId = -1L;

        }

        mediaUrls = new LinkedList<>();

        for (MediaEntity media : status.getMediaEntities()) {

            mediaUrls.add(media.getMediaURL());

        }

        isRetweet = status.isRetweet();

        if (isRetweet) {

            retweetedStatus = status.getRetweetedStatus().getId();

            if (!StatusArchive.contains(retweetedStatus)) {

                StatusArchive.save(status.getRetweetedStatus());

            }

        } else {

            retweetedStatus = -1L;

        }

    }

    public UserArchive user() {

        UserArchive user = UserArchive.get(from);

        return user;

    }

    public String url() {

        return "https://twitter.com/" + user().screenName + "/status/" + id;

    }

    public String htmlURL() {

        return Html.a(StrUtil.padAfter(text,5,"..."),url());

    }

    public String toHtml() {

        return toHtml(-1);

    }

    public int depth() {

        int depth = 0;

        if (inReplyToStatusId != -1) {

            depth++;

            StatusArchive inReplyTo = StatusArchive.get(inReplyToStatusId);

            if (inReplyTo != null) {

                depth += inReplyTo.depth();

            }

        }

        return depth;

    }

    public String toHtml(int depth) {

        return toHtml(depth,false,true);

    }

    public String toHtml(int depth,boolean quoted,boolean current) {

        StringBuilder archive = new StringBuilder();

        if (!quoted && inReplyToStatusId != -1) {

            if (depth != 0) {

                StatusArchive inReplyTo = StatusArchive.get(inReplyToStatusId);

                if (inReplyTo != null) {

                    archive.append(inReplyTo.toHtml(depth > 0 ? depth - 1 : depth,false,false));

                } else {

                    archive.append(notAvilableStatus(inReplyToUserId,inReplyToScreenName));

                }

                archive.append(split);

            }

            archive.append(user().urlHtml()).append(" 的 ").append(Html.a("回复",current ? url() : "https://t.me/" + Launcher.INSTANCE.me.username() + "?start=status_" + id));


        } else if (!quoted && isRetweet) {

            StatusArchive retweeted = StatusArchive.get(retweetedStatus);

            archive.append(user().urlHtml()).append(" 转推从 " + retweeted.user().urlHtml()).append(" 的 ").append(Html.a("推文",current ? url() : "https://t.me/" + Launcher.INSTANCE.me.username() + "?start=status_" + id)).append(" : ");

            archive.append(split);

            archive.append(retweeted.toHtml(depth > 0 ? depth - 1 : depth,false,false));

            return archive.toString();

        } else {

            archive.append(user().urlHtml()).append(" 的 ").append(Html.a("推文",current ? url() : "https://t.me/" + Launcher.INSTANCE.me.username() + "?start=status_" + id));

        }

        String content = text;

        if (!mediaUrls.isEmpty()) {

            content = StrUtil.subBefore(content,"https://t.co",true);

        }

        archive.append(" :");

        content = HtmlUtil.escape(content);

        archive.append("\n");
        if (!content.trim().isEmpty()) {

            archive.append("\n").append(content).append("\n");

        }

        if (!mediaUrls.isEmpty()) {

            archive.append("\n");

            for (String url : mediaUrls) {

                archive.append(Html.a("媒体文件",url)).append(" ");

            }

            archive.append("\n");

        }

        if (quotedStatusId != -1 && !quoted) {

            StatusArchive quotedStatus = StatusArchive.get(quotedStatusId);

            archive.append("\n").append(split_tiny).append("\n");

            if (quotedStatus != null) {

                archive.append("引用 " + quotedStatus.toHtml(1,true,false));

            } else {

                if (quotedUserId != -1) {

                    archive.append("引用的").append(UserArchive.get(quotedUserId).urlHtml()).append("的推文不可用");

                }

            }

            archive.append(split_tiny).append("\n");

        }

        if (!quoted) {

            archive.append("\n在 ");

            Calendar date = Calendar.getInstance();

            date.setTimeInMillis(createdAt);

            archive.append(date.get(Calendar.YEAR) - 2000).append("年").append(date.get(Calendar.MONTH) + 1).append("月").append(date.get(Calendar.DAY_OF_MONTH)).append("日");

            archive.append(", ").append(date.get(Calendar.AM_PM) == 0 ? "上午" : "下午").append(" ").append(date.get(Calendar.HOUR)).append(":").append(date.get(Calendar.MINUTE));

        }

        return archive.toString().replaceAll("[:cntrl:]","");

    }

    String notAvilableStatus(Long id,String screenName) {

        if (UserArchive.contains(id)) {

            return UserArchive.get(id).urlHtml() + " 的 不可用的推文";

        } else {

            return Html.a("@" + screenName,"https://twitter.com/" + screenName) + " 的 不可用的推文";

        }

    }

    public StatusArchive loop(Twitter api) {

        return loop(api,false);

    }

    public StatusArchive loop(Twitter api,boolean avoid) {

        for (long mention : userMentions) {

            if (!UserArchive.contains(mention)) {

                try {

                    UserArchive.save(api.showUser(mention));

                } catch (TwitterException e) {
                }

            }

        }

        if (quotedUserId == -1 && quotedScreenName != null) {

            if (UserArchive.contains(quotedScreenName)) {

                quotedUserId = UserArchive.get(quotedScreenName).id;

            } else {

                try {

                    quotedUserId = UserArchive.save(api.showUser(quotedScreenName)).id;

                } catch (TwitterException e) {
                }

            }

            if (quotedUserId != -1) {

                data.setById(id,this);

            }

        }

        if (inReplyToStatusId != -1) {

            try {

                if (StatusArchive.contains(inReplyToStatusId)) {

                    StatusArchive.get(inReplyToStatusId).loop(api);

                } else {

                    Status status = api.showStatus(inReplyToStatusId);

                    StatusArchive inReplyTo = StatusArchive.save(status);

                    inReplyTo.loop(api);

                }

            } catch (TwitterException ex) {

                TAuth accessable = NTT.loopFindAccessable(inReplyToUserId);

                if (accessable != null) {

                    try {

                        Status status = accessable.createApi().showStatus(inReplyToStatusId);

                        StatusArchive inReplyTo = StatusArchive.save(status);

                        inReplyTo.loop(api);

                    } catch (TwitterException e) {
                    }

                }
            }

        }


        if (quotedStatusId != -1) {

            try {

                if (StatusArchive.contains(quotedStatusId)) {

                    StatusArchive.get(quotedStatusId).loop(api);

                } else {

                    Status status = api.showStatus(quotedStatusId);

                    StatusArchive quoted = StatusArchive.save(status);

                    quoted.loop(api);

                }


            } catch (TwitterException ex) {

                if (quotedUserId != -1) {

                    TAuth accessable = NTT.loopFindAccessable(quotedUserId);

                    if (accessable != null) {

                        try {

                            Status status = accessable.createApi().showStatus(quotedStatusId);

                            StatusArchive quoted = StatusArchive.save(status);

                            quoted.loop(api);

                        } catch (TwitterException e) {
                        }


                    }

                }

            }

        }

        return this;

    }


}
