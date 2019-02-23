package io.kurumi.ntt.db;

import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.response.GetChatResponse;
import io.kurumi.ntt.BotConf;
import io.kurumi.ntt.fragment.Fragment;
import io.kurumi.ntt.utils.CData;
import java.util.HashMap;
import java.util.LinkedList;

public class UserData extends JSONObject {

    public static final String KEY = "NTT_USERS";
    public static HashMap<Integer, UserData> fastCache = new HashMap<>();

    public Integer id;
    public String firstName;
    public String lastName;
    public String userName;
    public boolean isBot;

    private UserData(int id) {

        this.id = id;

    }

    private UserData(int id, String json) {

        super(json);

        this.id = id;

        this.userName = getStr("u");

        this.firstName = getStr("f");

        this.lastName = getStr("l");

        this.isBot = getBool("i", false);

    }

    public static UserData get(User u) {

        UserData user = get(u.id());

        user.refresh(u);

        return user;

    }

    public static UserData get(Integer id) {

        if (fastCache.containsKey(id)) return fastCache.get(id);

        String data = BotDB.jedis.hget(KEY, id.toString());

        if (data == null) {

            UserData user = new UserData(id);

            fastCache.put(id, user);

            return user;

        }

        UserData user = new UserData(id, data);

        fastCache.put(id, user);

        return user;

    }

    public static LinkedList<UserData> getAll() {

        return new LinkedList<UserData>(fastCache.values());

    }

    public boolean refresh(Fragment fragment) {

        GetChatResponse chat = fragment.bot().execute(new GetChat(id));

        if (!chat.isOk()) return false;

        userName = chat.chat().username();

        firstName = chat.chat().firstName();

        lastName = chat.chat().lastName();

        save();

        return true;

    }

    public void refresh(User u) {

        userName = u.username();

        firstName = u.firstName();

        lastName = u.lastName();

        save();

    }
    
    public String formattedName() {
        
        return name() + " (" + userName() + ") ";
        
    }

    public String name() {

        String name = firstName;

        if (lastName != null) {

            name = lastName + " " + name;

        }

        return name;

    }

    public String userName() {

        return userName != null ? "@" + userName : name();

    }
    
    public boolean isAdmin() {

        return BotConf.FOUNDER.equals(userName);

    }

    public boolean hasPoint() {

        return UserPoint.exists(this);

    }

    public CData point() {

        return UserPoint.get(this);

    }

    public void point(CData data) {

        if (data == null) UserPoint.remove(this);

        else UserPoint.set(this, data);

    }

    public void save() {

        put("u", userName);

        put("f", firstName);

        put("l", lastName);

        put("i", isBot);

        BotDB.jedis.hset(KEY, id.toString(), toString());

    }

}
