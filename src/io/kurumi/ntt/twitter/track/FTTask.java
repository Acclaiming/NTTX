package io.kurumi.ntt.twitter.track;

import cn.hutool.core.thread.*;
import cn.hutool.json.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import twitter4j.*;

import cn.hutool.json.JSONArray;

public class FTTask extends TimerTask {

    static FTTask INSTANCE = new FTTask();

    public static JSONArray enable = SData.getJSONArray("data","track",true);

    static {
        
        enable = new JSONArray(new LinkedHashSet<Long>(enable.toList(Long.class)));
        save();
        
    }
    
	static HashMap<Long,LinkedList<Long>> frIndex = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> flIndex = new HashMap<>();

	static HashMap<Long,LinkedList<Long>> frSubIndex = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> flSubIndex = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> frSubIndexC = new HashMap<>();
    static HashMap<Long,LinkedList<Long>> flSubIndexC  = new HashMap<>();

    static Timer timer;

    public static void start() {

        stop();

        timer = new Timer("NTT Twitter Track Task");
        timer.schedule(INSTANCE,new Date(),15 * 60 * 1000);

    }

    public static void stop() {

        if (timer != null) timer.cancel();
        timer = null;

    }

    public static void save() {

        SData.setJSONArray("data","track",enable);

    }

    @Override
    public void run() {

		if (running.get() > 0) {

			return;

		}

		synchronized (INSTANCE) {

            frSubIndex = frSubIndexC;
            frSubIndexC = new HashMap<>();

            flSubIndex = flSubIndexC;
            flSubIndexC = new HashMap<>();


		}

        for (long userId : enable.toList(Long.class)) {

            startUserStackAsync(userId);

        }

        userTrackPool.execute(new Runnable() {

                @Override
                public void run() {

					running.incrementAndGet();

					while (running.get() != 1) {

						ThreadUtil.sleep(1000);

					}

                    synchronized (UTTask.pedding) {

                        UTTask.pedding.addAll(pedding);
                        pedding.clear();

                    }

					running.decrementAndGet();


                }

            });

    }

    LinkedHashSet<Long> pedding = new LinkedHashSet<>();

    ExecutorService userTrackPool = Executors.newFixedThreadPool(3);

	AtomicInteger running = new AtomicInteger();

    private void startUserStackAsync(final long userId) {

        userTrackPool.execute(new Runnable() {

                @Override
                public void run() {

					running.incrementAndGet();

                    startUserStack(userId);

					running.decrementAndGet();

                }

            });



    }


    void startUserStack(Long userId) {


        if (!TAuth.avilable(userId)) {

            enable.remove(userId.toString());

            save();

            return;

        }

        Twitter api = TAuth.get(userId).createApi();

        try {


            User me = api.verifyCredentials();

            if (me == null) return;

            BotDB.saveUser(me);

            LinkedList<Long> flLast = flIndex.containsKey(api.getId()) ? flIndex.get(api.getId()) : null;

            LinkedList<Long> flLatest = TApi.getAllFoIDs(api,api.getId());

            flIndex.put(api.getId(),flLatest);

			synchronized (INSTANCE) {

                for (long id : flLatest) {

                    LinkedList<Long> subIndex = flSubIndexC.get(id);

                    if (subIndex == null) {

                        subIndex = new LinkedList<>();

                    }

                    subIndex.add(userId);

                    flSubIndexC.put(id,subIndex);

                    if (flLast != null) {

                        if (!flLast.remove(id)) {

                            newFollower(userId,api,id);

                        }

                    }

                }

			}

            if (flLast != null && flLast.size() > 0) {

                for (int index = 0;index < flLast.size();index ++) {

                    long id = flLast.get(index);

                    lostFollower(userId,api,id);

                }

            }

			LinkedList<Long> allFr = TApi.getAllFrIDs(api,api.getId());

			frIndex.put(api.getId(),allFr);

            for (long id : allFr) {

                LinkedList<Long> subIndex = frSubIndexC.get(id);

                if (subIndex == null) {

                    subIndex = new LinkedList<>();

                }

                subIndex.add(userId);

                frSubIndexC.put(id,subIndex);

            }

            pedding.addAll(allFr);
            pedding.addAll(flLatest);

			/* 

             if (pedding.size() > 10000) {

             pedding = pedding.subList(0,10000);

             }

             while (pedding.size() > 0) {

             List<Long> target;

             if (pedding.size() > 100) {

             target = pedding.subList(0,100);

             pedding = pedding.subList(99,pedding.size());

             } else {

             target = pedding;

             pedding.clear();

             }

             ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

             for (User tuser : result) UserArchive.saveCache(tuser);

             }

			 */

        } catch (TwitterException e) {

            if (e.getErrorCode() == 326) {

                enable.remove(userId.toString());
                save();

                new Send(userId,"对不起，但是因乃的账号已被Twitter限制，已经自动关闭关注者历史监听 (需要登录 twitter.com 以解除限制并使用 /tstart 重新开启 (⁎˃ᆺ˂)").exec();

            } else if (e.getErrorCode() != 130) {

                BotLog.error("UserArchive ERROR",e);

            }

        }

    }

    String link = Html.a("姬生平","https://esu.wiki/姬生平");

    HashMap<Long,LinkedList<Long>> userBlock;
    HashMap<Long,LinkedList<Long>> userMute;


    String parseStatus(Twitter api,User user) {

        StringBuilder status = new StringBuilder();

        try {

            if (!api.showFriendship(api.getId(),user.getId()).isSourceFollowingTarget() && !user.isFollowRequestSent()) {

                if (user.isProtected()) status.append("这是一个是锁推用户 :)\n");

            }

        } catch (TwitterException e) {}

        // if (user.isFollowRequestSent()) status.append("乃发送了关注请求 :)\n");
        if (user.getStatusesCount() == 0) status.append("这个用户没有发过推 :)\n");
        if (user.getFavouritesCount() == 0) status.append("这个用户没有喜欢过推文 :)\n");
        if (user.getFollowersCount() < 20) status.append("这个用户关注者低 (").append(user.getFollowersCount()).append(")  :)\n");

        /*

         try {

         Relationship ship = api.showFriendship(user.getId(),917716145121009664L);

         if (ship.isTargetFollowingSource() && ship.isTargetFollowedBySource()) {

         status.append("这个用户与 ").append(link).append(" 互相关注 是萌萌的二次元 :)\n");

         } else if (ship.isSourceFollowingTarget()) {

         status.append("这个用户关注了 ").append(link).append(" :)\n");

         } else if (ship.isSourceFollowedByTarget()) {

         status.append("这个用户被 ").append(link).append(" 关注 是萌萌的二次元 :)\n");

         }

         } catch (TwitterException e) {}

         */

        String statusR = status.toString();

        if (statusR.endsWith("\n")) {

            statusR.substring(0,statusR.length() - 1);

        }

        return statusR;

    }

    void newFollower(Long userId,Twitter api,long id) {

        try {

            User follower = api.showUser(id);

            BotDB.saveUser(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            new Send(userId,(ship.isSourceFollowingTarget() ? "乃关注的 " : "") + TApi.formatUserNameHtml(follower) + " 关注了你 :)",parseStatus(api,follower)).enableLinkPreview().html().exec();

        } catch (TwitterException e) {

            if (BotDB.userExists(id)) {

                new Send(userId,BotDB.getUser(id).urlHtml() + " 关注你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(userId,"用户 (" + id + ") 关注了你 , 但是该账号已经不存在了 :(").enableLinkPreview().html().exec();


            }

        }

    }

    void lostFollower(Long userId,Twitter api,long id) {

        try {

            User follower = api.showUser(id);
            BotDB.saveUser(follower);

            Relationship ship = api.showFriendship(api.getId(),id);

            if (ship.isSourceBlockingTarget()) {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关并屏蔽了你 :)").enableLinkPreview().html().exec();

            } else if (follower.getFriendsCount() == 0) {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关了你，但对方关注人数为空，可能是账号异常 :)").enableLinkPreview().html().exec();

            } else {

                new Send(userId,TApi.formatUserNameHtml(follower) + " 取关了你 :)").enableLinkPreview().html().exec();

            }

        } catch (TwitterException e) {

            if (BotDB.userExists(id)) {

                new Send(userId,BotDB.getUser(id).urlHtml() + " 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();

            } else {

                new Send(userId,"用户 (" + id + ") 取关了你 , 因为该账号已经不存在了 :(").enableLinkPreview().html().exec();

            }

        }



    }

}
