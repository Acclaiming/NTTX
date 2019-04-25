package io.kurumi.ntt.funcs.twitter.ext;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.twitter.archive.*;
import io.kurumi.ntt.utils.*;
import java.util.*;
import twitter4j.*;

public class StatusGetter extends TwitterFunction {

    public static StatusGetter INSTANCE = new StatusGetter();
    
    @Override
    public void functions(LinkedList<String> names) {

        names.add("status");

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,TAuth account) {

        if (params.length != 1) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        Long statusId = NTT.parseStatusId(params[0]);

        if (statusId == -1L) {

            msg.send("用法 /status <推文链接|ID>").publicFailed();

            return;

        }

        Twitter api = account.createApi();

        try {

            StatusArchive newStatus = StatusArchive.save(api.showStatus(statusId));

            newStatus.loop(api);

            msg.send("已存档 :)").exec();

            msg.send(newStatus.toHtml()).html().exec();

        } catch (TwitterException e) {

            if (StatusArchive.contains(statusId)) {

                msg.send(StatusArchive.get(statusId).toHtml()).html().exec();

            } else {

                msg.send("推文/存档不存在 :(").publicFailed();

                return;

            }


        }



    }

}
