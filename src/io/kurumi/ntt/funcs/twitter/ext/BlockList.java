package io.kurumi.ntt.funcs.twitter.ext;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import io.kurumi.ntt.twitter.*;
import twitter4j.*;
import com.pengrad.telegrambot.request.*;
import cn.hutool.core.util.*;
import io.kurumi.ntt.funcs.abs.*;
import java.util.*;

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
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        try {

            Twitter api = account.createApi();

            String name = "@" + api.verifyCredentials().getScreenName();

            long[] ids = TApi.getAllBlockIDs(api);

            msg.sendUpdatingFile();

            bot().execute(new SendDocument(msg.chatId(),StrUtil.utf8Bytes(ArrayUtil.join(ids,"\n"))).fileName(name + " - " + (System.currentTimeMillis() / 1000) + ".csv"));

        } catch (TwitterException e) {

            msg.send("拉取失败 (" + e.getErrorCode() + ")... 你的认证可能失效或者到达了API调用上限。").exec();

        }

    }

}

