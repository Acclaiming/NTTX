package io.kurumi.ntt.funcs.twitter.ext;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.funcs.twitter.track.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;
import io.kurumi.ntt.funcs.twitter.track.TrackTask.*;

public class StatusGetter extends TwitterFunction {

    public static StatusGetter INSTANCE = new StatusGetter();

    @Override
    public void functions(LinkedList<String> names) {

        names.add("status");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

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

		try {

			StatusArchive newStatus = StatusArchive.save(api.showStatus(statusId));

            newStatus.loop(api);

            msg.send(newStatus.toHtml()).html().point(1,statusId);
			
			return;

		} catch (TwitterException ex) {} 

        try {

            StatusArchive newStatus = StatusArchive.save(api.showStatus(statusId));

            newStatus.loop(api);

            msg.send(newStatus.toHtml()).html().point(1,statusId);

        } catch (TwitterException e) {

            if (StatusArchive.contains(statusId)) {

                msg.send(StatusArchive.get(statusId).toHtml()).html().point(1,statusId);

            } else {

                msg.send(NTT.parseTwitterException(e)).publicFailed();

                return;

            }


        }



    }

}
