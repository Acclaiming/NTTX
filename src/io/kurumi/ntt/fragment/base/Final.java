package io.kurumi.ntt.fragment.base;

import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;

import io.kurumi.ntt.db.StickerSet;
import com.pengrad.telegrambot.model.request.*;



public class Final extends Fragment {

	final String split = "------------------------\n";
	
	@Override
	public boolean onPrivate(UserData user,Msg msg) {
		
		StringBuilder str = new StringBuilder();
		
		Message message = msg.message();
		
		str.append("消息ID : " + message.messageId()).append("\n");
		
		if (message.forwardFrom() != null) {
		
			str.append("来自用户 : ").append(UserData.get(message.forwardFrom()).userName()).append("\n");
			str.append("用户ID : ").append(message.forwardFrom().id()).append("\n");
			
		}
		
		if (message.forwardFromChat() != null) {
			
			if (message.forwardFromChat().type() == Chat.Type.channel) {
				
				str.append("来自频道 : ").append(message.forwardFromChat().username() == null ? message.forwardFromChat().title() : Html.a(message.forwardFromChat().username(),"https://t.me/" + message.forwardFromChat().username())).append("\n");
				
				str.append("频道ID : ").append(message.forwardFromChat().id());
				
				if (message.forwardSenderName() != null) {
					
					str.append("签名用户 : ").append(message.forwardSenderName());
					
				}
				
			} else if (message.forwardFromChat().type() == Chat.Type.group || message.forwardFromChat().type() == Chat.Type.supergroup) {
				
				str.append("来自群组 : ").append(message.forwardFromChat().username() == null ? message.forwardFromChat().title() : Html.a(message.forwardFromChat().username(),"https://t.me/" + message.forwardFromChat().username())).append("\n");
				
				} else {
				
				if (message.forwardFrom() == null) {
					
					str.append("来自 : ").append(message.forwardSenderName()).append(" (隐藏来源)\n");

				}
				
			}
			
			str.append("消息链接 : https://t.me/c/").append(message.forwardFromChat().id()).append("/").append(message.forwardFromMessageId()).append("\n");
			
		}
		
		if (message.sticker() != null) {
			
			str.append(split);
			
			str.append("贴纸ID : ").append(message.sticker().fileId()).append("\n");
			
			str.append("贴纸表情 : ").append(message.sticker().emoji()).append("\n");
			
			if (message.sticker().setName() != null) {
				
				str.append("贴纸包 : ").append("https://t.me/addstickers/" + message.sticker().setName()).append("\n");
				
			}
			
			msg.sendUpdatingPhoto();

            bot().execute(new SendPhoto(msg.chatId(),getFile(msg.message().sticker().fileId())).caption(str.toString()).parseMode(ParseMode.HTML).replyToMessageId(msg.messageId()));

		} else {
			
			msg.send("这一条消息未被处理 将忽略 :(",str.toString()).replyTo(msg).html().exec();
			
		}
		
		return true;
		
	}

}
