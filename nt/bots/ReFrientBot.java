package io.kurumi.nt.bots;

import twitter4j.*;
import io.kurumi.nt.*;
import java.util.concurrent.atomic.*;

public class ReFrientBot extends SwitchBot implements Runnable {

    @Override
    public String getBotName() {
        return "ReFriendBot";
    }

    @Override
    public String getConfigKey() {
        return "bot_re_friend";
    }

    private Thread th;
    private Twitter api;

    @Override
    public void onInit(TwitterStream stream, TwiAccount acc) {

        api = acc.createApi();

        th = new Thread(this);
        th.setName(getBotName() + "Thread");
        th.start();
    }
    
    private AtomicBoolean stopped = new AtomicBoolean(false);

    @Override
    public void run() {

        try {

            do {

                try {

                    IDs fo = api.getFollowersIDs(-1);

                    for (long id : fo.getIDs())  {

                        if (stopped.get()) return;
                        
                        User u = api.showUser(id);

                        if (u.isFollowRequestSent()) break;

                        Relationship fs = api.showFriendship(api.getId(), u.getId());

                        if (fs.isSourceFollowingTarget()) break;

                        api.createFriendship(u.getId());

                        System.out.println("「" + getBotName() + "」 已回关 " + NTApi.formatUsernName(u));

                    }

                } catch (TwitterException e) {}


                Thread.sleep(10 * 60 * 1000);

            } while(true);



        } catch (InterruptedException e) {}



    }

    @Override
    public void onTerminated() {

        stopped.set(true);
        th.interrupt();

    }

}
