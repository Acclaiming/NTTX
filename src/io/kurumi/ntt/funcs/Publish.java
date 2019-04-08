package io.kurumi.ntt.funcs;

import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.model.Msg;

public class Publish extends Fragment {

    @Override
    public boolean onMsg(UserData user,Msg msg) {
        return false;
    }
    
}
