package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;

public class PermUI extends Fragment {

    @Override
    public boolean onPrivMsg(UserData user, Msg msg) {

        if (!"perm".equals(msg.commandName())) return false;

        return true;

    }

    public void register(UserData user, Msg msg) {


    }

}
