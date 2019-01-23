package io.kurumi.ntt;

import java.io.File;
import cn.hutool.json.*;
import cn.hutool.core.io.*;
import java.util.*;
import com.pengrad.telegrambot.model.*;

public class Data {

    public File dataDir;
    public File dataFile;
    
    public String botToken;
    
    public boolean useAuthServer = false;
    public int authServerPort = 19132;
    public String authServerDomain;
    
    public Data(File rootDir) {

        dataDir = new File(rootDir, "data");
        dataFile = new File(dataDir, "botData.json");
        
        refresh();

    }
    
    private HashMap<Long,UserData> userDataCache = new HashMap<>();
    
    public LinkedList<UserData> getUsers() {
        
        return new LinkedList<UserData>(userDataCache.values());
        
    }
    
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
    
    public void refresh() {

        try {

            JSONObject botData = new JSONObject(FileUtil.readUtf8String(dataFile));
            
            botToken = botData.getStr("bot_token");

        } catch (Exception e) {}

    }

    public void save() {

        JSONObject botData = new JSONObject();
        
        botData.put("bot_token",botToken);
        
        JSONObject authServer = new JSONObject();
        
        authServer.put("enable",useAuthServer);
        authServer.put("local_port",authServerPort);
        authServer.put("domain",authServerDomain);
        
        botData.put("auth_server",authServer);
        
        FileUtil.writeUtf8String(botData.toStringPretty(), dataFile);

    }

}
