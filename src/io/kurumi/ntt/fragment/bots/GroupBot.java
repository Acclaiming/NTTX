package io.kurumi.ntt.fragment.bots;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.group.GroupActions;
import io.kurumi.ntt.fragment.group.GroupAdmin;
import io.kurumi.ntt.fragment.group.GroupFunction;
import io.kurumi.ntt.fragment.group.JoinCaptcha;
import io.kurumi.ntt.fragment.group.options.OptionsMain;
import io.kurumi.ntt.model.Msg;

public class GroupBot extends UserBotFragment {

    @Override
    public void reload() {

        super.reload();

        addFragment(new OptionsMain());
        addFragment(new GroupFunction());
        addFragment(new GroupAdmin());
        addFragment(new JoinCaptcha());
        addFragment(new GroupActions());

    }

    @Override
    public void onFinalMsg(UserData user, Msg msg) {
    }

}
