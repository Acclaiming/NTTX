package io.kurumi.ntt.fragment.twitter.tasks;

import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.utils.Html;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.Date;
import java.util.TimerTask;

public class MargedNoticeTask extends TimerTask {

    public static void start() {

        Date next = new Date();

        if (next.getHours() >= 8) {

            next.setDate(next.getDate() + 1);

        }

        next.setHours(8);
        next.setMinutes(0);
        next.setSeconds(0);

        BotFragment.mainTimer.schedule(new MargedNoticeTask(), next, 1 * 24 * 60 * 60 * 1000L);

    }

    @Override
    public void run() {

        new Thread("Marged Followers Notice Thread") {

            @Override
            public void run() {

                for (TAuth account : TAuth.data.getAllByField("fo_marge", true)) {

                    //if (account.fo == null || account.fo_marge ) continue;

                    doNotice(account);

                }

            }

        }.start();


    }

    public static void doNotice(TAuth account) {

        if (account.fo_new == null && account.fo_lost == null) return;

        String message = Html.b("新关注者 : ");

        Twitter api = account.createApi();

        if (account.fo_new == null) {

            message += "暂时没有";

        } else {

            message += "\n";

            for (Long id : account.fo_new) {

                try {

                    User follower = api.showUser(id);

                    UserArchive archive = UserArchive.save(follower);

                    Relationship ship = api.showFriendship(account.id, id);

                    message += "\n";

                    if (ship.isSourceFollowingTarget()) message += "[ 互相关注 ]";
                    else if (archive.isProtected) message += "[ 锁推 ]";

                    message += " " + archive.urlHtml() + " #" + archive.screenName;


                } catch (TwitterException e) {
                }

            }

        }

        message += "\n\n" + Html.b("失去关注者 : ");

        if (account.fo_lost == null) {

            message += "暂时没有";

        } else {

            message += "\n";

            for (Long id : account.fo_lost) {

                try {

                    User follower = api.showUser(id);
                    UserArchive archive = UserArchive.save(follower);

                    Relationship ship = api.showFriendship(account.id, id);

                    if (ship.isSourceFollowedByTarget()) {

                        continue;

                    }

                    message += "\n";

                    if (ship.isSourceFollowingTarget()) message += "[ 单向取关 ]";
                    else if (archive.isProtected) message += "[ 锁推 ]";

                    message += " " + archive.urlHtml() + " #" + archive.screenName;

                } catch (TwitterException e) {

                    UserArchive archive = UserArchive.get(id);

                    if (archive == null) continue;

                    message += "\n[ " + NTT.parseTwitterException(e) + " ] " + archive.urlHtml() + " #" + archive.screenName;

                }

            }

        }

        if (account.multiUser()) message += "\n\n账号 : " + account.archive().bName();

        account.fo_new = null;
        account.fo_lost = null;

        TAuth.data.setById(account.id, account);

        new Send(account.user, message).html().async();

    }

}
