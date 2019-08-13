package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class StatusGetter extends Fragment {

    public static String PAYLOAD_SHOW_STATUS = "status";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("status");

        registerPayload("status");

    }

    @Override
    public void onPayload(UserData user, Msg msg, String payload, String[] params) {

        requestTwitterPayload(user, msg);

    }

    @Override
    public void onTwitterPayload(UserData user, Msg msg, String payload, String[] params, TAuth account) {

        Long statusId = NumberUtil.parseLong(params[0]);

        if (account == null) {

            StatusArchive archive = StatusArchive.get(statusId);

            if (archive == null) {

                msg.send("找不到存档...").exec();

            } else {

                msg.send(archive.toHtml(account)).html().exec();

            }

        } else {

            Twitter api = account.createApi();

            try {

                Status newStatus = api.showStatus(statusId);

                StatusArchive archive = StatusArchive.save(newStatus);

                archive.loop(api);

                archive.sendTo(msg.chatId(), -1, account, msg.isPrivate() ? newStatus : null);

            } catch (TwitterException e) {

                if (StatusArchive.contains(statusId)) {

                    StatusArchive.get(statusId).sendTo(msg.chatId(), -1, null, null);

                } else {

                    msg.send(NTT.parseTwitterException(e)).publicFailed();

                }

            }

        }

    }


    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (params.length != 1) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        if (NTT.parseStatusId(params[0]) == -1L) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        requestTwitter(user, msg);

    }

    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        Twitter api = account.createApi();

        msg.sendTyping();

        Long statusId = NTT.parseStatusId(params[0]);

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

                msg.send(StatusArchive.get(statusId).toHtml(account)).html().point(1, statusId);

            } else {

                msg.send(NTT.parseTwitterException(e)).publicFailed();

                return;

            }


        }


    }

}
