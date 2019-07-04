package io.kurumi.ntt.fragment.abs;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.request.KickChatMember;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.UnbanChatMember;
import com.pengrad.telegrambot.response.BaseResponse;
import io.kurumi.ntt.fragment.BotFragment;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.fragment.abs.request.Send;
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

    public boolean kick(Long userId) {

        return kick(userId, false);

    }

    public boolean kick(final Long userId, boolean ban) {

        BaseResponse resp = fragment.bot().execute(new KickChatMember(chatId(), userId.intValue()));

        if (!ban) {

			if (isPublicGroup()) {
				
				BotFragment.mainTimer.schedule(new TimerTask() {

						@Override
						public void run() {
						
							fragment.bot().execute(new UnbanChatMember(chatId(), userId.intValue()));
							
							
						}
						
					},new Date(System.currentTimeMillis() + 10 * 100));
				
			} else {
				
				fragment.bot().execute(new UnbanChatMember(chatId(), userId.intValue()));
				
			}
			
			
            
		}

        return resp.isOk();

    }

    public boolean unrestrict(long id) {

        return fragment.bot().execute(new RestrictChatMember(chatId(), (int) id)
                .canSendMessages(true)
                .canSendMediaMessages(true)
                .canSendOtherMessages(true)
                .canAddWebPagePreviews(true)
        ).isOk();

    }

    public boolean restrict(long id) {

        return fragment.bot().execute(new RestrictChatMember(chatId(), (int) id)
                .canSendMessages(false)
                .canSendMediaMessages(false)
                .canSendOtherMessages(false)
                .canAddWebPagePreviews(false)
        ).isOk();

    }

    public boolean restrict(long id, long until) {

        return fragment.bot().execute(new RestrictChatMember(chatId(), (int) id)
                .canSendMessages(false)
                .canSendMediaMessages(false)
                .canSendOtherMessages(false)
                .canAddWebPagePreviews(false)
                .untilDate((int) until)).isOk();

    }

    public Send send(String... msg) {

        return new Send(fragment, chatId(), msg);

    }


}


