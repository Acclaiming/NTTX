package io.kurumi.ntt.fragment.admin;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.TwitterException;

public class StatusDel extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("del_status");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (params.length == 0) {

            msg.invalidParams("statusId").async();

            return;

        }

        Long statusId = NTT.parseStatusId(params[0]);

        if (StatusArchive.contains(statusId)) {

            long userId = StatusArchive.get(statusId).user().id;

            TAuth auth = TAuth.getById(userId);

            if (auth == null) {

                msg.send("不在范围内").async();

                return;

            }

            try {

                auth.createApi().destroyStatus(statusId);

                msg.send("完成").async();

            } catch (TwitterException e) {

                msg.send(NTT.parseTwitterException(e)).async();

            }

            return;

        }

        String sn = NTT.parseScreenName(params[0]);

        if (UserArchive.contains(sn)) {

            long userId = UserArchive.get(sn).id;

            TAuth auth = TAuth.getById(userId);

            if (auth == null) {

                msg.send("不在范围内").async();

                return;

            }

            try {

                auth.createApi().destroyStatus(statusId);

                msg.send("完成").async();

            } catch (TwitterException e) {

                msg.send(NTT.parseTwitterException(e)).async();

            }

            return;


        }

    }

}
