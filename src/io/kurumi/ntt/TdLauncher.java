package io.kurumi.ntt;

import io.kurumi.ntt.listeners.base.TdGetIdFunction;
import io.kurumi.ntt.listeners.base.TdGetUser;
import io.kurumi.ntt.listeners.base.TdPingFunction;
import io.kurumi.ntt.listeners.extra.TdDnsLookup;
import io.kurumi.ntt.listeners.group.CleanAccounts;
import io.kurumi.ntt.td.client.TdBot;

public class TdLauncher extends TdBot {

    public TdLauncher(String botToken) {

        super(botToken);

    }

    @Override
    public void init() {

        addListener(new TdPingFunction());
        addListener(new TdGetIdFunction());

        addListener(new TdDnsLookup());

        addListener(new CleanAccounts());

        addListener(new TdGetUser());
		
    }

}
