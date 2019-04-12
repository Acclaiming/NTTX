package io.kurumi.ntt.funcs;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.db.SData;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.db.UserData;
import com.pengrad.telegrambot.model.User;
import io.kurumi.ntt.twitter.TAuth;
import com.pengrad.telegrambot.request.GetChatAdministrators;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.model.ChatMember;
import io.kurumi.ntt.Launcher;
import cn.hutool.json.JSONArray;

public class GroupProtecter extends Fragment {
    
    public static JSONObject conf = SData.getJSON("data","group_protect",true);
    
    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {
        
        if (msg.isCommand()) {
            
            switch (msg.command()) {
                
                case "gp_enable" : enable(user,msg);break;
                case "gp_disable" : disable(user,msg);break;
                default : return false;
                
            }
            
            return true;
            
        } else if (msg.message().newChatMembers() != null) {
           
            for (User newer : msg.message().newChatMembers()) {
                
                UserData newUser = BotDB.getUserData(newer);

                newUser(user,msg,newUser);
                
            }
            
        }
        
        return false;
        
    }

    void newUser(UserData user,Msg msg,UserData newUser) {
       
        msg.send(newUser.userName()).html();
        
    }
    
    void save() {
        
        SData.setJSON("data","group_protect",conf);
        
    }
    
    void enable(UserData user,Msg msg) {
        
        GetChatMemberResponse resp = bot().execute(new GetChatMember(msg.chatId(),user.id.intValue()));

        if (!resp.isOk() || ((resp.chatMember().status() != (ChatMember.Status.administrator) && resp.chatMember().status() != ChatMember.Status.creator))) {
            
            msg.reply("你不是群组管理 :)").publicFailed();
            
            return;
            
        }
        
        ChatMember me = bot().execute(new GetChatMember(msg.chatId(),origin.me.id())).chatMember();
        
        if (!me.canRestrictMembers()) {
            
            msg.reply("失败 BOT无法移除群员。").publicFailed();
            
            return;
            
        }
        
        JSONArray enable = conf.getJSONArray("enable");
       
        if (enable != null && enable.contains(msg.chatId())) {
            
            msg.reply("无需重复开启 :)").publicFailed();
            
            return;
            
        }
        
        if (enable == null) enable = new JSONArray();
        
        enable.add(msg.chatId());
        
        conf.put("enable",enable);
        
        save();

        msg.reply("开启成功 新群员必须人工通过或认证账号 :)").exec();
        
    }
    
    void disable(UserData user,Msg msg) {
        
        GetChatMemberResponse resp = bot().execute(new GetChatMember(msg.chatId(),user.id.intValue()));

        if (!resp.isOk() || ((resp.chatMember().status() != (ChatMember.Status.administrator) && resp.chatMember().status() != ChatMember.Status.creator))) {

            msg.reply("你不是群组管理 :)").publicFailed();

            return;

        }
        
        JSONArray enable = conf.getJSONArray("enable");

        if (enable != null || !enable.contains(msg.chatId())) {

            msg.reply("没有开启 :)").publicFailed();

            return;

        }

        enable.remove(msg.chatId());

        conf.put("enable",enable);

        save();

        msg.reply("关闭成功 :)").exec();
        
        
    }
    
}
