package io.kurumi.ntt.fragment.twitter.track;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpUtil;
import com.mongodb.client.FindIterable;
import com.neovisionaries.i18n.CountryCode;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.response.SendResponse;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.Launcher;
import io.kurumi.ntt.db.Data;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.request.Send;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.fragment.twitter.auto.AutoTask;
import io.kurumi.ntt.fragment.twitter.status.MessagePoint;
import io.kurumi.ntt.utils.BotLog;
import twitter4j.*;

import java.io.File;
import java.util.*;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import io.kurumi.ntt.utils.NTT;
import io.kurumi.ntt.fragment.BotFragment;

public class TrackTask extends TimerTask {

    public static TrackTask INSTANCE = new TrackTask();
    public static Data<IdsList> followers = new Data<IdsList>("Followers", IdsList.class);
    public static Data<IdsList> friends = new Data<IdsList>("Friends", IdsList.class);

    public static void onUserChange(UserArchive archive, String change) {
		
        if (TrackUI.data.collection.countDocuments(and(eq("_id", archive.id), eq("hideChange", true))) > 0) {

            return;

        }

        FindIterable<IdsList> subFr = friends.collection.find(eq("ids", archive.id));
        FindIterable<IdsList> subFo = followers.collection.find(eq("ids", archive.id));

        LinkedList<Long> processed = new LinkedList<>();

        for (IdsList sub : subFr) {


            TAuth account = TAuth.getById(sub.id);

            if (account == null) {

                friends.deleteById(sub.id);

                continue;

            }

            TrackUI.TrackSetting setting = TrackUI.data.getById(account.id);

            if (setting == null || (!setting.followers && !setting.followersInfo && !setting.followingInfo)) {

                friends.deleteById(account.id);
                followers.deleteById(account.id);

                if (setting != null) TrackUI.data.deleteById(account.id);

                return;

            }

            if (setting.followingInfo) {

                processChangeSend(archive, account, change, setting);

                processed.add(account.id);
                processed.add(account.user);

            }

        }

        for (IdsList sub : subFo) {

            if (processed.contains(sub.id)) continue;

            TAuth account = TAuth.getById(sub.id);

            if (account == null) {

                friends.deleteById(sub.id);

                continue;

            }

            System.out.println("sub : " + account.archive().name);

            TrackUI.TrackSetting setting = TrackUI.data.getById(account.id);

            if (setting == null || (!setting.followers && !setting.followersInfo && !setting.followingInfo)) {

                friends.deleteById(account.id);
                followers.deleteById(account.id);

                if (setting != null) TrackUI.data.deleteById(account.id);

                return;

            }

            if (processed.contains(account.user)) continue;

            if (setting.followersInfo) {

                processChangeSend(archive, account, change, setting);

                processed.add(account.id);
                processed.add(account.user);

            }

        }

    }

    static void processChangeSend(UserArchive archive, TAuth account, String change, TrackUI.TrackSetting setting) {

        StringBuilder msg = new StringBuilder(TAuth.data.countByField("user", account.user) > 1 ? account.archive().urlHtml() + " : " : "");

        boolean isfo = followers.fieldEquals(account.id, "ids", archive.id);
        boolean isfr = friends.fieldEquals(account.id, "ids", archive.id);

        if (isfo && isfr) msg.append("与乃互关");
        else if (isfo) msg.append("关注乃");
        else msg.append("乃关注");

        msg.append("的 ").append(archive.urlHtml()).append(" ( #").append(archive.oldScreenName()).append(" ) :\n").append(change);

        if ((archive.oldPhotoUrl == null || archive.photoUrl == null) && (archive.oldBannerUrl == null || archive.bannerUrl == null)) {

            new Send(account.user, msg.toString()).html().point(0, archive.id);

        } else if (archive.oldPhotoUrl != null) {

            File photo = new File(Env.CACHE_DIR, "twitter_profile_images/" + FileUtil.getName(archive.photoUrl));

            if (!photo.isFile()) {

                HttpUtil.downloadFile(archive.photoUrl, photo);

            }

            SendResponse resp = Launcher.INSTANCE.bot().execute(new SendPhoto(account.user, photo).caption(msg.toString()).parseMode(ParseMode.HTML));

            if (resp.isOk()) {

                MessagePoint.set(resp.message().messageId(), 0, archive.id);

            }

        } else {

            File photo = new File(Env.CACHE_DIR, "twitter_banner_images/" + archive.id + "/" + System.currentTimeMillis() + ".jpg");

            if (!photo.isFile()) {

                HttpUtil.downloadFile(archive.bannerUrl, photo);

            }

            SendResponse resp = Launcher.INSTANCE.bot().execute(new SendPhoto(account.user, photo).caption(msg.toString()).parseMode(ParseMode.HTML));

            if (resp.isOk()) {

                MessagePoint.set(resp.message().messageId(), 0, archive.id);

            }

        }

    }

    public static void start() {

        BotFragment.mainTimer.schedule(INSTANCE, new Date(), 3 * 60 * 1000);

    }

    @Override
    public void run() {

        LinkedList<TAuth> remove = new LinkedList<>();

        for (TAuth account : TAuth.data.collection.find()) {

            TrackUI.TrackSetting setting = TrackUI.data.getById(account.id);

            if (setting == null) setting = new TrackUI.TrackSetting();

            Twitter api = account.createApi();

            try {

                api.verifyCredentials();

                //if (setting.followers || setting.followersInfo || setting.followingInfo) {

                doTracking(account, setting, api, UserData.get(account.user));

                //}

            } catch (TwitterException e) {

                if (e.getErrorCode() == 89 || e.getErrorCode() == 215 || e.getErrorCode() == 215) {

                    remove.add(account);

                } else if (e.getErrorCode() == 326) {

                    // 被限制;

                } else if (e.getErrorCode() != 130) {

                    BotLog.error("UserArchive ERROR", e);

                }
            }


        }

        for (TAuth account : remove) {

            TrackUI.data.deleteById(account.id);
            TAuth.data.deleteById(account.id);

            new Send(account.user, "对不起，但是因乃的账号已停用 / 冻结 / NTT被取消授权，已移除 (⁎˃ᆺ˂)").exec();

            new Send(Env.GROUP, "Invalid Auth : " + UserData.get(account.user).userName() + " -> " + account.archive().urlHtml()).html().exec();

        }


    }

    void doTracking(TAuth account, TrackUI.TrackSetting setting, Twitter api, UserData user) throws TwitterException {

        List<Long> lostFolowers = followers.containsId(account.id) ? followers.getById(account.id).ids : null;
        List<Long> newFollowers = TApi.getAllFoIDs(api, account.id);

        if (lostFolowers == null) {

            followers.setById(account.id, new IdsList(account.id, newFollowers));

            return;

        } else {

            followers.setById(account.id, new IdsList(account.id, newFollowers));

        }

        friends.setById(account.id, new IdsList(account.id, TApi.getAllFrIDs(api, account.id)));

        List<Long> retains = new LinkedList<>();

        if (!newFollowers.equals(lostFolowers)) {

            retains.addAll(lostFolowers);
            retains.retainAll(newFollowers);

            lostFolowers.removeAll(retains);
            newFollowers.removeAll(retains);

            for (Long newfollower : newFollowers) {

                newFollower(account, api, newfollower, setting.followers);

            }

            for (Long lostFolower : lostFolowers) {

                lostFollower(account, api, lostFolower, setting.followers);

            }

        }

        while (!retains.isEmpty()) {

            List<Long> target;

            if (retains.size() > 100) {

                target = retains.subList(0, 100);

                retains = new LinkedList<Long>(retains.subList(99, retains.size()));


            } else {

                target = new LinkedList<>();
                target.addAll(retains);

                retains.clear();

            }

            try {

                ResponseList<User> result = api.lookupUsers(ArrayUtil.unWrap(target.toArray(new Long[target.size()])));

                for (User tuser : result) {

                    target.remove(tuser.getId());

                    UserArchive.save(tuser);

                }

                for (Long da : target) {

                    UserArchive.saveDisappeared(da);

                }

            } catch (TwitterException e) {

                if (e.getErrorCode() == 17) {

                    for (Long da : target) {

                        UserArchive.saveDisappeared(da);

                    }

                }

            }


        }

    }

    String parseStatus(Twitter api, User user) {

        StringBuilder status = new StringBuilder();

        try {

            if (!api.showFriendship(api.getId(), user.getId()).isSourceFollowingTarget() && !user.isFollowRequestSent()) {

                if (user.isProtected()) status.append("\n这是一个是锁推用户");

            }

        } catch (TwitterException e) {
        }

        // if (user.isFollowRequestSent()) status.append("乃发送了关注请求 :)\n");
        if (user.getStatusesCount() == 0) status.append("\n这个用户没有发过推文");
        if (user.getFavouritesCount() == 0) status.append("\n这个用户没有喜欢过推文 :)");
        if (user.getFollowersCount() < 20)
            status.append("\n这个用户关注者低 (").append(user.getFollowersCount()).append(")  :)");

        if (user.getWithheldInCountries() != null) {

            status.append("\n警告 : 此账号违反了");

            for (String countryCode : user.getWithheldInCountries()) {

                CountryCode country = CountryCode.getByAlpha2Code(countryCode);

                status.append(country.toLocale().toString()).append(" ");

            }

            status.append("的当地法律 被Twitter标识");

        }

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

            statusR.substring(0, statusR.length() - 1);

        }

        return statusR;

    }

    void newFollower(TAuth auth, Twitter api, long id, boolean notice) {

        try {

            User follower = api.showUser(id);

            UserArchive archive = UserArchive.save(follower);

            Relationship ship = api.showFriendship(auth.id, id);

            if (notice) {

                StringBuilder msg = new StringBuilder();

                msg.append(ship.isSourceFollowingTarget() ? "已关注的 " : "").append(archive.urlHtml()).append(" #").append(archive.screenName).append(" 关注了你 :)").append(parseStatus(api, follower));

                if (auth.multiUser()) msg.append("\n\n账号 : #").append(auth.archive().screenName);

                new Send(auth.user, msg.toString()).html().point(0, archive.id);

            }

            AutoTask.onNewFollower(auth, api, archive, ship);

        } catch (TwitterException e) {

            if (!notice) return;

			/*
			
            StringBuilder msg = new StringBuilder(UserArchive.contains(id) ? UserArchive.get(id).urlHtml() : "无记录的用户 : (" + id + ")").append("关注了你").append("\n状态异常: ").append(NTT.parseTwitterException(e));

            if (auth.multiUser()) msg.append("\n\n账号 : #").append(auth.archive().screenName);

            new Send(auth.user, msg.toString()).html().point(0, id);

			*/

        }

    }

    void lostFollower(TAuth auth, Twitter api, long id, boolean notice) {

        try {

            User follower = api.showUser(id);
            UserArchive archive = UserArchive.save(follower);

            Relationship ship = api.showFriendship(id, auth.id);

            if (notice) {

                StringBuilder msg = new StringBuilder();

                msg.append(ship.isSourceFollowedByTarget() ? "已关注的 " : "").append(archive.urlHtml()).append(" #").append(archive.screenName).append(ship.isSourceFollowingTarget() ? " 账号异常了 :(" : " 取关了你 :)").append(parseStatus(api, follower));

                if (auth.multiUser()) msg.append("\n\n账号 : #").append(auth.archive().screenName);

                new Send(auth.user, msg.toString()).html().point(0, archive.id);

                
            }

        } catch (TwitterException e) {

            if (!notice) return;

            StringBuilder msg = new StringBuilder(UserArchive.contains(id) ? UserArchive.get(id).urlHtml() : "无记录的用户 : (" + id + ")").append(" 取关了你\n状态异常 : ").append(NTT.parseTwitterException(e));

            if (auth.multiUser()) msg.append("\n\n账号 : #").append(auth.archive().screenName);

            new Send(auth.user, msg.toString()).html().point(0, id);

        }


    }

    public static class IdsList {

        public Long user;

        public Long id;
        public List<Long> ids;

        public IdsList() {
        }

        public IdsList(Long id, List<Long> ids) {

            this.id = id;
            this.ids = ids;

        }

    }


}
