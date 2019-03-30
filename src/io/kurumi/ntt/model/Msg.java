package io.kurumi.ntt.model;

import cn.hutool.core.util.*;
import cn.hutool.http.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.request.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.fragment.*;
import io.kurumi.ntt.model.request.*;
import java.io.*;

import java.io.File;

public class Msg extends Context {

    public static String[] NO_PARAMS = new String[0];
    private Message message;
    private String name;
    private String[] params;

    public Msg(Message message) {
        
        this(Launcher.INSTANCE,message);
        
    }
    
    public Msg(Fragment fragment,Message message) {

        super(fragment,message.chat());

        this.fragment = fragment;
        this.message = message;

    }
	
	public UserData from() {
		
		UserData user = UserData.INSTANCE.getOrNew((long)message.from().id());

		user.refresh(message.from());
		
		return user;
		
	}

    public Message message() {
        return message;
    }

    public int messageId() {
        return message.messageId();
    }


    public boolean hasText() {

        return message.text() != null;

    }

    public Document doc() {

        return message.document();

    }

    public String text() {

        return message.text();

    }

	public boolean isReply() {

		return message.replyToMessage() != null;

	}

    @Override
    public Send send(String... msg) {
        
        if (msg.length > 0 && !isPrivate() && message.from() != null) {
        
            ArrayUtil.setOrAppend(msg,0,from().userName() + " " + ArrayUtil.get(msg,0));
            
        }
            
        return super.send(msg);
    }


	public Msg replyTo() {

		return new Msg(fragment,message.replyToMessage());

	}

    public Send reply(String... msg) {

        return send(msg).replyTo(this);

    }

    public Edit edit(String... msg) {

        return new Edit(fragment,chatId(),messageId(),msg);

    }

    public boolean delete() {

        return fragment.bot().execute(new DeleteMessage(chatId(),messageId())).isOk();

    }
    
    public boolean unrestrict() {

        return restrict(true,true,true,true);

    }

    public boolean restrict() {

        return restrict(false,false,false,false);

    }

    public boolean restrict(boolean canSendMessage,boolean canSendMediaMessages,boolean canSendOtherMessages,boolean canAddWebViewPagePreviews) {

        return restrict(from().id.intValue(),canSendMessage,canSendMediaMessages,canSendOtherMessages,canAddWebViewPagePreviews);

    }
    
	
	public boolean unrestrict(int id) {
		
		return restrict(id,true,true,true,true);
		
	}
	
	public boolean restrict(int id) {
		
		 return restrict(id,false,false,false,false);
		
	}
	
	public boolean restrict(int id,boolean canSendMessage,boolean canSendMediaMessages,boolean canSendOtherMessages,boolean canAddWebViewPagePreviews) {

		return fragment.bot().execute(new RestrictChatMember(chatId(),id).canSendMessages(canSendMessage).canSendMediaMessages(canSendMediaMessages).canSendOtherMessages(canSendOtherMessages).canAddWebPagePreviews(canAddWebViewPagePreviews)).isOk();

	}
	
	public void kick() {
		
		fragment.bot().execute(new KickChatMember(chatId(),from().id.intValue()));
		
	}
	
	public Msg forwardTo(Object chatId) {
		
		return new Msg(fragment,fragment.bot().execute(new ForwardMessage(chatId,chatId(),messageId())).message());
		
	}

	public int photoSize() {
		
		if (message.photo() != null) {

			return message.photo().length;

		}

		return 0;


	}

	public File photo(int index) {

        if (photoSize() <= index) return null;

        File local = new File(Env.CACHE_DIR,"files/" + message.photo()[index].fileId());

        if (local.isFile()) return local;

        String path = fragment.bot().getFullFilePath(fragment.bot().execute(new GetFile(message.photo()[index].fileId())).file());

        HttpUtil.downloadFile(path,local);

        return local;



    }


    public File file() {

        Document doc = message.document();

        if (doc == null) return null;

        File local = new File(Env.CACHE_DIR,"files/" + doc.fileId());

        if (local.isFile()) return local;

        String path = fragment.bot().getFullFilePath(fragment.bot().execute(new GetFile(doc.fileId())).file());

        HttpUtil.downloadFile(path,local);

        return local;

    }
    
    int isCommand = 0;

    public boolean isCommand() {

        isCommand = isCommand == 0 ? (text() != null && text().startsWith("/") && text().length() > 1) ? 1 : 2 : isCommand;
        
        return isCommand == 1;

    }

    public String command() {

        if (!isCommand()) return null;
        
        if (name != null) return name;

        if (text() == null) return null;

        if (!text().contains("/")) return null;

        String body = text().substring(1);

        if (body.contains(" ")) {

            String cmdAndUser = StrUtil.subBefore(body," ",false);

            if (cmdAndUser.contains("@" + fragment.origin.me.username())) {

                name = StrUtil.subBefore(cmdAndUser,"@",false);

            } else {

                name = cmdAndUser;

            }

        } else if (body.contains("@" + fragment.origin.me.username())) {

            name = StrUtil.subBefore(body,"@",false);

        } else {

            name = body;

        }

        return name;

    }
    
    boolean noParams = false;

    public String[] params() {

        if (params != null) return params;

        if (noParams) {
          
            return NO_PARAMS;
            
        }
        
        if (!isCommand()) {
            
            noParams = true;
            
            return NO_PARAMS;
            
        }

        String body = StrUtil.subAfter(text(),"/",false);

        if (body.contains(" ")) {

            params = StrUtil.subAfter(body," ",false).split(" ");

        } else {

            noParams = true;
           
            params = NO_PARAMS;

        }

        return params;

    }


}
