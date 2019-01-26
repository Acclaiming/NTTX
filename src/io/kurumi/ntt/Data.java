package io.kurumi.ntt;

import java.io.File;
import cn.hutool.json.*;
import cn.hutool.core.io.*;
import java.util.*;
import com.pengrad.telegrambot.model.*;
import cn.hutool.core.util.*;

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

    public void doClean() {
        
        for (Map.Entry<Long,UserData> userData : userDataCache.entrySet()) {
            
            if (userData.getValue().registered == false) {
                
                userData.getValue().delete();
                
                userDataCache.remove(userData.getKey());
                
            }
            
        }
        
    }
    
    public LinkedList<UserData> loadUsers() {
        
        for (File userDataFile : new File(dataDir,"users").listFiles()) {
            
            Long userId = Long.parseLong(StrUtil.subBefore(userDataFile.getName(), ".json", true));
            
            if (userDataCache.containsKey(userId)) continue;
            
            userDataCache.put(userId,new UserData(this,userId));

        }
        
        return getUsers();
        
    }
    
    public LinkedList<UserData> getUsers() {
        
        return new LinkedList<UserData>(userDataCache.values());
        
    }
    
    public UserData getUser(Message msg) {

        UserData userData = getUser(msg.from().id());

        userData.update(msg);
        
        userData.save();

        return userData;

    }
    
    public UserData getUser(User user) {
        
        UserData userData = getUser(user.id());
        
        userData.update(user);
        
        userData.save();
        
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
            
            JSONObject authServer = botData.getJSONObject("auth_server");
            
            useAuthServer = authServer.getBool("enable",useAuthServer);
            authServerPort = authServer.getInt("local_port",authServerPort);
            authServerDomain = authServer.getStr("domain");
            
            
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
