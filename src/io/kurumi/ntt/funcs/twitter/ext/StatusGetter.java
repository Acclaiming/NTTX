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
import io.kurumi.ntt.fragment.twitter.status.StatusAction;

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

		if (StatusArchive.contains(statusId) && !msg.isPrivate()) {
			
StatusArchive.get(statusId).sendTo(msg.chatId(),-1,null,null);
		
			return;
			
		}
		
		try {
		
			Status newStatus = api.showStatus(statusId);

			StatusArchive archive = StatusArchive.save(newStatus).loop(api);
			
            archive.sendTo(msg.chatId(),-1,account,msg.isPrivate() ? newStatus : null);
			
			return;

		} catch (TwitterException ex) {
			
			TAuth auth = NTT.loopFindAccessable(NTT.parseScreenName(params[0]));

			if (auth != null) {
				
				api = auth.createApi();
				
			}
			
		} 

		if (StatusArchive.contains(statusId)) {
			
			StatusArchive.get(statusId).sendTo(msg.chatId(),-1,null,null);
			
		}
		
        try {

            Status newStatus = api.showStatus(statusId);

			StatusArchive archive = StatusArchive.save(newStatus);

            archive.loop(api);
			
            archive.sendTo(msg.chatId(),-1,account,null);

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
