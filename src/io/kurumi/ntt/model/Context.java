package io.kurumi.ntt.model;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendDocument;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.request.Send;
import java.io.File;
import io.kurumi.ntt.db.StickerPoint;
import com.pengrad.telegrambot.request.SendSticker;

public class Context {
    
    public Fragment fragment;
    private Chat chat;

    public Context(Fragment fragment, Chat chat) {
        
        this.fragment = fragment;
        this.chat = chat;
        
    }
    
    public Chat chat() { return chat; }

    public long chatId() { return chat.id(); }

    public boolean isPrivate() { return chat.type() == Chat.Type.Private; }

    public boolean isGroup() { return chat.type() == Chat.Type.group || chat.type() == Chat.Type.supergroup; }

    public boolean isSuperGroup() { return chat.type() == Chat.Type.supergroup; }
    
    public boolean isChannel() { return chat.type() == Chat.Type.channel; }
    
    public Send send(String... msg)  {

        return new Send(fragment,chatId(),msg);

    }
    
    public Msg sendSticker(StickerPoint sticker) {
        
        return new Msg(fragment,fragment.bot.execute(new SendSticker(chatId(),sticker.fileId)).message());
        
    }
    
    public Msg sendFile(String file) {
        
        return new Msg(fragment,fragment.bot.execute(new SendDocument(chatId(), file)).message());

    }
    
    public Msg sendFile(File file) {

        return new Msg(fragment,fragment.bot.execute(new SendDocument(chatId(), file)).message());

    }
    
    public Msg sendFile(byte[] file) {

        return new Msg(fragment,fragment.bot.execute(new SendDocument(chatId(), file)).message());

    }

    public void sendTyping() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.typing));

    }


    public void sendUpdatingFile() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.upload_document));

    }

    public void sendUpdatingPhoto() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.upload_photo));

    }

    public void sendUpdatingAudio() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.upload_audio));

    }

    public void sendUpdatingVideo() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.upload_video));

    }

    public void sendUpdatingVideoNote() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.upload_video_note));

    }

    public void sendFindingLocation() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.find_location));

    }

    public void sendRecordingAudio() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.record_audio));

    }


    public void sendRecordingViedo() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.record_video));

    }

    public void sendRecordingVideoNote() {

        fragment.bot.execute(new SendChatAction(chatId(),ChatAction.record_video_note));

    }
    
}


