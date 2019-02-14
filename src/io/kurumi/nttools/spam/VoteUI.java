package io.kurumi.nttools.spam;

import io.kurumi.nttools.NTTBot;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.FragmentBase;
import io.kurumi.nttools.fragments.MainFragment;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.model.request.Edit;
import io.kurumi.nttools.model.request.Send;
import io.kurumi.nttools.timer.TimerTask;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.Markdown;
import io.kurumi.nttools.utils.UserData;
import java.util.Map;
import java.util.LinkedList;

public class VoteUI extends FragmentBase implements TimerTask {

    public static final VoteUI INSTANCE = new VoteUI();

    private static final String POINT_VOTE_AGREE = "p|a";
    private static final String POINT_VOTE_DISAGREE = "p|d";
    private static final String POINT_VOTE_PASS = "p|p";
    private static final String POINT_VOTE_REJ = "p|r";

    public void startVote(Fragment fragment, final SpamVote spam) {

        StringBuilder msg = new StringBuilder();

        if (spam.origin != null) {

            UserData origin = fragment.main.getUserData(spam.origin);

            msg.append("[").append(Markdown.encode(origin.name)).append("](https://t.me/").append(origin.userName).append(")");

        } else {

            msg.append("匿名用户");

        }

        msg.append("\n\n提议将 #账号").append(spam.twitterAccountId);

        msg.append("\n\n[").append(Markdown.encode(spam.twitterDisplyName)).append("](https://twitter.com/").append(spam.twitterScreenName).append(") ");

        msg.append("\n\n添加到公共分类 「").append(fragment.main.getSpamList(spam.listId).name).append(" 」");

        msg.append("\n\n原因是 : ").append(spam.spamCause).append("\n\n");

        Msg voteMsg = new Send(fragment, "@" + TwitterSpam.VOTE_CHANNEL, msg.toString())

            .buttons(new ButtonMarkup() {{

                    newButtonLine("同意", POINT_VOTE_AGREE, spam.id);
                    newButtonLine("反对", POINT_VOTE_DISAGREE, spam.id);

                }}).markdown().disableLinkPreview().send();

        spam.vote_message_id = voteMsg.messageId();
        spam.save();

    }

    @Override
    public boolean processCallbackQuery(UserData user, Callback callback, boolean point) {

        if (point) return false;

        switch (callback.data.getPoint()) {

                case POINT_VOTE_AGREE: break;
                case POINT_VOTE_DISAGREE :break;
                case POINT_VOTE_PASS:

                adminManage(user, callback, true);

                return true;

                case POINT_VOTE_REJ:

                adminManage(user, callback, false);

                return true;

                default : return false;

        }

        if (user.twitterAccounts.size() == 0) {

            callback.alert("为防止滥用 您必须到 @NTToolsBot 认证Twitter账号后参与投票 ପ( ˘ᵕ˘ ) ੭ ☆");

            return false;

        }


        String id = callback.data.getIndex();

        Boolean target = callback.data.getPoint().equals(POINT_VOTE_AGREE);

        SpamVote vote = callback.fragment.main.getSpamVote(id);

        String msg;

        if (target) {

            if (vote.agree.contains(user.id))  {

                vote.agree.remove(user.id);

                msg = "取消投票成功 o(´^｀)o";

            } else {

                if (vote.disagree.contains(user.id)) vote.disagree.remove(user.id);

                vote.agree.add(user.id);

                vote.save();

                msg = "投票成功 *٩(๑´∀`๑)ง*";

            }

        } else {

            if (vote.disagree.contains(user.id))  {

                vote.disagree.remove(user.id);

                msg = "取消投票成功 o(´^｀)o";

            } else {

                if (vote.agree.contains(user.id)) vote.agree.remove(user.id);

                vote.disagree.add(user.id);

                vote.save();

                msg = "投票成功 *٩(๑´∀`๑)ง*";

            }

        }

        vote.save();

        updateVote(callback.fragment, vote, user.isAdmin);

        callback.text(msg);

        return true;

    }

    public void updateVote(Fragment fragment, final SpamVote vote, final boolean admin) {

        StringBuilder msg = new StringBuilder();

        if (vote.origin != null) {

            UserData origin = fragment.main.getUserData(vote.origin);

            msg.append("[").append(Markdown.encode(origin.name)).append("](https://t.me/").append(origin.twitterAccounts.getFirst().screenName).append(")");

        } else {

            msg.append("有人");

        }

        msg.append("\n\n提议将 #账号").append(vote.twitterAccountId);

        msg.append("\n\n[").append(Markdown.encode(vote.twitterDisplyName)).append("](https://twitter.com/").append(vote.twitterScreenName).append(") ");

        msg.append("\n\n添加到公共分类 「").append(fragment.main.getSpamList(vote.listId).name).append(" 」");

        msg.append("\n\n原因是 : ").append(vote.spamCause).append("\n\n");

        msg.append(fragment.main.spam.formatSpam(vote));

        new Edit(fragment, "@" + TwitterSpam.VOTE_CHANNEL, vote.vote_message_id, msg.toString())
            .buttons(new ButtonMarkup() {{

                    newButtonLine("同意 : " + vote.agree.size(), POINT_VOTE_AGREE, vote.id);
                    newButtonLine("反对 : " + vote.disagree.size(), POINT_VOTE_DISAGREE, vote.id);

                    if (admin) {

                        newButtonLine()
                            .newButton("通过", POINT_VOTE_PASS, vote.id)
                            .newButton("否决", POINT_VOTE_REJ, vote.id);

                    }

                }}).markdown().disableLinkPreview().exec();
    }

    public void adminManage(UserData user, Callback callback, boolean target) {

        if (!user.isAdmin) {

            callback.alert("您 「不能」 滥权！");

            return;

        } 

        SpamVote vote = callback.fragment.main.getSpamVote(callback.data.getIndex());

        if (target) {

            callback.text("已通过");

            callback.fragment.main.spam.adminPassed(user, vote, "通过");

        } else {

            callback.text("已否决");

            callback.fragment.main.spam.adminRejected(user, vote, "否决");


        }

    }

    @Override
    public void run(MainFragment fragment) {

        for (SpamVote vote : fragment.getSpamVotes()) {

            updateVote(fragment, vote, false);

            if (System.currentTimeMillis() - vote.startTime >  4 * 60 * 60 * 1000) {

                if (vote.agree.size() > vote.disagree.size()) {

                    fragment.main.spam.votePassed(vote);

                } else {

                    fragment.main.spam.voteRejected(vote);

                }

            }

        }

        for (SpamList list : fragment.getSpamLists()) {

            long lastTime = list.getLong("last_spam_time", -1L);

            if (System.currentTimeMillis() - lastTime > 30 * 60 * 1000) {

                fragment.data.put("last_spam_time", System.currentTimeMillis());

                for (UserData user : fragment.getUsers()) {

                    for (TwiAccount account : user.twitterAccounts) {

                        if (!list.disables.containsKey(account)) {

                            new SpamTask(list, account).start();


                        }

                    }

                }

                for (Map.Entry<Long,Long> sub : list.disables.entrySet()) {

                    UserData user = fragment.getUserData(sub.getValue());

                    if (user == null) {

                        list.disables.remove(sub.getKey());
                        list.save();
                        continue;

                    }

                    TwiAccount account = user.findUser(sub.getKey());

                    if (account == null) {

                        list.disables.remove(sub.getKey());
                        list.save();
                        continue;

                    }


                }



            }



        }



    }


}
