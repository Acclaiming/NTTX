package io.kurumi.ntt.fragment.admin;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.utils.NTT;
import java.util.LinkedList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import io.kurumi.ntt.fragment.BotFragment;

public class TASReply extends Fragment {

	@Override
	public void init(BotFragment origin) {
		
		super.init(origin);
		
		registerAdminFunction("tas");
		
	}
	

	@Override
	public void onFunction(UserData user,Msg msg,String function,String[] params) {
		
        if (params.length == 0) {

            msg.send("/tas <推文ID/链接>").exec();

            return;

        }
		
		requestTwitter(user,msg);
		
	}

	@Override
	public int checkTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
		return PROCESS_THREAD;
		
	}

	@Override
	public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {
		
        Twitter api = account.createApi();

        Status status;
        LinkedList<Status> replies = new LinkedList<>();

        int count = 0;

        try {

            status = api.showStatus(NTT.parseStatusId(params[0]));
			// replies = TApi.getReplies(api, status);

        } catch (TwitterException e) {

			try {

				status = NTT.loopFindAccessable(NTT.parseScreenName(params[0])).createApi().showStatus(NTT.parseStatusId(params[0]));

			} catch (TwitterException ex) {

				msg.send(NTT.parseTwitterException(e)).exec();

			}

            return;

        }

        for (TAuth auth : TAuth.data.collection.find()) {

            if (auth.id.equals(account.id)) continue;

            try {

                LinkedList<Status> ann = TApi.getReplies(auth.createApi(),status);

                ann.removeAll(replies);

                for (Status hide : ann) {

                    count ++;

                    StatusArchive.save(hide).sendTo(msg.chatId(),0,account,null);

					replies.add(hide);

                }

                replies.addAll(ann);


            } catch (TwitterException e) {


            }

        }

        msg.send("完成 发现 " + count + "条 回复").exec();

    }

}
