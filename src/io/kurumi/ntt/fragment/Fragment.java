package io.kurumi.ntt.fragment;

import cn.hutool.http.*;
import com.pengrad.telegrambot.*;
import com.pengrad.telegrambot.model.*;
import com.pengrad.telegrambot.model.request.*;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.*;
import io.kurumi.ntt.*;
import io.kurumi.ntt.db.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.utils.*;
import java.io.*;

import io.kurumi.ntt.model.Callback;
import java.io.File;

public class Fragment {

    public BotFragment origin;

    public TelegramBot bot() {

        return origin.bot();

    }

    public PointStore point() {

        return origin.point();

    }

    public <T> void setPoint(UserData user,String pointTo,PointStore.Type context,T content) {

        point().set(user,context,pointTo,content);

    }

    public <T> void setPoint(UserData user,String pointTo,T content) {

        point().set(user,pointTo,content);

    }

    public void setPoint(UserData user,String pointTo) {

        point().set(user,pointTo,null);

    }

    public <T> PointStore.Point<T> clearPoint(UserData user) {

        return point().clear(user);

    }

    public <T> PointStore.Point<T> getPoint(UserData user) {

        return point().get(user);

    }

    public boolean onUpdate(UserData user,Update update) {

        return false;

    }

    public boolean onMsg(UserData user,Msg msg) {

        return false;

    }

    public boolean onPointedMsg(UserData user,Msg msg) {

        return false;

    }

    public boolean onPrivate(UserData user,Msg msg) {

        return false;

    }

    public boolean onPointedPrivate(UserData user,Msg msg) {

        return false;

    }

    public boolean onGroup(UserData user,Msg msg) {

        return false;

    }

    public boolean onPointedGroup(UserData user,Msg msg) {

        return false;

    }

    public boolean onChanPost(UserData user,Msg msg) {

        return false;

    }

    public boolean onCallback(UserData user,Callback callback) {

        return false;

    }

    public boolean onQuery(UserData user,Query inlineQuery) {
        return false;
    }

    public File getFile(String fileId) {

        File local = new File(Env.CACHE_DIR,"files/" + fileId);

        if (local.isFile()) return local;

        GetFileResponse file = bot().execute(new GetFile(fileId));

        if (!file.isOk()) {
            
            BotLog.debug("取文件失败 : " + file.errorCode() + " " + file.description());
            
            return null;
            
            }

        String path = bot().getFullFilePath(file.file());

        HttpUtil.downloadFile(path,local);

        return local;

    }

    public Msg sendSticker(long chatId,StickerPoint sticker) {

        return Msg.from(this,bot().execute(new SendSticker(chatId,sticker.fileId)));

    }
    public Msg sendSticker(long chatId,String sticker) {

        return Msg.from(this,bot().execute(new SendSticker(chatId,sticker)));

    }


    public Msg sendFile(long chatId,String file) {

        return Msg.from(this,this.bot().execute(new SendDocument(chatId,file)));

    }

    public Msg sendFile(long chatId,File file) {

        return Msg.from(this,bot().execute(new SendDocument(chatId,file)));

    }

    public Msg sendFile(long chatId,byte[] file) {

        return Msg.from(this,bot().execute(new SendDocument(chatId,file)));

    }

    public void sendTyping(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.typing));

    }

    public void sendUpdatingFile(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.upload_document));

    }

    public void sendUpdatingPhoto(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.upload_photo));

    }

    public void sendUpdatingAudio(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.upload_audio));

    }

    public void sendUpdatingVideo(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.upload_video));

    }

    public void sendUpdatingVideoNote(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.upload_video_note));

    }

    public void sendFindingLocation(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.find_location));

    }

    public void sendRecordingAudio(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.record_audio));

    }


    public void sendRecordingViedo(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.record_video));

    }

    public void sendRecordingVideoNote(long chatId) {

        bot().execute(new SendChatAction(chatId,ChatAction.record_video_note));

    }


}
