package io.kurumi.ntt.funcs;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import java.util.*;

public class GroupRepeat extends Fragment {

    public static GroupRepeat INSTANCE = new GroupRepeat();

    public HashMap<Long,LinkedList<Msg>> msgs = new HashMap<>();
	
    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        if (msg.isCommand()) {}

        return false;

    }

}
