package io.kurumi.ntt.funcs;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import io.kurumi.ntt.db.BotDB;
import io.kurumi.ntt.db.SData;
import io.kurumi.ntt.db.UserData;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.model.Msg;
import io.kurumi.ntt.model.request.ButtonMarkup;
import io.kurumi.ntt.twitter.TAuth;
import io.kurumi.ntt.utils.CData;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;
import io.kurumi.ntt.model.request.Send;
import io.kurumi.ntt.Launcher;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.KickChatMember;
import io.kurumi.ntt.model.Callback;
import java.util.Timer;
import java.util.Date;

public class GroupProtecter extends Fragment {

    public static GroupProtecter INSTANCE = new GroupProtecter();
    
    public static JSONObject conf = SData.getJSON("data","group_protect",true);

    public static JSONArray enable;
    public static JSONObject pedding;

    static {

        enable = conf.getJSONArray("enable");
        if (enable == null) enable = new JSONArray();

        pedding = conf.getJSONObject("pedding");
        if (pedding == null) pedding = new JSONObject();

    }

    @Override
    public boolean onGroupMsg(UserData user,Msg msg,boolean superGroup) {

        if (msg.isCommand()) {

            switch (msg.command()) {

                case "gp_enable" : enable(user,msg);break;
                case "gp_disable" : disable(user,msg);break;
                default : return false;

            }

            return true;

        } else if (msg.message().newChatMembers() != null && enable.contains(msg.chatId())) {

            for (User newer : msg.message().newChatMembers()) {

                UserData newUser = BotDB.getUserData(newer);

                newUser(user,msg,newUser);

            }

        }

        return false;

    }

    final String POINT_ALLOW_SHOW = "g|a";
    final String POINT_LEAVE = "g|l";

    final String POINT_ADMIN_PASS = "g|p";
    final String POINT_ADMIN_KICK = "g|k";

    void newUser(UserData user,Msg msg,UserData newUser) {

        final CData allow = cdata(POINT_ALLOW_SHOW);
        allow.put("o",newUser.id);

        final CData leave = cdata(POINT_LEAVE);
        leave.put("o",newUser.id);

        final CData pass = cdata(POINT_ADMIN_PASS);
        pass.put("o",newUser.id);

        final CData kick = cdata(POINT_ADMIN_KICK);
        kick.put("o",newUser.id);

        if (TAuth.exists(newUser.id)) {

            msg.send(newUser.userName() + " 你好 ~","本群开启了验证 所以需要显示乃的Twitter认证 请在 180s 之内选择 :)")
                .buttons(new ButtonMarkup() {{


                        newButtonLine()
                            .newButton("「 同意显示账号 」",allow)
                            .newButton("「 退出本群 」",leave);

                        newButtonLine()
                            .newButton("「 通过 (绒布球) 」",pass)
                            .newButton("「 移出 (绒布球) 」",kick);

                    }}).html().exec();

        } else {

            msg.send(newUser.userName() + " 你好 ~","本群开启了认证 请在 300s 内私聊Bot使用 /tauth 验证账号 :)")
                .buttons(new ButtonMarkup() {{

                        newButtonLine()
                            .newButton("「 退出本群 」",leave);

                        newButtonLine()
                            .newButton("「 通过 (绒布球) 」",pass)
                            .newButton("「 移出 (绒布球) 」",kick);

                    }}).html().exec();

        }

    }

    static Timer timer;
    
    public static void start() {
        
        stop();
        
        timer = new Timer();
        timer.schedule(checkTask,new Date(),30 * 1000);
        
    }
    
    public static void stop() {
        
        if (timer != null) timer.cancel();
        timer = null;
        
    }
    
    static TimerTask checkTask = new TimerTask() {

        @Override
        public void run() {

            synchronized (pedding) { 

                for (String userIdStr : new LinkedList<String>(pedding.keySet())) {

                    Integer userId = Integer.parseInt(userIdStr);

                    JSONArray chatsObj = pedding.getJSONArray(userIdStr);
                    List<JSONObject> chats = new LinkedList<JSONObject>(chatsObj.toList(JSONObject.class));

                    for (int index = 0;index < chats.size();index ++) {

                        JSONObject chat = chatsObj.getJSONObject(index);

                        boolean authedBefore = chat.getBool("authed_before");
                        long chatId = chat.getLong("chat_id");
                        long enterAt = chat.getLong("enter_at");
                        int msgId = chat.getInt("msg_id");

                        if ((authedBefore || System.currentTimeMillis() - enterAt > 3 * 60 * 1000)) {
                            
                            Launcher.INSTANCE.bot().execute(new DeleteMessage(chatId,msgId));
                            Launcher.INSTANCE.bot().execute(new KickChatMember(chatId,userId));

                            new Send(chatId,BotDB.getUserData(userId).userName() + " 没有在 180s 内选择是否公开Twitter账号，已移除。").exec();

                        } else if (System.currentTimeMillis() - enterAt > 5 * 60 * 1000) {

                            
                            Launcher.INSTANCE.bot().execute(new DeleteMessage(chatId,msgId));
                            Launcher.INSTANCE.bot().execute(new KickChatMember(chatId,userId));

                            new Send(chatId,BotDB.getUserData(userId).userName() + " 没有在 300s 内认证Twitter账号，已移除。").exec();

                        } else {
                            
                            continue;
                            
                        }
                        
                        chatsObj.remove(chat);
                        
                        pedding.put(userIdStr,chatsObj);

                        save();
                        

                    }

                }

            }

        }

    };

    @Override
    public boolean onCallback(UserData user,Callback callback) {

        switch (callback.data.getPoint()) {

            case POINT_ALLOW_SHOW : allowShow(user,callback);break;
            case POINT_LEAVE : leave(user,callback);break;
            case POINT_ADMIN_PASS : pass(user,callback);break;
            case POINT_ADMIN_KICK : kick(user,callback);break;
            
            default : return false;

        }

        return true;

    }

    void allowShow(UserData user,Callback callback) {

        long origin = callback.data.getLong("o");

        if (user.id != origin) {

            callback.alert("这个验证不针对乃 :)");

            return;

        }

        if (!TAuth.exists(user.id)) {

            callback.alert("乃的认证已移除 请重新认证 :)");

            return;

        }

        synchronized (pedding) {

            if (!pedding.containsKey(user.id.toString())) {

                callback.alert("这个认证已经过期。");
                
                return;

            }

            JSONArray chatsObj = pedding.getJSONArray(user.id.toString());
            List<JSONObject> chats = new LinkedList<JSONObject>(chatsObj.toList(JSONObject.class));

            for (int index = 0;index < chats.size();index ++) {

                JSONObject chat = chats.get(index);

                long chatId = chat.getLong("chat_id");
                int msgId = chat.getInt("msg_id");

                if (chatId == callback.chatId()) {

                    chatsObj.remove(chat);

                    Launcher.INSTANCE.bot().execute(new DeleteMessage(chatId,msgId));
                    new Send(chatId,user.userName() + " 的 Twitter 账号 : " + TAuth.get(user.id).getFormatedNameHtml(),"欢迎加入本群 :)").html().exec();

                }

            }

            pedding.put(user.id.toString(),chatsObj);

            save();


        }

    }
    
    void leave(UserData user,Callback callback) {

        long origin = callback.data.getLong("o");

        if (user.id != origin) {

            callback.alert("这个验证不针对乃 :)");

            return;

        }

        if (!TAuth.exists(user.id)) {

            callback.alert("乃的认证已移除 请重新认证 :)");

            return;

        }

        synchronized (pedding) {

            if (!pedding.containsKey(user.id.toString())) {

                callback.alert("这个认证已经过期。");

                return;

            }

            JSONArray chatsObj = pedding.getJSONArray(user.id.toString());
            List<JSONObject> chats = new LinkedList<JSONObject>(chatsObj.toList(JSONObject.class));

            for (int index = 0;index < chats.size();index ++) {

                JSONObject chat = chats.get(index);

                long chatId = chat.getLong("chat_id");
                int msgId = chat.getInt("msg_id");

                if (chatId == callback.chatId()) {

                    chatsObj.remove(chat);
                    
                    callback.alert("好的。");
                    
                    Launcher.INSTANCE.bot().execute(new DeleteMessage(chatId,msgId));
                    Launcher.INSTANCE.bot().execute(new KickChatMember(chatId,user.id.intValue()));

                    new Send(chatId,user.userName() + " 选择了退出。").exec();
                    
                }

            }

            pedding.put(user.id.toString(),chatsObj);

            save();


        }

    }

    void pass(UserData user,Callback callback) {

        Long origin = callback.data.getLong("o");

        GetChatMemberResponse resp = bot().execute(new GetChatMember(callback.chatId(),user.id.intValue()));

        if (!resp.isOk() || ((resp.chatMember().status() != (ChatMember.Status.administrator) && resp.chatMember().status() != ChatMember.Status.creator))) {

            callback.alert("你不是绒布球 :)");

            return;

        }

        synchronized (pedding) {

            if (!pedding.containsKey(user.id.toString())) {

                callback.alert("这个认证已经过期。");

                return;

            }

            JSONArray chatsObj = pedding.getJSONArray(origin.toString());
            List<JSONObject> chats = new LinkedList<JSONObject>(chatsObj.toList(JSONObject.class));

            for (int index = 0;index < chats.size();index ++) {

                JSONObject chat = chats.get(index);

                long chatId = chat.getLong("chat_id");
                int msgId = chat.getInt("msg_id");

                if (chatId == callback.chatId()) {

                    chatsObj.remove(chat);

                    callback.alert("好。");

                    Launcher.INSTANCE.bot().execute(new DeleteMessage(chatId,msgId));
                    
                    new Send(chatId,BotDB.getUserData(origin).userName() + " 已被绒布球 " + user.userName() + " 放行。").html().exec();

                }

            }

            pedding.put(user.id.toString(),chatsObj);

            save();


        }

    }

    void kick(UserData user,Callback callback) {

        Long origin = callback.data.getLong("o");

        GetChatMemberResponse resp = bot().execute(new GetChatMember(callback.chatId(),user.id.intValue()));

        if (!resp.isOk() || ((resp.chatMember().status() != (ChatMember.Status.administrator) && resp.chatMember().status() != ChatMember.Status.creator))) {

            callback.alert("你不是绒布球 :)");

            return;

        }

        synchronized (pedding) {

            if (!pedding.containsKey(user.id.toString())) {

                callback.alert("这个认证已经过期。");

                return;

            }

            JSONArray chatsObj = pedding.getJSONArray(origin.toString());
            List<JSONObject> chats = new LinkedList<JSONObject>(chatsObj.toList(JSONObject.class));

            for (int index = 0;index < chats.size();index ++) {

                JSONObject chat = chats.get(index);

                long chatId = chat.getLong("chat_id");
                int msgId = chat.getInt("msg_id");

                if (chatId == callback.chatId()) {

                    chatsObj.remove(chat);

                    callback.alert("好。");

                    Launcher.INSTANCE.bot().execute(new DeleteMessage(chatId,msgId));
                    Launcher.INSTANCE.bot().execute(new KickChatMember(chatId,origin.intValue()));

                    new Send(chatId,BotDB.getUserData(origin).userName() + " 已被绒布球 " + user.userName() + " 移除。").html().exec();

                }

            }

            pedding.put(user.id.toString(),chatsObj);

            save();


        }

    }

    static void userAuthed(Long userId) {

        synchronized (pedding) {

            if (pedding.containsKey(userId.toString())) {

                JSONArray chatsObj = pedding.getJSONArray(userId.toString());
                List<JSONObject> chats = new LinkedList<JSONObject>(chatsObj.toList(JSONObject.class));

                for (int index = 0;index < chats.size();index ++) {

                    JSONObject chat = chats.get(index);

                    boolean authedBefore = chat.getBool("authed_before");
                    long chatId = chat.getLong("chat_id");
                    int msgId = chat.getInt("msg_id");

                    if (!authedBefore) {

                        chatsObj.remove(chat);

                        Launcher.INSTANCE.bot().execute(new DeleteMessage(chatId,msgId));
                        new Send(chatId,BotDB.getUserData(userId).userName() + " 的 Twitter 账号 : " + TAuth.get(userId).getFormatedNameHtml(),"欢迎加入本群 :)").html().exec();

                    }

                }

                pedding.put(userId.toString(),chatsObj);

                save();

            }

        }

    }

    static void save() {

        conf.put("enable",enable);

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

        if (enable.contains(msg.chatId())) {

            msg.reply("无需重复开启 :)").publicFailed();

            return;

        }

        enable.add(msg.chatId());

        save();

        msg.reply("开启成功 新群员必须人工通过或认证账号 :)").exec();

    }

    void disable(UserData user,Msg msg) {

        GetChatMemberResponse resp = bot().execute(new GetChatMember(msg.chatId(),user.id.intValue()));

        if (!resp.isOk() || ((resp.chatMember().status() != (ChatMember.Status.administrator) && resp.chatMember().status() != ChatMember.Status.creator))) {

            msg.reply("你不是群组管理 :)").publicFailed();

            return;

        }

        if (!enable.contains(msg.chatId())) {

            msg.reply("没有开启 :)").publicFailed();

            return;

        }

        enable.remove(msg.chatId());

        save();

        msg.reply("关闭成功 :)").exec();


    }

}
