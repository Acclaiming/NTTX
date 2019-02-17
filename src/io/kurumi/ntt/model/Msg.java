package io.kurumi.ntt.model;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.GetFile;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.Edit;
import io.kurumi.ntt.model.request.Send;
import java.io.File;
import io.kurumi.ntt.BotConf;

public class Msg extends Context {

    private Message message;

    public Msg(Fragment fragment, Message message) {
        
        super(fragment,message.chat());
        
        this.fragment = fragment;
        this.message = message;
        
    }
    
    public Message message() { return message; }

    public int messageId() { return message.messageId(); }

    public boolean hasText() {

        return message.text() != null;

    }
   
    public Document doc() {
        
        return message.document();
        
    }

    public String text() {

        return message.text();

    }
    
    public Send reply(String... msg)  {

        return send(msg).replyTo(this);

    }
    
    public Edit edit()  {

        return new Edit(fragment,chatId(),messageId(),text());

    }
    
    public Edit edit(String... msg)  {
        
        return new Edit(fragment,chatId(),messageId(),msg);

    }
    
    public void delete() {
        
        fragment.bot().execute(new DeleteMessage(chatId(),messageId()));
        
    }
    
    public File file() {
        
        Document doc = message.document();
        
        if (doc == null) return null;

        File local = new File(BotConf.CACHE_DIR,"files/" + doc.fileId());

        if (local.isFile()) return local;

        String path = fragment.bot().getFullFilePath(fragment.bot().execute(new GetFile(doc.fileId())).file());

        HttpUtil.downloadFile(path,local);

        return local;

    }
    
    
    public boolean isCommand() {

        if (text() == null) return false;

        return text().startsWith("/");

    }
    
    private String name;

    public String commandName() {

        if (name != null) return name;
        
        if (text() == null) return null;

        if (!text().contains("/")) return null;

        String body = StrUtil.subAfter(text(), "/", false);

        if (body.contains(" ")) {

            String cmdAndUser =  StrUtil.subBefore(body, " ", false);

            if (cmdAndUser.contains("@")) {

                name = StrUtil.subBefore(cmdAndUser, "@", false);

            } else {

                name = cmdAndUser;

            }

        } else if (body.contains("@")) {

            name = StrUtil.subBefore(body, "@", false);

        } else {

            name = body;

        }
        
        return name;

    }
    
    public static String[] NO_PARAMS = new String[0];

    private String[] params;
    
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
