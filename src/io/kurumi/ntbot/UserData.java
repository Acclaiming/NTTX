package io.kurumi.ntbot;

import java.io.File;
import cn.hutool.json.*;
import cn.hutool.core.io.*;
import com.pengrad.telegrambot.model.*;
import io.kurumi.ntbot.ui.entries.*;
import java.util.*;
import io.kurumi.ntbot.twitter.*;

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

    public boolean registered = false;
    public boolean isAdmin = false;
    public boolean isSpam = false;

    public String userName;
    public String name;
    public boolean isBot = false;
    
    public String point = "";

    public LinkedList<ApiToken> apiTokens = new LinkedList<>();
    public LinkedList<TwiAccount> twitterAccounts = new LinkedList<>();
    
    public void setName(String first, String last) {

        if (last != null && !"".equals(last)) {

            first =  last + " " + first;

        }

        name = first;

    }

    public void refresh() {

        try {

            JSONObject userData = new JSONObject(FileUtil.readUtf8String(userDataFile));

            registered = userData.getBool("registered", false);
            isAdmin = userData.getBool("is_admin", false);
            isSpam = userData.getBool("is_spam",false);
            userName = userData.getStr("user_name");
            name = userData.getStr("name");
             isBot = userData.getBool("is_bot");
            point = userData.getStr("point");

            JSONArray twitterAccountList = userData.getJSONArray("twitter_accounts");

            twitterAccounts.clear();

            for (Object obj : twitterAccountList) {

                twitterAccounts.add(new TwiAccount((JSONObject)obj));

            }
            
            JSONArray apiTokenList = userData.getJSONArray("api_tokens");

            apiTokens.clear();

            for (Object obj : apiTokenList) {

                apiTokens.add(new ApiToken((JSONObject)obj));

            }
            

        } catch (Exception e) {}

    }

    public JSONObject getBaseData() {

        JSONObject userData = new JSONObject();

        JSONArray apiTokenList = new JSONArray();

        for (ApiToken apiToken : apiTokens) {

            apiTokenList.add(apiToken.toJSONObject());

        }
        
        JSONArray twitterAccountList = new JSONArray();

        for (TwiAccount account : twitterAccounts) {
            
            twitterAccountList.add(account.toJsonObject());
            
        }
        
        userData.put("api_tokens",apiTokenList);
        userData.put("twitter_accounts",twitterAccountList);
        
        return userData;

    }

    public JSONObject toJSONObject() {

        JSONObject userData = getBaseData();

        userData.put("registered", registered);
        userData.put("is_admin", isAdmin);
        userData.put("is_spam",isSpam);
        
        userData.put("user_name", userName);
        userData.put("name", name);

        userData.put("is_bot", isBot);
        
        userData.put("point",point);
        
        return userData;
        
    }

    public void save() {
        
        FileUtil.writeUtf8String(toJSONObject().toStringPretty(),userDataFile);
        
    }

    public void update(User from) {

        userName = from.username();
        setName(from.firstName() , from.lastName());
        isBot = from.isBot();

        save();

    }

    public void delete() {

        FileUtil.del(userDataFile);

    }

    private UserInterface ui;

    public UserInterface getInterface() {

        if (ui == null) ui = new UserInterface(this);

        return ui;

    }


}
