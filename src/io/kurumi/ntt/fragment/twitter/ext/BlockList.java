package io.kurumi.ntt.fragment.twitter.ext;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.pengrad.telegrambot.request.SendDocument;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.abs.Msg;
import io.kurumi.ntt.fragment.abs.TwitterFunction;
import io.kurumi.ntt.fragment.twitter.TApi;
import io.kurumi.ntt.fragment.twitter.TAuth;
import io.kurumi.ntt.utils.NTT;

import java.util.LinkedList;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class BlockList extends TwitterFunction {

    public static BlockList INSTANCE = new BlockList();

    @Override
    public void functions(LinkedList<String> names) {

        names.add("bl");

    }

    @Override
    public int target() {

        return Private;

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params, TAuth account) {

        try {

            Twitter api = account.createApi();

            String name = "@" + api.verifyCredentials().getScreenName();

            long[] ids = TApi.getAllBlockIDs(api);

            msg.sendUpdatingFile();

            bot().execute(new SendDocument(msg.chatId(), StrUtil.utf8Bytes(ArrayUtil.join(ids, "\n"))).fileName(name + " - " + (System.currentTimeMillis() / 1000) + ".csv"));

        } catch (TwitterException e) {

            msg.send(NTT.parseTwitterException(e)).exec();

        }

    }

}

