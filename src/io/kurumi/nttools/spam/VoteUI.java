package io.kurumi.nttools.spam;

import com.pengrad.telegrambot.request.EditMessageText;
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
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import java.util.Map;

public class VoteUI extends FragmentBase implements TimerTask {

    public static final VoteUI INSTANCE = new VoteUI();

    private static final String POINT_VOTE_AGREE = "p|a";
    private static final String POINT_VOTE_DISAGREE = "p|d";

    public void startVote(Fragment fragment, final SpamVote spam) {

        StringBuilder msg = new StringBuilder();

        if (spam.origin != null) {

            UserData origin = fragment.main.getUserData(spam.origin);

            msg.append("[").append(Markdown.encode(origin.twitterAccounts.getFirst().name)).append("](https://twitter.com/").append(origin.twitterAccounts.getFirst().screenName).append(")");

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
    public boolean processCallbackQuery(UserData user, Callback callback) {

        switch (callback.data.getPoint()) {

                case POINT_VOTE_AGREE: break;
                case POINT_VOTE_DISAGREE :break;
                default : return false;

        }

        if (user.twitterAccounts.size() == 0) {

            callback.alert("为防止滥用 您必须到 @NTToolsBot 认证Twitter账号后参与投票 ପ( ˘ᵕ˘ ) ੭ ☆");

            return false;

        }


        String id = callback.data.getIndex();

        Boolean target = callback.data.getPoint().equals(POINT_VOTE_AGREE);

        SpamVote vote = callback.fragment.main.getSpamVote(id);

        if (target) {

            if (vote.agree.contains(user.id))  {

                vote.agree.remove(user.id);

                callback.text("取消投票成功 o(´^｀)o");

            } else {

                if (vote.disagree.contains(user.id)) vote.disagree.remove(user.id);

                vote.agree.add(user.id);

                vote.save();

                callback.text("投票成功 *٩(๑´∀`๑)ง*");

            }

        } else {

            if (vote.disagree.contains(user.id))  {

                vote.disagree.remove(user.id);

                callback.text("取消投票成功 o(´^｀)o");

            } else {

                if (vote.agree.contains(user.id)) vote.agree.remove(user.id);

                vote.disagree.add(user.id);

                vote.save();

                callback.text("投票成功 *٩(๑´∀`๑)ง*");

            }

        }

        updateVote(callback.fragment, vote);

        return true;

    }

    public void updateVote(Fragment fragment, final SpamVote vote) {

        StringBuilder msg = new StringBuilder();

        if (vote.origin != null) {

            UserData origin = fragment.main.getUserData(vote.origin);

            msg.append("[").append(Markdown.encode(origin.twitterAccounts.getFirst().name)).append("](https://twitter.com/").append(origin.twitterAccounts.getFirst().screenName).append(")");

        } else {

            msg.append("匿名用户");

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

                }}).markdown().disableLinkPreview().exec();
    }

    @Override
    public void run(MainFragment fragment) {

        for (SpamVote vote : fragment.getSpamVotes()) {

            updateVote(fragment, vote);

            if (System.currentTimeMillis() - vote.startTime > 0.5 * 24 * 60 * 60 * 1000) {

                if (vote.agree.size() > vote.disagree.size()) {

                    fragment.main.spam.votePassed(vote);

                } else {

                    fragment.main.spam.voteRejected(vote);

                }

            }

        }

        for (SpamList list : fragment.getSpamLists()) {

            long lastTime = list.getLong("last_spam_time", -1L);

            if (System.currentTimeMillis() - lastTime > 10 * 60 * 1000) {

                for (Map.Entry<Long,Long> sub : list.subscribers.entrySet()) {

                    UserData user = fragment.getUserData(sub.getValue());

                    TwiAccount account = user.findUser(sub.getKey());

                    new SpamTask(list, account).start();

                }


                fragment.data.put("last_spam_time", System.currentTimeMillis());

            }



        }



    }


}
