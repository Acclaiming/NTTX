package io.kurumi.ntt.spam;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.db.WrongUse;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.twitter.TwitterUI;
import cn.hutool.core.util.NumberUtil;
import twitter4j.Twitter;
import twitter4j.User;
import twitter4j.TwitterException;
import java.util.Map;

public class SpamUI extends Fragment {

    public static SpamUI INSTANCE = new SpamUI();

    @Override
    public boolean onMsg(UserData user, Msg msg) {

        if (!msg.isCommand()) return false;

        switch (msg.commandName()) {

                case "show" : show(user, msg);break;

               case "tag" : tag(user, msg); break;

        }

        return true;

    }

    void tag(UserData user, Msg msg) {

        if (msg.commandParms().length == 0) {

            StringBuilder usage = new StringBuilder("用法 :\n\n");

            usage.append("/tag <名称> [查看]\n\n");

            usage.append("/tag new [创建]\n\n");

            usage.append("/tag del <名称> [删除]\n\n");
            
            usage.append("/tag desc <名称> [修改]\n\n");

            usahe.
            
            usage.append(WrongUse.incrWithMsg(user));

            msg.reply(usage.toString()).exec();

            return;

        }

        String tagName = msg.commandParms()[0];

        if ("new".equals(tagName)) {

            if (msg.commandParms().length < 2) {
                
                msg.reply("用法 :\n","/tag new <名称>\n",WrongUse.incrWithMsg(user)).exec();
                
                return;
                
            }
            
            tagName = msg.commandParms()[1];
            
            new SpamTag();

        }

        SpamTag tag = SpamTag.get(msg.commandParms()[0]);

        if (tag == null) {

            msg.reply(

        }

    }

    void show(UserData user, Msg msg) {

        if (msg.message().forwardFromMessageId() != null) {

            // TODO

        } else if (msg.commandParms().length != 1) {

            StringBuilder usage = new StringBuilder();

            usage.append("用法 :\n\n");

            usage.append("/show <Twitter用户名/链接> [查找联合封禁记录]\n\n");

            //  usage.append("/show <群内成员用户名> [查看对方Twitter]\n\n");

            //  usage.append("/show (对群内成员信息回复) [查看对方Twitter]\n\n");

            usage.append(WrongUse.incrWithMsg(user));

            msg.reply(usage.toString()).exec();

            return;

        }

        String target = msg.commandParms()[0];

        if (target.startsWith("@")) {

            // TODO

        } else if (target.contains("twitter.com/")) {

            target = StrUtil.subAfter(target, "twitter.com/", false).trim();

            if (target.contains("?")) target = StrUtil.subBefore(target, "?", false);

            if (target.contains(" ")) target = StrUtil.subBefore(target, " ", false);

            showTwitterUser(user, msg, target);

        } else {

            showTwitterUser(user, msg, target.trim());

        }

    }

    void showTwitterUser(UserData user, Msg msg, String target) {

        if (user.cTU() == null) {

            TwitterUI.noAuth(user, msg);

            return;

        }

        Twitter api = user.cTU().createApi();

        User u;

        try {

            if (NumberUtil.isLong(target)) {

                Long id = NumberUtil.parseLong(target);

                u = api.showUser(id);

            } else {

                u = api.showUser(target);

            }

        } catch (TwitterException e) {

            msg.send("查无此人 ( ￣▽￣)σ").exec();

            return;

        }

        TwiREC rec;

        if (!TwiREC.cache.containsKey(user.id)) {

            rec = new TwiREC();

            rec.accountId = u.getId();
            rec.screenName = u.getScreenName();
            rec.displayName = u.getName();

            rec.save();

        } else {

            rec = TwiREC.get(u.getId());

        }

        if (rec.tags.isEmpty()) {

            msg.send(rec.nameMarkdown() + " 没有标签记录 ( ￣▽￣)σ").markdown().exec();

        } else {

            StringBuilder spam = new StringBuilder(rec.nameMarkdown());

            spam.append(" 在分类 :");

            for (Map.Entry<SpamTag,String> tag : rec.tags.entrySet()) {

                spam.append("\n\n").append(tag.getKey().name).append(" : ").append(tag.getValue());

            }

        }

    }

}
