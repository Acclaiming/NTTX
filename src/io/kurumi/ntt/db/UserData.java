package io.kurumi.ntt.db;

import cn.hutool.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import io.kurumi.ntt.fragment.Fragment;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.response.GetChatResponse;
import com.pengrad.telegrambot.model.Chat;
import cn.xsshome.taip.nlp.NLPConsts;
import io.kurumi.ntt.BotConf;
import io.kurumi.ntt.utils.CData;

public class UserData extends JSONObject {

    public static final String KEY = "NTT_USERS";

    public Integer id;

    public String firstName;

    public String lastName;
    
    public String userName;

    public boolean isBot;

    private UserData(int id) {

        this.id = id; 

    }
    
    private UserData(int id,String json) {

        super(json);
        
        this.id = id;
        
        this.userName = getStr("u");
        
        this.firstName = getStr("f");
        
        this.lastName = getStr("l");
        
        this.isBot = getBool("i",false);

    }
    
    public boolean refresh(Fragment fragment) {
        
        GetChatResponse chat = fragment.bot.execute(new GetChat(id));
        
        if (!chat.isOk()) return false;
        
        refresh(chat.chat());
        
        return true;

    }

    public void refresh(Chat chat) {
        
        userName = chat.username();
        
        firstName = chat.firstName();
        
        lastName = chat.lastName();
        
    }
    
    public String name() {
        
        String name = firstName;

        if (lastName != null) {
            
            name =  lastName + " " + name;
            
        }
        
        return name;
        
    }
    
    public boolean isAdmin() {
        
        return BotConf.FOUNDER.equals(userName) || Permission.isAdmin(id);
        
    }
    
    public boolean isBureaucrats() {

        return BotConf.FOUNDER.equals(userName) || Permission.isBureaucrats(id);

    }
    
    public boolean hasPoint() {
        
        return UserPoint.exists(this);
        
    }
    
    public CData point() {

        return UserPoint.get(this);

    }
    
    public void point(CData data) {

        if (data == null) UserPoint.remove(this);
        
        else UserPoint.set(this,data);

    }

    public void save() {

        put("u", userName);

        put("f",firstName);
        
        put("l",lastName);

        put("i",isBot);
        
        BotDB.jedis.hset(KEY, id.toString(), toString());

    }

    private static HashMap<Integer,UserData> cache = new HashMap<>();
    
    static {
        
        Map<String, String> all = BotDB.jedis.hgetAll(KEY);
        
        for (Map.Entry<String,String> user : all.entrySet()) {
            
            int id = Integer.parseInt(user.getKey());
            
            UserData u = new UserData(id,user.getValue());

            cache.put(id,u);
            
        }

    }
    
    
    public static UserData get(Integer id) {

        if (cache.containsKey(id)) return cache.get(id);
        
        String data = BotDB.jedis.hget(KEY, id.toString());

        if (data == null) {
            
            UserData user = new UserData(id);
            
            cache.put(id,user);
            
            return user;

        }
        
        UserData user = new UserData(id,data);
        
        cache.put(id,user);
        
        return user;
        
    }
    
    public static LinkedList<UserData> getAll() {
        
        return new LinkedList<UserData>(cache.values());
        
    }

}
