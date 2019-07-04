package io.kurumi.ntt.fragment.twitter;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import java.util.HashSet;
import java.util.HashSet;
import java.util.LinkedList;
import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;

public class TApi {

    public static LinkedList<User> getListUsers(Twitter api, long id) throws TwitterException {


        LinkedList<User> list = new LinkedList<>();
        PagableResponseList<User> ppl;
        long cursor = -1;

        do {

            ppl = api.getUserListMembers(id, cursor);
            cursor = ppl.getNextCursor();

            list.addAll(ppl);

        } while (ppl.hasNext());

        return list;

    }

    public static long[] longMarge(long[] a, long[] b) {

        HashSet<Long> set = new HashSet<>();

        for (long sa : a) {

            set.add(sa);

        }

        for (long sb : b) {

            set.add(sb);

        }


        //   long[] ret = new long[set.size()];

        //     System.arraycopy(set.toArray(new Long[set.size()]),0,ret,0,set.size());


        return ArrayUtil.unWrap(set.toArray(new Long[set.size()]));
    }
	
	public static long[] getAllMuteIDs(Twitter api) throws TwitterException {

        LinkedList<Long> all = new LinkedList<>();

        IDs users = api.getMutesIDs(-1);

        for (Long id : users.getIDs()) all.add(id);

        while (users.hasNext()) {

            users = api.getMutesIDs(users.getNextCursor());

            for (Long id : users.getIDs()) all.add(id);


        }

        return ArrayUtil.unWrap(all.toArray(new Long[all.size()]));

    }


    public static LinkedList<User> getAllMute(Twitter api) throws TwitterException {

        LinkedList<User> all = new LinkedList<>();

        PagableResponseList<User> users = api.getBlocksList(-1);

        all.addAll(users);

        while (users.hasNext()) {

            users = api.getMutesList(users.getNextCursor());

            all.addAll(users);

        }

        return all;

    }
	

    public static long[] getAllBlockIDs(Twitter api) throws TwitterException {

        LinkedList<Long> all = new LinkedList<>();

        IDs users = api.getBlocksIDs(-1);

        for (Long id : users.getIDs()) all.add(id);

        while (users.hasNext()) {

            users = api.getBlocksIDs(users.getNextCursor());

            for (Long id : users.getIDs()) all.add(id);


        }

        return ArrayUtil.unWrap(all.toArray(new Long[all.size()]));

    }


    public static LinkedList<User> getAllBlock(Twitter api) throws TwitterException {

        LinkedList<User> all = new LinkedList<>();

        PagableResponseList<User> users = api.getBlocksList(-1);

        all.addAll(users);

        while (users.hasNext()) {

            users = api.getBlocksList(users.getNextCursor());

            all.addAll(users);

        }

        return all;

    }


    public static LinkedList<User> getAllFr(Twitter api, Long target) throws TwitterException {

        LinkedList<User> all = new LinkedList<>();

        PagableResponseList<User> users = api.getFriendsList(target, -1, 200);

        all.addAll(users);

        while (users.hasNext()) {

            users = api.getFriendsList(target, users.getNextCursor(), 200);

            all.addAll(users);

        }

        return all;

    }

    public static LinkedList<Long> getAllFrIDs(Twitter api, String target) throws TwitterException {

        LinkedList<Long> all = new LinkedList<>();

        IDs ids = api.getFriendsIDs(target, -1, 5000);

        for (long id : ids.getIDs()) {

            all.add(id);

        }

        while (ids.hasNext()) {

            ids = api.getFriendsIDs(target, ids.getNextCursor(), 5000);

            for (long id : ids.getIDs()) {

                all.add(id);

            }

        }

        return all;

    }


    public static LinkedList<Long> getAllFrIDs(Twitter api, Long target) throws TwitterException {

        LinkedList<Long> all = new LinkedList<>();

        IDs ids = api.getFriendsIDs(target, -1, 5000);

        for (long id : ids.getIDs()) {

            all.add(id);

        }

        while (ids.hasNext()) {

            ids = api.getFriendsIDs(target, ids.getNextCursor(), 5000);

            for (long id : ids.getIDs()) {

                all.add(id);

            }

        }

        return all;

    }

    public static LinkedList<User> getAllFo(Twitter api, String target) throws TwitterException {

        LinkedList<User> all = new LinkedList<>();

        PagableResponseList<User> users = api.getFollowersList(target, -1, 200);

        all.addAll(users);

        while (users.hasNext()) {

            users = api.getFollowersList(target, users.getNextCursor(), 200);

            all.addAll(users);

        }

        return all;

    }

    public static LinkedList<User> getAllFo(Twitter api, Long target) throws TwitterException {

        LinkedList<User> all = new LinkedList<>();

        PagableResponseList<User> users = api.getFollowersList(target, -1, 200);

        all.addAll(users);

        while (users.hasNext()) {

            users = api.getFollowersList(target, users.getNextCursor(), 200);

            all.addAll(users);

        }

        return all;

    }

    public static LinkedList<Long> getAllFoIDs(Twitter api, Long target) throws TwitterException {

        LinkedList<Long> all = new LinkedList<>();

        IDs ids = api.getFollowersIDs(target, -1, 5000);

        for (long id : ids.getIDs()) {

            all.add(id);

        }

        while (ids.hasNext()) {

            ids = api.getFollowersIDs(target, ids.getNextCursor(), 5000);

            for (long id : ids.getIDs()) {

                all.add(id);
            }

        }

        return all;

    }

    public static LinkedList<Long> getAllFoIDs(Twitter api, String target) throws TwitterException {

        LinkedList<Long> all = new LinkedList<>();

        IDs ids = api.getFollowersIDs(target, -1, 5000);

        for (long id : ids.getIDs()) {

            all.add(id);

        }

        while (ids.hasNext()) {

            ids = api.getFollowersIDs(target, ids.getNextCursor(), 5000);

            for (long id : ids.getIDs()) {

                all.add(id);
            }

        }

        return all;

    }


    public static HashSet<Status> getContextStatusWhenSearchRated(Twitter api, Status status, long[] target) throws TwitterException {

        Status top = status;

        HashSet<Status> all = new HashSet<>();

        all.add(status);

        try {

            while (top.getInReplyToStatusId() != -1 || top.getQuotedStatusId() != -1) {

                if (top.getInReplyToStatusId() != -1) {


                    Status superStatus = api.showStatus(top.getInReplyToStatusId());


                    if (target == null || ArrayUtil.contains(target, superStatus.getUser().getId())) {

                        top = superStatus;

                        all.add(superStatus);

                    } else break;


                } else {

                    Status superStatus = top.getQuotedStatus();

                    if (target == null || ArrayUtil.contains(target, superStatus.getUser().getId())) {

                        top = superStatus;

                        all.add(status);

                    } else break;

                }

            }

        } catch (TwitterException exc) {

            // 有锁推推文

        }

        return all;

    }

    public static HashSet<Status> getContextStatus(Twitter api, Status status, long[] target) throws TwitterException {

        Status top = status;

        HashSet<Status> all = new HashSet<>();

        all.add(status);

        try {

            while (top.getInReplyToStatusId() != -1 || top.getQuotedStatusId() != -1) {

                if (top.getInReplyToStatusId() != -1) {
                    
                    Status superStatus = api.showStatus(top.getInReplyToStatusId());
                    
                    if (target == null || ArrayUtil.contains(target, superStatus.getUser().getId())) {

                        top = superStatus;

                        all.add(superStatus);

                    } else break;


                } else {

                    Status superStatus = top.getQuotedStatus();

                    if (target == null || ArrayUtil.contains(target, superStatus.getUser().getId())) {

                        top = superStatus;

                        all.add(status);

                    } else break;

                }

            }

        } catch (TwitterException exc) {

            // 有锁推推文

        }

        all.addAll(loopReplies(api, top, target));

        return all;


    }

    public static LinkedList<Status> loopReplies(Twitter api, Status s, long[] target) throws TwitterException {

        LinkedList<Status> list = new LinkedList<>();

        for (Status ss : getReplies(api, s)) {

            list.add(ss);

            if (target == null || ArrayUtil.contains(target, ss.getUser().getId())) {

                list.addAll(loopReplies(api, ss, target));

            }

        }

        return list;

    }

    public static LinkedList<Status> getReplies(Twitter api, Status status) throws TwitterException {

        LinkedList<Status> list = new LinkedList<>();

        QueryResult resp = api.search(new Query()
                .query("to:" + status
                        .getUser()
                        .getScreenName())
                .sinceId(status.getId())
				.count(100)
                .resultType(Query.RECENT));

        for (Status s : resp.getTweets()) {

            if (s.getInReplyToStatusId() == status.getId()) list.add(s);

        }
		
        while (resp.hasNext()) {

            resp = api.search(resp.nextQuery());

            for (Status s : resp.getTweets()) {

                if (s.getInReplyToStatusId() == status.getId()) list.add(s);

            }

        }

        return list;


    }

    public static LinkedList<UserList> getLists(Twitter api) throws IllegalStateException, TwitterException {

        return new LinkedList<UserList>(api.getUserLists(api.getId(), true));

    }


    public static LinkedList<UserList> getSubLists(Twitter api) throws IllegalStateException, TwitterException {

        LinkedList<UserList> list = new LinkedList<>();

        PagableResponseList<UserList> sublist = api.getUserListSubscriptions(api.getId(), -1);
        list.addAll(sublist);

        if (sublist.hasNext()) {

            sublist = api.getUserListSubscriptions(api.getId(), sublist.getNextCursor());
            list.addAll(sublist);

        }

        return list;

    }


    public static LinkedList<UserList> getAllLists(Twitter api) throws IllegalStateException, TwitterException {

        LinkedList<UserList> list = new LinkedList<>();

        ResponseList<UserList> ownlists = api.getUserLists(api.getId());
        list.addAll(ownlists);

        PagableResponseList<UserList> sublist = api.getUserListSubscriptions(api.getId(), -1);
        list.addAll(sublist);

        if (sublist.hasNext()) {

            sublist = api.getUserListSubscriptions(api.getId(), sublist.getNextCursor());
            list.addAll(sublist);

        }

        return list;

    }


    public static String getContext(Status status) {

        if (status.getInReplyToStatusId() != -1) {

            return StrUtil.subAfter(status.getText(), "@" + status.getInReplyToScreenName() + " ", false);

        } else if (status.getQuotedStatusId() != -1) {

            return status.getText();

        }

        String text = status.getText();

        if (!text.contains("@")) return text;

        String sar = StrUtil.subAfter(text, "@", true);

        if (sar.contains(" ")) {

            return StrUtil.subAfter(sar, " ", false);

        } else {

            return "@" + sar;

        }

    }

}
