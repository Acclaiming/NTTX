package io.kurumi.ntt.td.client;

import io.kurumi.ntt.td.TdApi.User;
import io.kurumi.ntt.td.model.TMsg;

public abstract class TdFunction extends TdListener {

    public abstract String functionName();

    @Override
    public void init() {
        registerFunction(functionName());
    }

    @Override
    public abstract void onFunction(User user, TMsg msg, String function, String[] params);

}
