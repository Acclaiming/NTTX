package io.kurumi.nttools.twitter;

import twitter4j.*;
import java.util.*;
import java.lang.reflect.*;
import cn.hutool.core.util.*;

public class TApi {

    public static String formatUserName(User u) {

        return "「" + u.getName() + "」 (@" + u.getScreenName() + ")";

    }

    public static LinkedList<User> getListUsers(Twitter api, long id) throws TwitterException {


        LinkedList<User> list = new LinkedList<>();
        PagableResponseList<User> ppl;
        long cursor = -1;

        do {

            ppl = api.getUserListMembers(id, cursor);
            cursor = ppl.getNextCursor();

            list.addAll(ppl);

        } while(ppl.hasNext());

        return list;

    }

    public static long[] longMarge(long[] a, long[] b) {

        LinkedHashSet<Long> set = new LinkedHashSet<>();

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

    public static long[] getAllFr(Twitter api) throws TwitterException {

        long[] all = new long[api.verifyCredentials().getFriendsCount()];

        int index = 0;

        IDs ids = api.getFriendsIDs(-1);

        for (long id : ids.getIDs()) {

            all[index] = id;

            index ++;

        }

        while (ids.hasNext()) {

            ids = api.getFriendsIDs(ids.getNextCursor());

            for (long id : ids.getIDs()) {

                all[index] = id;

                index ++;

            }

        }

        return all;

    }

    public static long[] getAllFo(Twitter api) throws TwitterException {

        long[] all = new long[api.verifyCredentials().getFollowersCount()];

        int index = 0;

        IDs ids = api.getFollowersIDs(-1);

        for (long id : ids.getIDs()) {

            all[index] = id;

            index ++;

        }

        while (ids.hasNext()) {

            ids = api.getFollowersIDs(ids.getNextCursor());

            for (long id : ids.getIDs()) {

                all[index] = id;

                index ++;

            }

        }

        return all;

    }

    public static LinkedHashSet<Status> getContextStatusWhenSearchRated(Twitter api, Status status, long[] target) throws TwitterException {

        Status top = status;

        LinkedHashSet<Status> all = new LinkedHashSet<>();

        all.add(status);

        try {

            while (top.getInReplyToStatusId() != -1 || top.getQuotedStatusId() != -1) {

                if (top.getInReplyToStatusId() != -1) {



                    Status superStatus =  api.showStatus(top.getInReplyToStatusId());



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
    public static LinkedHashSet<Status> getContextStatus(Twitter api, Status status, long[] target) throws TwitterException {

        Status top = status;

        LinkedHashSet<Status> all = new LinkedHashSet<>();

        all.add(status);

        try {

            while (top.getInReplyToStatusId() != -1 || top.getQuotedStatusId() != -1) {

                if (top.getInReplyToStatusId() != -1) {



                    Status superStatus =  api.showStatus(top.getInReplyToStatusId());



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
                                      .resultType(Query.RECENT));



        for (Status s : resp.getTweets()) {

            if (s.getInReplyToStatusId() == status.getId()
                || s.getQuotedStatusId() == status.getId()) list.add(s);

        }

        while (resp.hasNext()) {

            resp = api.search(resp.nextQuery());

            for (Status s : resp.getTweets()) {

                if (s.getInReplyToStatusId() == status.getId()
                    || s.getQuotedStatusId() == status.getId()) list.add(s);

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

            sublist  = api.getUserListSubscriptions(api.getId(), sublist.getNextCursor());
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

            sublist  = api.getUserListSubscriptions(api.getId(), sublist.getNextCursor());
            list.addAll(sublist);

        }

        return list;

    }



    public static Status reply(Twitter api, Status status, String contnent) throws TwitterException {
        
        if (status.getQuotedStatusId() == -1 && status.getInReplyToStatusId() == -1) {

            return api.updateStatus(new StatusUpdate(contnent).inReplyToStatusId(status.getId()));

        }




        String reply = "@" + status.getUser().getScreenName() + " ";

        Status superStatus = status;

        while (superStatus.getQuotedStatusId() != -1) {

            superStatus = superStatus.getQuotedStatus();

            if (!reply.contains(superStatus.getUser().getScreenName())) {

                reply = "@" + superStatus.getUser().getScreenName() + "" + reply;

            }

        }

        reply = reply + contnent;

        return api.updateStatus(new StatusUpdate(reply).inReplyToStatusId(status.getId()));

    }
    
    public static String getContext(Status status) {
        
        if (status.getInReplyToStatusId() != -1) {
            
            return StrUtil.subAfter(status.getText(),"@" + status.getInReplyToScreenName() + " ",false);
            
        } else if (status.getQuotedStatusId() != -1) {
            
            return status.getText();
            
        }
        
        String text = status.getText();
        
        if (!text.contains("@")) return text;
        
        String sar = StrUtil.subAfter(text,"@",true);
        
        if (sar.contains(" ")) {
            
            return StrUtil.subAfter(sar," ",false);
            
        } else {
            
            return "@" + sar;
            
        }
        
    }

}