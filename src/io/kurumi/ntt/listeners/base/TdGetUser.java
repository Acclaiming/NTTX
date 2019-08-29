package io.kurumi.ntt.listeners.base;

import io.kurumi.ntt.td.client.TdFunction;
import io.kurumi.ntt.td.TdApi.*;
import io.kurumi.ntt.td.model.TMsg;
import cn.hutool.core.util.NumberUtil;

public class TdGetUser extends TdFunction {

	@Override
	public String functionName() {

		return "get_user";

	}

	@Override
	public void onFunction(User user,TMsg msg,String function,String[] params) {

		if (msg.isChannel()) return;
		
		if ((params.length == 0 || !msg.isText()) && msg.replyTo == 0L) {

			replyText(msg,getLocale(user).GET_USER);

			return;

		}
		
		if (msg.replyTo != 0L) {
			
			Message replyTo = E(new GetMessage(msg.chatId,msg.replyTo));
		
			if (replyTo == null) {
				
				replyText(msg,getLocale(user).GET_USER_NOT_FOUND);
				
				return;
				
			}
			
			int userId = replyTo.senderUserId;
			
			User target = E(new GetUser(userId));

			if (target == null) {

				replyText(msg,mention(userName(target),userId));

			} else {

				replyText(msg,getLocale(user).GET_USER_NOT_FOUND);
				
			}
			
			return;
			
		}

		MessageText content = (MessageText) msg.content;

		for (TextEntity entity : content.text.entities) {

			if (entity.type instanceof TextEntityTypeMentionName) {

				TextEntityTypeMentionName mention = (TextEntityTypeMentionName) entity.type;

				User target = E(new GetUser(mention.userId));

				if (target == null) {

					replyText(msg,mention(userName(target),mention.userId));

				} else {

					replyText(msg,mention(msg.text().substring(entity.offset,entity.offset + entity.length),mention.userId));

				}

				return;

			}

		}
		
		if (NumberUtil.isInteger(msg.param())) {
			
			int userId = NumberUtil.parseInt(msg.param());
			
			User target = E(new GetUser(userId));

			if (target == null) {

				replyText(msg,mention(userName(target),userId));

			} else {

				replyText(msg,mention("TARGET",userId));
				
			}
			
			return;
			
		}
		
		// BOT METHOD INVALID

		//if (msg.param().trim().contains(" ")) {
			
		replyText(msg,getLocale(user).GET_USER);

			/*
			
			return;
			
		}
		
		
		
		String userName = msg.param().trim();
		
		if (userName.startsWith("@")) {
			
			userName = userName.substring(1);
			
		}
		
		Chat chat = E(new SearchPublicChat(userName));

		if (chat == null || !(chat.type instanceof ChatTypePrivate)) {
			
			sendText(msg,getLocale(user).GET_USER_NOT_FOUND);
			
			return;
			
		}
		
		User target = E(new GetUser((int)chat.id));

		if (target == null) {

			sendText(msg,mention(userName(target),target.id));

		} else {

			sendText(msg,getLocale(user).GET_USER_NOT_FOUND);

		}
		
		*/
		
	}

}
