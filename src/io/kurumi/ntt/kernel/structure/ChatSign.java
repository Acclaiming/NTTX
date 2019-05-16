package io.kurumi.ntt.kernel.structure;

import org.telegram.bot.structure.Chat;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * @brief TODO
 * @date 16 of October of 2016
 */
public class ChatSign implements Chat {
	
    public Long id;
    public Long accessHash;
    public boolean isChannel;

    @Override
    public int getId() {
        return id.intValue();
    }

    @Override
    public Long getAccessHash() {
        return accessHash;
    }

    @Override
    public boolean isChannel() {
        return isChannel;
    }

}
