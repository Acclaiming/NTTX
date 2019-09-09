package io.kurumi.ntt.fragment.twitter.ext;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.fragment.twitter.tasks.TrackTask;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.*;

public class FriendsList extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("friends_list");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        if (params.length < 2) {

            msg.invalidParams("目标用户", "名称").async();

            return;

        }

        requestTwitter(user, msg, true);

    }

    @Override
    public int checkTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        return PROCESS_ASYNC;

    }

    @Override
    public void onTwitterFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        Twitter api = account.createApi();

        UserArchive target;

        try {

            target = NTT.findUser(api, params[0]);

        } catch (TwitterException e) {

            msg.send(NTT.parseTwitterException(e)).async();

            return;

        }

        msg.send("读取推文").async();

        LinkedList<Status> timeline;

        try {

            timeline = new LinkedList<Status>(api.getUserTimeline(target.id, new Paging().count(200)));

        } catch (TwitterException e) {

            NTT.Accessable accessable = NTT.loopFindAccessable(target.id);

            if (accessable == null) {

                msg.send(NTT.parseTwitterException(e)).async();

                return;

            }

            api = accessable.auth.createApi();

            if (accessable.timeline != null) {

                timeline = new LinkedList<Status>(accessable.timeline);

            } else {

                try {

                    timeline = new LinkedList<Status>(api.getUserTimeline(target.id, new Paging().count(200)));

                } catch (TwitterException ex) {

                    msg.send(NTT.parseTwitterException(ex)).async();

                    return;

                }

            }

        }

        // 读取可用推文

        List<Status> tl = timeline;

        while (!tl.isEmpty()) {

            Long maxId = tl.get(tl.size() - 1).getId() - 1;

            try {

                tl = api.getUserTimeline(target.id, new Paging().count(200).maxId(maxId));

                timeline.addAll(tl);

            } catch (TwitterException e) {

                msg.send(NTT.parseTwitterException(e)).async();

                return;

            }

        }

        msg.send("解析回复").async();

        // 解析回复

        HashMap<Long, InReplyTo> replyCount = new HashMap<>();

        for (Status status : timeline) {

            if (status.isRetweet()) {
            }

            long replyTo = status.isRetweet() ? status.getRetweetedStatus().getUser().getId() : status.getInReplyToUserId();

            if (target.id.equals(replyTo)) continue;

            if (replyTo != -1) {

                if (replyCount.containsKey(replyTo)) {

                    replyCount.get(replyTo).replyCount++;

                } else {

                    replyCount.put(replyTo, new InReplyTo(replyTo));

                }

            }

        }

        TreeSet<InReplyTo> result = new TreeSet<InReplyTo>(replyCount.values());

        LinkedList<Long> idList = new LinkedList<>();

        Iterator<InReplyTo> iter = result.iterator();

        while (iter.hasNext()) {

            FriendsList.InReplyTo to = iter.next();

            idList.add(to.userId);

        }

        msg.send("开始输出").async();

        String message = params[1] + "最近和谁互动最多 ？\n";

        iter = result.iterator();

        for (int index = 0; iter.hasNext(); index++) {

            InReplyTo to = iter.next();

            message += "\n" + (index + 1) + " · " + UserArchive.get(api, to.userId).bName();

        }

        message += "\n\n" + params[1] + "有哪些正在关注的关注者？\n";

        List<Long> ids;

        TrackTask.IdsList frs = TrackTask.friends.getById(account.id);

        if (frs != null) {

            ids = frs.ids;

        } else {

            try {

                ids = TApi.getAllFoIDs(api, account.id);

            } catch (TwitterException e) {

                ids = new LinkedList<>();

            }

        }

        try {

            ids.retainAll(TApi.getAllFoIDs(api, target.id));

        } catch (TwitterException e) {

            msg.send(NTT.parseTwitterException(e)).async();

            return;

        }

        for (Long follwing : ids) {

            message += "\n" + UserArchive.get(api, follwing).name;

        }

        msg.send(message).html().async();

    }

    class InReplyTo implements Comparable<InReplyTo> {

        public InReplyTo(Long userId) {

            this.userId = userId;

        }

        @Override
        public int compareTo(InReplyTo target) {

            return target.replyCount - replyCount;

        }

        Long userId;

        int replyCount = 1;

    }

}
