package io.kurumi.ntt.bots.template;

import io.kurumi.ntt.bots.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.ui.*;
import io.kurumi.ntt.ui.confs.*;
import io.kurumi.ntt.ui.request.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import twitter4j.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.*;

public class TwitterReFoBot extends UserBot {

    public TwitterReFoBot(UserData owner, String name) {
        super(owner, name);
    }

    public static final String TYPE = "TwitterReFo";

    @Override
    public String type() {
        return TYPE;
    }

    public TwitterAcconutsConf twitterAccounts = new TwitterAcconutsConf(this, "开启的账号", "accounts");
    public IntConf sleepTime = new IntConf(this, "回Fo间隔 (分钟)", "sleep_time");


    @Override
    public void confs(ConfRoot confs) {

        confs.add(twitterAccounts);
        confs.add(sleepTime);

    }

    @Override
    public AbsResuest start(DataObject obj) {

        List<TwiAccount> accounts = twitterAccounts.get();

        if (accounts.isEmpty()) {

            return obj.reply().alert("没有启用的账号 >_<");

        } else if (sleepTime.get() == null) {

            return obj.reply().alert("未设置回Fo间隔 >_<");

        }

        startAtBackground();

        return obj.reply().text("已开启...");
    }


    private ReFriendThread thread;
    private AtomicBoolean stopped = new AtomicBoolean(false);

    @Override
    public void startAtBackground() {

        stopped.set(false);

        enable = true;

        thread = new ReFriendThread();

        thread.run();

    }

    @Override
    public void interrupt() {

        super.interrupt();

        thread.interrupt();

        stopped.set(true);

        thread = null;

    }


    public class ReFriendThread extends Thread {

        @Override
        public void run() {

            LinkedList<Twitter> apis = new LinkedList<>();

            for (TwiAccount account : twitterAccounts.get()) {

                apis.add(account.createApi());

            }

            try {

                do {

                    for (Twitter api : apis) {

                        try {

                            reFriend(api);

                        } catch (TwitterException e) {}

                    }

                    Thread.sleep(sleepTime.get() * 60 * 1000);
                    
                } while (true);

            } catch (InterruptedException e) {}

        }

        public void reFriend(Twitter api) throws TwitterException {

            IDs followers = api.getFollowersIDs(-1);

            for (long id : followers.getIDs()) {

                Relationship ship =  api.showFriendship(api.getId(), id);

                if (!ship.isSourceFollowingTarget()) {

                    User u = api.showUser(id);

                    api.createFriendship(id);

                    if (u.isProtected()) {

                        new SendMsg(owner.chatId, "「TwitterReFo」 : 正在申请关注 " + TApi.formatUserName(u)).exec();

                    } else {

                        new SendMsg(owner.chatId, "「TwitterReFo」 : 已关注 " + TApi.formatUserName(u)).exec();


                    }

                } else break;

            }



        }

    }

}
