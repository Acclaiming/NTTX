package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.NumberUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.fragment.twitter.archive.StatusArchive;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.fragment.twitter.status.MessagePoint;
import io.kurumi.ntt.fragment.twitter.tasks.TrackTask;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.NTT;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class UserActions extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerFunction("twuf","follow","unfo","mute","unmute","mute_rt","unmute_rt","block","unblock");
        registerPayload("twuf","follow","unfo","mute","unmute","mrt","umrt","block","unblock");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params) {

        requestTwitter(user,msg,true);

    }

    @Override
    public void onPayload(UserData user,Msg msg,String payload,String[] params) {

        requestTwitterPayload(user,msg,true);

    }

    @Override
    public void onTwitterFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        doAction(user,msg,function,params,account);

    }

    @Override
    public void onTwitterPayload(UserData user,Msg msg,String payload,String[] params,TAuth account) {

        doAction(user,msg,payload,params,account);

    }

    public void doAction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        Twitter api = account.createApi();

        long targetId = -1;

        if (params.length > 0) {

            for (String target : params) {

                if (NumberUtil.isNumber(target)) {

                    targetId = NumberUtil.parseLong(target);

                } else {

                    String screenName = NTT.parseScreenName(target);

                    try {

                        targetId = api.showUser(screenName).getId();

                    } catch (TwitterException e) {

                        msg.send(NTT.parseTwitterException(e)).exec();

                        return;

                    }

                }

            }

        } else if (msg.targetChatId == -1 && msg.isPrivate() && msg.isReply()) {

            MessagePoint point = MessagePoint.getFromStatus(msg.replyTo().message());
			
			if (point == null) point = MessagePoint.get(msg.replyTo().messageId());

            if (point == null) {

                msg.send("咱不知道目标是谁 (｡í _ ì｡)").exec();

                return;

            }

            if (point.type == 1) {

                targetId = StatusArchive.get(point.targetId).from;

            } else {

                targetId = point.targetId;

            }

        } else {

            msg.send("/" + function + " <ID|用户名|链接> / 或对私聊消息回复 (如果你觉得这条消息包含一个用户或推文)").exec();

            return;

        }

        UserArchive archive;

        try {

            archive = UserArchive.save(api.showUser(targetId));

        } catch (TwitterException e) {

            msg.send(NTT.parseTwitterException(e)).exec();

            return;

        }

        try {

            Relationship ship = api.showFriendship(account.id,targetId);

            if ("follow".equals(function)) {


                if (ship.isSourceBlockingTarget()) {

                    msg.send("你被 " + archive.urlHtml() + " 屏蔽了").html().point(0,targetId);

                    return;

                } else if (ship.isSourceFollowedByTarget()) {

                    msg.send("你已经关注了 " + archive.urlHtml()).html().point(0,targetId);

                    return;

                }

                try {

                    api.createFriendship(targetId);

                    msg.send((archive.isProtected ? "已发送关注请求给 " : "已关注 ") + archive.urlHtml() + " ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("关注失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("unfo".equals(function)) {

                if (ship.isSourceBlockingTarget()) {

                    msg.send("你已经把 " + archive.urlHtml() + " 屏蔽了").html().point(0,targetId);

                    return;

                } else if (!ship.isSourceFollowingTarget()) {

                    msg.send("你没有关注了 " + archive.urlHtml()).html().point(0,targetId);

                    return;

                }

                try {

                    api.destroyFriendship(targetId);

                    msg.send("已取关 " + archive.urlHtml() + " ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("取关失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("mute".equals(function)) {

                if (ship.isSourceMutingTarget()) {

                    msg.send("你已经停用了对 " + archive.urlHtml() + " 的通知").html().point(0,targetId);

                    return;

                }

                try {

                    api.createMute(targetId);

                    msg.send("已静音来自 " + archive.urlHtml() + " 的通知 ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("静音失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("unmute".equals(function)) {

                if (!ship.isSourceMutingTarget()) {

                    msg.send("你没有停用对 " + archive.urlHtml() + " 的通知 ~").html().point(0,targetId);

                    return;

                }

                try {

                    api.destroyMute(targetId);

                    msg.send("已启用对 " + archive.urlHtml() + " 的通知 ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("启用失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("mute_rt".equals(function) || "mrt".equals(function)) {

                if (!ship.isSourceWantRetweets()) {

                    msg.send("你已经屏蔽了 " + archive.urlHtml() + " 的转推 ~").html().point(0,targetId);

                    return;

                }

                try {

                    api.updateFriendship(targetId,ship.isSourceNotificationsEnabled(),false);

                    msg.send("已屏蔽 " + archive.urlHtml() + " 的转推 ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("屏蔽转推失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("unmute_rt".equals(function) || "umrt".equals(function)) {

                if (ship.isSourceWantRetweets()) {

                    msg.send("你没有屏蔽 " + archive.urlHtml() + " 的转推 ~").html().point(0,targetId);

                    return;

                }

                try {

                    api.updateFriendship(targetId,ship.isSourceNotificationsEnabled(),true);

                    msg.send("已取消屏蔽 " + archive.urlHtml() + " 的转推 ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("取消屏蔽转推失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("block".equals(function)) {

                if (ship.isSourceBlockingTarget()) {

                    msg.send("你已经把 " + archive.urlHtml() + " 屏蔽了 ~").html().point(0,targetId);

                    return;

                }

                try {

                    api.createBlock(targetId);

                    TrackTask.IdsList fo = TrackTask.followers.getById(account.id);

                    fo.ids.remove(targetId);

                    TrackTask.followers.setById(account.id,fo);

                    msg.send("已屏蔽 " + archive.urlHtml() + " ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("屏蔽失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("unblock".equals(function)) {

                if (!ship.isSourceBlockingTarget()) {

                    msg.send("你没有屏蔽 " + archive.urlHtml() + " ~").html().point(0,targetId);

                    return;

                }

                try {

                    api.destroyBlock(targetId);

                    msg.send("已解除屏蔽 " + archive.urlHtml() + " ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("解除屏蔽失败 :",NTT.parseTwitterException(e)).exec();

                }

            } else if ("twuf".equals(function)) {

                if (ship.isSourceBlockingTarget()) {

                    msg.send("你已经把 " + archive.urlHtml() + " 屏蔽了 ~").html().point(0,targetId);

                    return;

                }

                try {

                    api.createBlock(targetId);

                    api.destroyBlock(targetId);

                    TrackTask.IdsList fo = TrackTask.followers.getById(account.id);

                    fo.ids.remove(targetId);

                    TrackTask.followers.setById(account.id,fo);

                    msg.send("已双向取关 " + archive.urlHtml() + " ~").html().point(0,targetId);

                } catch (TwitterException e) {

                    msg.send("双向取关失败 :",NTT.parseTwitterException(e)).exec();

                }


            }


        } catch (TwitterException e) {

            msg.send("读取对方状态错误 :",NTT.parseTwitterException(e)).exec();

        }

    }

}
