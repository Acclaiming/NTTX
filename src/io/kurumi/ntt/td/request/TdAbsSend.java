package io.kurumi.ntt.td.request;

import io.kurumi.ntt.td.client.TdClient;

public abstract class TdAbsSend {

    public TdClient main;
    public long chatId;

    public TdAbsSend(TdClient main, long chatId) {
        this.main = main;
        this.chatId = chatId;
    }

}
