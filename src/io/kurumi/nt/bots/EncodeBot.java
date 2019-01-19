package io.kurumi.nt.bots;

import twitter4j.*;

public class EncodeBot extends StatusListenerBot {

    @Override
    public String getBotName() {
        return "EncodeBot";
    }

    @Override
    public String getConfigKey() {
        return "bot_encode";
    }

    @Override
    public void onStatus(Twitter api, Status status) throws TwitterException {
        
        
        
    }

}
