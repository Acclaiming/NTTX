package io.kurumi.ntt.model;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.UnbanChatMember;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.Send;

import java.util.Date;
import java.util.TimerTask;

public class Context {

    public Fragment fragment;
    public long targetChatId = -1;
    private Chat chat;

    public Context(Fragment fragment, Chat chat) {

        this.fragment = fragment;
        this.chat = chat;


    }

    public Chat chat() {
        return chat;
    }

    public Long chatId() {

        return targetChatId == -1 ? chat.id() : targetChatId;

    }

    public boolean isPrivate() {
        return chat.type() == Chat.Type.Private;
    }

    public boolean isGroup() {
        return chat.type() == Chat.Type.group || chat.type() == Chat.Type.supergroup;
    }

    public boolean isPublicGroup() {
        return chat.username() != null;
    }

    public boolean isSuperGroup() {
        return chat.type() == Chat.Type.supergroup;
    }

    public boolean isChannel() {
        return chat.type() == Chat.Type.channel;
    }

    public Send send(String... msg) {

        return new Send(fragment, chatId(), msg);

    }


}


