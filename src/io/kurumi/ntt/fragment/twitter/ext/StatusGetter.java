package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.utils.NTT;

import java.util.LinkedList;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class StatusGetter extends TwitterFunction {

    public static StatusGetter INSTANCE = new StatusGetter();

    @Override
    public void functions(LinkedList<String> names) {

        names.add("status");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        if (params.length != 1) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        Long statusId = NTT.parseStatusId(params[0]);

        if (statusId == -1L) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        Twitter api = account.createApi();

        msg.sendTyping();

        if (StatusArchive.contains(statusId) && !msg.isPrivate()) {

            StatusArchive.get(statusId).sendTo(msg.chatId(), -1, null, null);

            return;

        }

        try {

            Status newStatus = api.showStatus(statusId);

            StatusArchive archive = StatusArchive.save(newStatus).loop(api);

            archive.sendTo(msg.chatId(), -1, account, msg.isPrivate() ? newStatus : null);

            return;

        } catch (TwitterException ex) {

            TAuth auth = NTT.loopFindAccessable(NTT.parseScreenName(params[0]));

            if (auth != null) {

                api = auth.createApi();

            }

        }

        if (StatusArchive.contains(statusId)) {

            StatusArchive.get(statusId).sendTo(msg.chatId(), -1, null, null);

        }

        try {

            Status newStatus = api.showStatus(statusId);

            StatusArchive archive = StatusArchive.save(newStatus);

            archive.loop(api);

            archive.sendTo(msg.chatId(), -1, account, null);

        } catch (TwitterException e) {

            if (StatusArchive.contains(statusId)) {

                msg.send(StatusArchive.get(statusId).toHtml()).html().point(1, statusId);

            } else {

                msg.send(NTT.parseTwitterException(e)).publicFailed();

                return;

            }


        }


    }

}
