package io.kurumi.ntbot;

import java.io.File;
import cn.hutool.json.*;
import cn.hutool.core.io.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.ui.entries.*;

public class UserData {

    public Data data;
    public long id;

    public File userDataFile;

    public UserData(Data data, long id) {

        this.data = data;
        this.id = id;

        userDataFile = new File(data.dataDir, "users/" + id + ".json");

        refresh();
        
    }

    public JSONObject userData;
    
    private UserInterface ui;
    
    public UserInterface getInterface() {
        
        if (ui == null) ui = new UserInterface(this);
        
        return ui;
        
    }
    
    public void refresh() {

        try {

            userData = new JSONObject(FileUtil.readUtf8String(userDataFile));

        } catch (Exception e) {

            userData = new JSONObject();
            save();

        }


    }

    public void save() {

        FileUtil.writeUtf8String(userData.toStringPretty(), userDataFile);

    }

    public boolean is(String key, boolean defaultValue) {

        return userData.getBool(key, defaultValue);

    }

    public String get(String key, String defaultValue) {

        return userData.getStr(key, defaultValue);

    }

    public Integer getInt(String key, Integer defaultValue) {

        return userData.getInt(key, defaultValue);

    }

    public Long getLong(String key, Long defaultValue) {

        return userData.getLong(key, defaultValue);

    }

    public void set(String key, Object value) {

        userData.put(key, value);

    }

    public boolean isRegistered() {

        return is("registered", false);

    }

    public void setRegistered(boolean registered) {

        set("registered", registered);

    }

    public boolean isAdmin() {

        return is("is_admin", false);

    }

    public void setAdmin(boolean admin) {

        set("is_admin", admin);

    }

    public String getUserName() {

        return get("username", null);

    }

    public void setUserName(String name) {

        set("username", name);

    }

    public String getName() {

        return get("name", null);

    }

    public void setName(String name, String last) {

        if (last != null && !"".equals(last)) {

            name =  last + " " + name;

        }

        set("name", name);

    }
    
    public boolean isBot() {
        
        return is("is_bot",false);
        
    }
    
    public void setIsBot(boolean isBot) {
        
        set("is_bot",isBot);
        
    }

    public void update(User from) {

        setUserName(from.username());
        setName(from.firstName(), from.lastName());
        setIsBot(from.isBot());
        
        save();
        
    }


    public void delete() {

        FileUtil.del(userDataFile);

    }

}
