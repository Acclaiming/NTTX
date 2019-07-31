package io.kurumi.ntt.fragment.debug;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Status;
import twitter4j.TwitterException;

public class DebugStatus extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("get_status");

    }


    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (params.length == 0) {

            msg.send("invalid status id").exec();

            return;

        }

        requestTwitter(user, msg);

    }

    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        try {

            Status status = account.createApi().showStatus(NTT.parseStatusId(params[0]));

            msg.send(status.toString()).exec();

        } catch (TwitterException e) {

            msg.send(NTT.parseTwitterException(e)).exec();

        }

    }

}
