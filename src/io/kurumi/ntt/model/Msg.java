package io.kurumi.ntt.model;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.GetFile;
import io.kurumi.ntt.Env;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.Edit;
import io.kurumi.ntt.model.request.Send;

import java.io.File;
import cn.hutool.extra.qrcode.*;

public class Msg extends Context {

    public static String[] NO_PARAMS = new String[0];
    private Message message;
    private String name;
    private String[] params;

    public Msg(Fragment fragment, Message message) {

        super(fragment, message.chat());

        this.fragment = fragment;
        this.message = message;

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
	
	
	public Msg replyTo() {
		
		return new Msg(fragment,message.replyToMessage());
		
	}

    public Send reply(String... msg) {

        return send(msg).replyTo(this);

    }

    public Edit edit(String... msg) {

        return new Edit(fragment, chatId(), messageId(), msg);

    }

    public void delete() {

        fragment.bot().execute(new DeleteMessage(chatId(), messageId()));

    }
	
	public int photoSize() {
		
		if (message.photo() != null) {
		
		return message.photo().length;
		
		}
		
		return 0;
		
		
	}
	
	public File phpto(int index) {

        if (photoSize() <= index) return null;

        File local = new File(Env.CACHE_DIR, "files/" + message.photo()[index].fileId());

        if (local.isFile()) return local;

        String path = fragment.bot().getFullFilePath(fragment.bot().execute(new GetFile(message.photo()[index].fileId())).file());

        HttpUtil.downloadFile(path, local);

        return local;
		
		

    }
	

    public File file() {

        Document doc = message.document();

        if (doc == null) return null;

        File local = new File(Env.CACHE_DIR, "files/" + doc.fileId());

        if (local.isFile()) return local;

        String path = fragment.bot().getFullFilePath(fragment.bot().execute(new GetFile(doc.fileId())).file());

        HttpUtil.downloadFile(path, local);

        return local;

    }

    public boolean isCommand() {

        if (text() == null) return false;

        return text().startsWith("/");

    }

    public String commandName() {

        if (name != null) return name;

        if (text() == null) return null;

        if (!text().contains("/")) return null;

        String body = StrUtil.subAfter(text(), "/", false);

        if (body.contains(" ")) {

            String cmdAndUser = StrUtil.subBefore(body, " ", false);

            if (cmdAndUser.contains("@" + fragment.origin.me.username())) {
				
                name = StrUtil.subBefore(cmdAndUser, "@", false);

            } else {

                name = cmdAndUser;

            }

        } else if (body.contains("@" + fragment.origin.me.username())) {

            name = StrUtil.subBefore(body, "@", false);

        } else {

            name = body;

        }

        return name;

    }

    public String[] commandParms() {

        if (params != null) return params;

        if (text() == null) return NO_PARAMS;

        if (!text().contains("/")) return NO_PARAMS;

        String body = StrUtil.subAfter(text(), "/", false);

        if (body.contains(" ")) {

            params = StrUtil.subAfter(body, " ", false).split(" ");

        } else {

            params = NO_PARAMS;

        }

        return params;

    }


}
