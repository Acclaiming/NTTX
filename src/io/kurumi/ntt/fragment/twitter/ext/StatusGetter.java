package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.abs.*;
import io.kurumi.ntt.fragment.twitter.*;
import io.kurumi.ntt.fragment.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

public class StatusGetter extends TwitterFunction {

    public static StatusGetter INSTANCE = new StatusGetter();
	
	String PAYLOAD_SHOW_STATUS = "status";

    @Override
    public void functions(LinkedList<String> names) {

        names.add("status");

    }

	@Override
	public boolean onMsg(UserData user, Msg msg) {
	
		if (super.onMsg(user, msg)) return true;
		
		if (!msg.isStartPayload() || !PAYLOAD_SHOW_STATUS.equals(msg.payload()[0])) return false;

        TAuth auth = TAuth.getByUser(user.id).first();

        Long statusId = NumberUtil.parseLong(msg.payload()[1]);

        if (auth == null) {

            StatusArchive archive = StatusArchive.get(statusId);

            if (archive == null) {

                msg.send("找不到存档...").exec();

            } else {

                msg.send(archive.toHtml()).html().exec();

            }

        } else {

            Twitter api = auth.createApi();

            msg.sendTyping();

            try {

                Status newStatus = api.showStatus(statusId);

                StatusArchive archive = StatusArchive.save(api.showStatus(statusId));

                archive.loop(api);

                archive.sendTo(msg.chatId(), -1, auth, msg.isPrivate() ? newStatus : null);

            } catch (TwitterException e) {

                if (StatusArchive.contains(statusId)) {

                    StatusArchive.get(statusId).sendTo(msg.chatId(), -1, null, null);

                } else {

                    msg.send(NTT.parseTwitterException(e)).publicFailed();

                }


            }

			
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
