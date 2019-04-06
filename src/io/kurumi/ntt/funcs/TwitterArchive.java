package io.kurumi.ntt.funcs;

import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.twitter.archive.StatusArchive;
import io.kurumi.ntt.utils.T;
import java.util.LinkedList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.twitter.archive.UserArchive;

public class TwitterArchive extends Fragment {

    public static TwitterArchive INSTANCE = new TwitterArchive();

    @Override
    public boolean onMsg(UserData user,Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.command()) {

            case "status" : statusArchive(user,msg);break;
            case "pull" : pullUser(user,msg);break;
                //   case "tuser" : userArchive(user,msg);break;

            default : return false;

        }

        return true;

    }

    void pullUser(UserData user,Msg msg) {

        Twitter api = TAuth.get(user).createApi();

    }

    void statusArchive(UserData user,Msg msg) {

        if (msg.params().length != 1) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        Long statusId = T.parseStatusId(msg.params()[0]);

        if (statusId == -1L) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        if (StatusArchive.INSTANCE.exists(statusId)) {

            msg.send("存档存在 :)").exec();

            msg.send(StatusArchive.INSTANCE.get(statusId).toHtml()).html().exec();

            return;

        } else if (!TAuth.exists(user)) {

            msg.send("存档不存在 :( 乃没有认证账号 无法通过API读取推文... 请使用 /tauth 认证 ( ⚆ _ ⚆ )").publicFailed();

            return;

        }

        msg.send("正在拉取 :)").exec();

        Twitter api = TAuth.get(user).createApi();

        try {

            Status status = api.showStatus(statusId);

            StatusArchive newStatus = StatusArchive.INSTANCE.getOrNew(statusId);

            newStatus.read(status);

            StatusArchive.INSTANCE.saveObj(newStatus);

            loopStatus(newStatus,api);

            msg.send("已存档 :)").exec();

            msg.send(newStatus.toHtml()).html().exec();

        } catch (TwitterException e) {

            msg.send("存档失败 :(","推文还在吗？是锁推推文吗？").exec();

        }



    }

    void loopStatus(StatusArchive archive,Twitter api) {

        String content = archive.text;

        if (content.startsWith("@")) {

            while (content.startsWith("@")) {
                
                String screenName = StrUtil.subBefore(content.substring(1)," ",false);

                content = StrUtil.subAfter(content, " ",false);
                
                if (UserArchive.findByScreenName(screenName) == null) {

                    try {

                        UserArchive.saveCache(api.showUser(screenName));

                    } catch (TwitterException ex) {} 

                }

            }

        }


        try {

            if (archive.inReplyToStatusId != -1) {

                if (StatusArchive.INSTANCE.exists(archive.inReplyToStatusId)) {

                    loopStatus(StatusArchive.INSTANCE.get(archive.inReplyToStatusId),api);

                } else {

                    Status status = api.showStatus(archive.inReplyToStatusId);

                    StatusArchive inReplyTo = StatusArchive.INSTANCE.getOrNew(archive.inReplyToStatusId);

                    inReplyTo.read(status);

                    StatusArchive.INSTANCE.saveObj(inReplyTo);

                    loopStatus(inReplyTo,api);

                }

            }

            if (archive.quotedStatusId != -1) {

                if (StatusArchive.INSTANCE.exists(archive.quotedStatusId)) {

                    loopStatus(StatusArchive.INSTANCE.get(archive.quotedStatusId),api);

                } else {

                    Status status = api.showStatus(archive.quotedStatusId);

                    StatusArchive qupted = StatusArchive.INSTANCE.getOrNew(archive.quotedStatusId);

                    qupted.read(status);

                    StatusArchive.INSTANCE.saveObj(qupted);

                    loopStatus(qupted,api);

                }

            }

        } catch (TwitterException ex) {}


    }

}
