package io.kurumi.ntt.model;

import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.request.*;

public class Context {

    public Fragment fragment;
    private Chat chat;

    public Context(Fragment fragment, Chat chat) {

        this.fragment = fragment;
        this.chat = chat;
		
		

    }

    public Chat chat() {
        return chat;
    }

    public Long chatId() {
        return chat.id();
    }

    public boolean isPrivate() {
        return chat.type() == Chat.Type.Private;
    }

    public boolean isGroup() {
        return chat.type() == Chat.Type.group || chat.type() == Chat.Type.supergroup;
    }

    public boolean isSuperGroup() {
        return chat.type() == Chat.Type.supergroup;
    }

    public boolean isChannel() {
        return chat.type() == Chat.Type.channel;
    }
    
    public void kick(Long userId) {

        fragment.bot().execute(new KickChatMember(chatId(),userId.intValue()));
		fragment.bot().execute(new UnbanChatMember(chatId(),userId.intValue()));

	
	}
    
    public boolean unrestrict(long id) {

        return fragment.bot().execute(new RestrictChatMember(chatId(),(int)id)
                                      .canSendMessages(true)
                                      .canSendMediaMessages(true)
                                      .canSendOtherMessages(true)
                                      .canAddWebPagePreviews(true)
                                      ).isOk();

    }
    
    
    public boolean restrict(long id) {

        return fragment.bot().execute(new RestrictChatMember(chatId(),(int)id)
                                      .canSendMessages(false)
                                      .canSendMediaMessages(false)
                                      .canSendOtherMessages(false)
                                      .canAddWebPagePreviews(false)
                                      ).isOk();

    }



    public boolean restrict(long id,long until) {

        return fragment.bot().execute(new RestrictChatMember(chatId(),(int)id)
                                      .canSendMessages(false)
                                      .canSendMediaMessages(false)
                                      .canSendOtherMessages(false)
                                      .canAddWebPagePreviews(false)
                                      .untilDate((int)until)).isOk();

    }
	
	
    public Send send(String... msg) {

        return new Send(fragment, chatId(), msg);

    }

    
}


