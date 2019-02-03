package io.kurumi.nttools.fragments;

import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.request.DeleteMessage;
import io.kurumi.nttools.model.Callback;
import io.kurumi.nttools.model.Msg;
import io.kurumi.nttools.model.request.AbstractSend;
import io.kurumi.nttools.twitter.TwiAccount;
import io.kurumi.nttools.utils.CData;
import io.kurumi.nttools.utils.UserData;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;

public abstract class FragmentBase {

    public boolean processPrivateMessage(UserData user, Msg msg) { return false; }
    public boolean processGroupMessage(UserData user, Msg msg) { return false; }
    public boolean processChannelPost(UserData user, Msg msg) { return false; }
    public boolean processCallbackQuery(UserData user, Callback callback) { return false; }
    public boolean processInlineQuery(UserData user, InlineQuery inlineQuery) { return false; }
    public boolean processChosenInlineQueryResult(UserData user, InlineQuery inlineQuery) { return false; }

    public CData cdata(String point) {

        CData data = new CData();

        data.setPoint(point);

        return data;

    }

    public CData cdata(String point, String index) {

        CData data = cdata(point);

        data.setindex(index);

        return data;

    }

    public CData cdata(String point, UserData userData, TwiAccount account) {

        CData data = cdata(point);

        data.setUser(userData, account);

        return data;

    }

    public CData cdata(String point, String index, UserData userData, TwiAccount account) {

        CData data = cdata(point, index);

        data.setUser(userData, account);

        return data;

    }
    
    public void deleteLastSend(UserData user,Msg msg,String key) {
        
        Integer lastMsgId = user.getByPath("last_msg_id." + key + "." + msg.fragment.name() + "." + user.id, Integer.class);

        if (lastMsgId != null) {

            msg.fragment.bot.execute(new DeleteMessage(user.id, lastMsgId));

        }
        
    }
    
    public void saveLastSent(UserData user,Msg msg,String key,BaseResponse resp) {

        if (resp instanceof SendResponse && resp.isOk()) {
        
            saveLastSent(user,msg,key,((SendResponse)resp).message().messageId());
       
        }
        
    }
    
    public void saveLastSent(UserData user,Msg msg,String key,int messageId) {
        
        user.putByPath("last_msg_id." + key + "." + msg.fragment.name() + "." + user,messageId);
        
    }

    public AbstractSend sendOrEdit(Msg msg, boolean edit, String... contnent) {

        if (!edit)return msg.send(contnent);
        else return msg.edit(contnent);

    }

}
