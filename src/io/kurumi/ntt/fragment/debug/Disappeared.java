package io.kurumi.ntt.fragment.debug;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.twitter.archive.UserArchive;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.utils.Html;

import java.util.*;

public class Disappeared extends Fragment {

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerAdminFunction("disappeared");

    }

    @Override
    public void onFunction(UserData user, Msg msg, String function, String[] params) {

        LinkedList<String> list = new LinkedList<>();

        for (UserArchive archive : UserArchive.data.findByField("isDisappeared", true)) {

            if (list.size() == 20) {

                msg.send(ArrayUtil.join(list.toArray(), "\n\n")).html().async();

                list.clear();

            }

            if (StrUtil.isBlank(archive.bio)) {

                list.add(Html.code(archive.name + " : @" + archive.screenName));

            } else {

                list.add(Html.code(archive.name + " : @" + archive.screenName + "\n\n简介 : " + archive.bio));

            }

        }

        if (!list.isEmpty()) msg.send(ArrayUtil.join(list.toArray(), "\n\n")).html().async();


    }

}
