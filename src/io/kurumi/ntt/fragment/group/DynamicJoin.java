package io.kurumi.ntt.fragment.group;

import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;

public class DynamicJoin extends Fragment {

    final String PAYLOAD_JOIN = "join";

    @Override
    public void init(BotFragment origin) {

        super.init(origin);

        registerPayload(PAYLOAD_JOIN);

    }

    @Override
    public void onPayload(UserData user, Msg msg, String payload, String[] params) {
    }

}
