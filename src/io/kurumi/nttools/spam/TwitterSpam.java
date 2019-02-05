package io.kurumi.nttools.spam;

import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.ButtonMarkup;
import io.kurumi.nttools.model.request.Send;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.Markdown;

public class TwitterSpam {

    public static final String PUBLIC_CHANNEL = "NTTSpamPublic";
    public static final String VOTE_CHANNEL = "NTTSpamVote";
    public static final String DISCUSS_GROUP = "NTTDiscuss";

    private Fragment fragment;

    public TwitterSpam(Fragment fragment) {

        this.fragment = fragment;

    }


    public void votePassed(SpamVote vote) {

        SpamList list = fragment.main.getSpamList(vote.listId);

        UserSpam spam = new UserSpam(list);

        spam.origin = vote.origin;

        spam.twitterAccountId = vote.twitterAccountId;
        spam.twitterScreenName = vote.twitterScreenName;
        spam.twitterDisplyName = vote.twitterDisplyName;

        spam.spamCause = vote.spamCause;

        spam.vote_message_id = vote.vote_message_id;

        list.spamUsers.add(spam);

        list.save();

        String[] passMsg = new String[] {

            "投票通过了将 [「" + Markdown.encode(spam.twitterDisplyName) + "」](https://twitter.com/" + spam.twitterScreenName + ")","","添加到公共列表 「 " + spam.belongTo.name + " 」 的决定",
            "","https://t.me/" + PUBLIC_CHANNEL

        };

        final Msg pubMsg = new Send(fragment, "@" + PUBLIC_CHANNEL, passMsg).markdown().disableLinkPreview().send();

        fragment.bot.execute(new EditMessageReplyMarkup("@" + VOTE_CHANNEL, vote.vote_message_id)
                             .replyMarkup(new ButtonMarkup() {{

                                                  newUrlButtonLine("结果 : 已通过", "https://t.me/" + PUBLIC_CHANNEL + "/" + pubMsg.messageId());

                                              }}.markup()));

        fragment.main.deleteSpamVote(vote.id);

    }

    public void voteRejected(SpamVote vote) {

        String[] passMsg = new String[] {

            "投票否决了将 [「" + Markdown.encode(vote.twitterDisplyName) + "」](https://twitter.com/" + vote.twitterScreenName + ")","","添加到公共列表 「 " + fragment.main.getSpamList(vote.listId).name + " 」 的决定",
            "","https://t.me/" + PUBLIC_CHANNEL

        };

        new Send(fragment, "@" + PUBLIC_CHANNEL, passMsg).markdown().disableLinkPreview().exec();

        final Msg pubMsg = new Send(fragment, "@" + PUBLIC_CHANNEL, passMsg).markdown().disableLinkPreview().send();

        fragment.bot.execute(new EditMessageReplyMarkup("@" + VOTE_CHANNEL, vote.vote_message_id)
                             .replyMarkup(new ButtonMarkup() {{

                                                  newUrlButtonLine("结果 : 已否决", "https://t.me/" + PUBLIC_CHANNEL + "/" + pubMsg.messageId());

                                              }}.markup()));

        fragment.main.deleteSpamVote(vote.id);

    }

    public void newSpam(UserSpam spam) {

        TwiAccount origin = fragment.main.getUserData(spam.origin).twitterAccounts.getFirst();

        String[] newSpamMsg = new String[] {

            "Twitter #用户" + spam.twitterAccountId + "\n\n[" + Markdown.encode(spam.twitterDisplyName) + "](https://twitter.com/" + spam.twitterScreenName + ") \n\n #" + spam.twitterScreenName + "\n\n已被添加到 公共分类 「 " + spam.belongTo.name + " 」","",
            "原因 : " + spam.spamCause,"",
            "操作人 : [" + Markdown.encode(origin.name) + "](" + origin.getUrl() + ")"

        };

        new Send(fragment, "@" + PUBLIC_CHANNEL, newSpamMsg).markdown().disableLinkPreview().exec();

    }

}
