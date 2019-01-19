package io.kurumi.ntbot;

import java.io.File;
import cn.hutool.json.*;
import cn.hutool.core.io.*;
import java.util.*;
import com.pengrad.telegrambot.model.*;

public class Data {

    public File dataDir;
    public File dataFile;

    public Data(File rootDir) {

        dataDir = new File(rootDir, "data");
        dataFile = new File(dataDir, "botData.json");
        
        refresh();

    }
    
    private HashMap<Long,UserData> userDataCache = new HashMap<>();
    
    public UserData getUser(User user) {
        
        UserData userData = getUser(user.id());
        
        userData.update(user);
        
        return userData;
        
    }
    
    public UserData getUser(long id) {
        
        if(userDataCache.containsKey(id)) return userDataCache.get(id);
        
        UserData userData = new UserData(this,id);
        
        userDataCache.put(id,userData);
        
        return userData;
        
    }

    public JSONObject botData;

    public void refresh() {

        try {

            botData = new JSONObject(FileUtil.readUtf8String(dataFile));

        } catch (Exception e) {

            botData = new JSONObject();
            
            setBotToken("");

        }

    }

    public void save() {

        FileUtil.writeUtf8String(botData.toStringPretty(), dataFile);

    }
    
    public String getBotToken() {
        
        return botData.getStr("bot_token","");
        
    }
    
    public void setBotToken(String token) {
        
        botData.put("bot_token",token);
        save();
        
    }

}
