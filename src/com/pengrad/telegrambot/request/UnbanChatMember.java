package com.pengrad.telegrambot.request;

/**
 * stas
 * 5/2/16.
 */
public class UnbanChatMember extends KickChatMember {

    public UnbanChatMember(Long chatId, int userId) {
        super(chatId, userId);
    }
}
