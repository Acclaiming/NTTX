package io.kurumi.ntt.kernel.structure;

import org.telegram.bot.structure.IUser;

/**
 * @author Ruben Bermudez
 * @version 1.0
 * @brief TODO
 * @date 16 of October of 2016
 */
public class UserSign implements IUser {
	
    public Long id; ///< ID of the user (provided by Telegram server)
    public Long hash; ///< Hash of the user (provide by Telegram server)

    @Override
    public int getUserId() {
        return id.intValue();
    }

    @Override
    public Long getUserHash() {
        return hash;
    }
	
    @Override
    public String toString() {
        return "" + this.id;
    }
}
