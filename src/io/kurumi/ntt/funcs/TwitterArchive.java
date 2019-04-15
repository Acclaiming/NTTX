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
import io.kurumi.ntt.db.BotDB;

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

        Twitter api = TAuth.get(user.id).createApi();

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

        if (BotDB.statusExists(statusId)) {

            msg.send("存档存在 :)").exec();

            msg.send(BotDB.getStatus(statusId).toHtml()).html().exec();

            return;

        } else if (!TAuth.exists(user.id)) {

            msg.send("存档不存在 :( 乃没有认证账号 无法通过API读取推文... 请使用 /login 认证 ( ⚆ _ ⚆ )").publicFailed();

            return;

        }

        msg.send("正在拉取 :)").exec();

        Twitter api = TAuth.get(user.id).createApi();

        try {

            StatusArchive newStatus = BotDB.saveStatus(api.showStatus(statusId));

            newStatus.loop(api);

            msg.send("已存档 :)").exec();
            
            msg.send(newStatus.toHtml()).html().exec();

        } catch (TwitterException e) {

            msg.send("存档失败 :(","推文还在吗？是锁推推文吗？").exec();

        }



    }

    

}
