package io.kurumi.ntt;

import io.kurumi.ntt.listeners.base.TdPingFunction;
import io.kurumi.ntt.td.client.TdBot;

public class TdLauncher extends TdBot {

    public TdLauncher(String botToken) {

        super(botToken);

    }

    @Override
    public void init() {

        addListener(new TdPingFunction());

    }

}
