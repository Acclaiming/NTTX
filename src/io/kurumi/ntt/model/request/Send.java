package io.kurumi.ntt.model.request;

import cn.hutool.core.util.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.fragment.twitter.status.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;

public class Send extends AbstractSend<Send> {

    private SendMessage request;

    public Send(Fragment fragment,String chatId,String... msg) {

        this(null,fragment,chatId,msg);

    }

    public Send(Fragment fragment,long chatId,String... msg) {

        this(null,fragment,chatId,msg);

    }

    public Send(String chatId,String... msg) {

        this(null,Launcher.INSTANCE,chatId,msg);

    }

    public Send(long chatId,String... msg) {

        this(null,Launcher.INSTANCE,chatId,msg);

    }


    private Send(Void v,Fragment fragment,Object chatId,String... msg) {

        super(fragment);

        request = new SendMessage(chatId,ArrayUtil.join(msg,"\n"));

        this.fragment = fragment;

        request.disableWebPagePreview(true);

    }

    @Override
    public Send enableLinkPreview() {

        request.disableWebPagePreview(false);

        return this;

    }


    public Send disableNotification() {

        request.disableNotification(true);

        return this;

    }

    public Send replyToMessageId(int replyToMessageId) {

        request.replyToMessageId(replyToMessageId);

        return this;

    }

    public Send replyTo(Msg msg) {

		if (msg == null) return this;

        replyToMessageId(msg.messageId());

        return this;

    }

    @Override
    public Send markdown() {

        request.parseMode(ParseMode.Markdown);

        return this;

    }

    @Override
    public Send html() {

        request.parseMode(ParseMode.HTML);

        return this;

    }

    public Send hideKeyboard() {

        request.replyMarkup(new ReplyKeyboardHide());

        return this;

    }

    public Send removeKeyboard() {

        request.replyMarkup(new ReplyKeyboardHide());

        return this;

    }

    public Send keyboard(Keyboard keyboard) {

        request.replyMarkup(keyboard.markup());

        return this;

    }

    @Override
    public Send buttons(ButtonMarkup markup) {

        request.replyMarkup(markup.markup());

        return this;

    }

    public void publicFailed() {

        if (origin.isPrivate()) {

            exec();

        } else {

            failedWith();

        }

    }

    public void failed() {

        failed(5000);

    }

    public void failed(final long delay) {

        if (origin == null) return;

        SendResponse resp = exec();

        if (resp.isOk()) {

            NTT.tryDelete(delay,new Msg(fragment,resp.message()));

        }

    }

	public void failedWith() {

        failedWith(5000);

    }

    public void failedWith(final long delay) {

        if (origin == null) return;

        SendResponse resp = exec();

        if (resp.isOk()) {

            NTT.tryDelete(delay,origin,new Msg(fragment,resp.message()));

        }




    }

    public void cancel() {

        cancel(5000);

    }

    public void cancel(final long delay) {

        if (origin == null) return;

        SendResponse resp = exec();

        if (resp.isOk()) {

            NTT.tryDelete(delay,new Msg(fragment,resp.message()));

        }

    }
    
    public Msg send() {

        SendResponse resp = exec();

        if (resp == null || !resp.isOk()) return null;

        return new Msg(fragment,resp.message());

    }
	
	public SendResponse point(int type,long targetId) {
		
		SendResponse resp = exec();
		
		if (resp.isOk() || resp.message().chat().type() == Chat.Type.Private) {
			
			MessagePoint.set(resp.message().messageId(),type,targetId);
			
		}
		
		return resp;

	}

    @Override
    public SendResponse exec() {

        char[] arr = request.getText().toCharArray();

        while (arr.length > 4096) {

            Character[] chars = (Character[])ArrayUtil.sub(ArrayUtil.wrap((Object)arr),0,4096);

            int index = chars.length;

            boolean sdd = false;

            for (Character c : ArrayUtil.reverse(chars)) {

                index --;

                if (c == '\n') {

                    char[] send = new char[index];

                    ArrayUtil.copy(arr,send,index);

                    fork(String.valueOf(send)).exec();

                    char[] subed = new char[arr.length - index - 1]; 

                    ArrayUtil.copy(arr,index,subed,0,subed.length);

                    request.setText(String.valueOf(subed));

                    sdd = true;

                    break;

                }

            }

            if (!sdd) {

                // 没有换行的情况

                char[] send = new char[4096];

                ArrayUtil.copy(arr,send,4096);

                fork(String.valueOf(send)).exec();

                char[] subed = new char[arr.length - 4096];

                ArrayUtil.copy(arr,4095,subed,0,subed.length);

                request.setText(String.valueOf(subed));

            }

            arr = request.getText().toCharArray();

        }

        try {

            SendResponse resp = fragment.bot().execute(request);

            if (!resp.isOk()) {
				
				if (resp.errorCode() == 403 && !(request.chatId instanceof String) && ((long)request.chatId > 0)) {
					
					UserData user = UserData.get((long)request.chatId);

					if (user != null) {
						
						user.contactable = false;
						
						UserData.userDataIndex.put(user.id,user);
						
						UserData.data.setById(user.id,user);
						
					}
					
				}

                BotLog.infoWithStack("消息发送失败 " + resp.errorCode() + " : " + resp.description());

            }

            return resp;


        } catch (Exception ex) {

            return null;

        }


    }

    public Send fork(String... msg) {

        Send send = new Send(null,fragment,request.getChatId(),msg);

        if (request.mode != null) {

            send.request.parseMode(request.mode);
        }

        send.request.disableWebPagePreview(request.disablePreview);

        return send;

    }

}
