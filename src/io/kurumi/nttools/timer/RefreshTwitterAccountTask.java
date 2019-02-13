package io.kurumi.nttools.timer;

import io.kurumi.nttools.fragments.MainFragment;
import io.kurumi.nttools.utils.UserData;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.model.request.Send;
import java.util.Iterator;

public class RefreshTwitterAccountTask implements TimerTask {

    public static final RefreshTwitterAccountTask INSTANCE = new RefreshTwitterAccountTask();

    @Override
    public void run(MainFragment fragment) {

        long lastTime = fragment.data.getLong("last_twitter_accounts_refresh_time", -1L);

        if (System.currentTimeMillis() - lastTime > 1 * 60 * 1000) {

            for (UserData user : fragment.getUsers()) {

                Iterator<TwiAccount> i = user.twitterAccounts.iterator();

                while (i.hasNext()) {

                    TwiAccount account = i.next();

                    if (!account.refresh()) {

                        i.remove();

                        user.save();

                        new Send(fragment, user.id, "您Twitter账号", "", account.getFormatedNameMarkdown(), "", "的认证已经失效 (。•́︿•̀。)").markdown().disableLinkPreview().exec();

                    }  

                }

                user.save();

            }

            fragment.data.put("last_twitter_accounts_refresh_time", System.currentTimeMillis());

        }

    }

}
