package io.kurumi.nttools.twitter;

import twitter4j.*;

import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.twitter.TApi;
import io.kurumi.nttools.utils.UserData;
import java.util.LinkedList;

public class CleanThread extends Thread {

    private UserData userData;
    private Callback callback;
    private boolean status;
    private boolean followers;
    private boolean friends;

    public CleanThread(UserData userData, Callback callback, boolean status, boolean followers, boolean friends) {
        this.userData = userData;
        this.callback = callback;
        this.status = status;
        this.followers = followers;
        this.friends = friends;
    }


    @Override
    public void run()  {

        Twitter api =  callback.data.getUser(userData).createApi();

        try {

            if (status) {

                ResponseList<Status> tl = api.getUserTimeline(new Paging().count(200));

                while (tl.size() != 0) {

                    for(Status s : tl) {

                        api.destroyStatus(s.getId());

                        callback.send(s.getText()).exec();

                    }

                    tl = api.getUserTimeline(new Paging().count(200));

                }

            }

            if (followers) {

                LinkedList<String> cache = new LinkedList<>();

                long[] fo = TApi.getAllFo(api);

                for(long id : fo) {

                    api.createBlock(id);
                    api.destroyBlock(id);

                    if (cache.size() < 20) {

                        cache.add(TApi.formatUserNameMarkdown(api.showUser(id)));

                    } else {

                        callback.send(cache.toArray(new String[cache.size()])).html().exec();

                    }

                }

                if (cache.size() > 0) {

                    callback.send(cache.toArray(new String[cache.size()])).html().exec();

                }


            }


            if (friends) {

                LinkedList<String> cache = new LinkedList<>();

                long[] fo = TApi.getAllFr(api);

                for(long id : fo) {

                    api.destroyFriendship(id);

                    if (cache.size() < 20) {

                        cache.add(TApi.formatUserNameMarkdown(api.showUser(id)));

                    } else {

                        callback.send(cache.toArray(new String[cache.size()])).html().exec();

                    }

                }

                if (cache.size() > 0) {

                    callback.send(cache.toArray(new String[cache.size()])).html().exec();

                }


            }

        } catch (TwitterException e) {}

    }

}

