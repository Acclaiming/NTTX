package io.kurumi.ntt.td.request;

public abstract class TdAbsSend {

    public TdMain main;
    public long chatId;

    public TdAbsSend(TdMain main, long chatId) {
        this.main = main;
        this.chatId = chatId;
    }

}
