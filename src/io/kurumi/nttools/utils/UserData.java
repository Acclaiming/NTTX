package io.kurumi.nttools.utils;

import java.io.File;
import cn.hutool.json.*;
import com.pengrad.telegrambot.model.*;
import cn.hutool.core.io.*;
import java.util.*;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.twitter.TwiAccount;
import cn.hutool.core.util.ArrayUtil;

public class UserData {

    public Fragment bot;
    public long id;
    public File userDataFile;
    
    public Long chatId;

    public UserData(Fragment bot, long id) {

        this.bot = bot;
        this.id = id;

        userDataFile = new File(bot.main.dataDir,bot.name() +  "/users/" + id + ".json");
        
        refresh();

    }

    public boolean isAdmin() {
        
        String[] admins = new String[] {
            "HiedaNaKan",
            "dodolookyukina",
            "bakaoxoxox",
            "qtqjaq",
            "shinoharaMia",
        };
        
        return ArrayUtil.contains(admins,userName);
        
    }
    
    public String userName;
    public String name;
    public boolean isBot = false;

    public String point;

    public JSONObject ext;


    // public LinkedList<ApiToken> apiTokens = new LinkedList<>();
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

                       userName = userData.getStr("user_name", "");
            name = userData.getStr("name", "");
            isBot = userData.getBool("is_bot", false);

            point = userData.getStr("point");



            chatId = userData.getLong("chatId");

            List<JSONObject> twitterAccountList = (List<JSONObject>)(Object)userData.getJSONArray("twitter_accounts");

            twitterAccounts.clear();

            for (JSONObject obj : twitterAccountList) {

                twitterAccounts.add(new TwiAccount(obj));

            }

            ext= userData.getJSONObject("ext");

            if (ext == null) {

                ext = new JSONObject();

            }

            /*

             JSONArray apiTokenList = userData.getJSONArray("api_tokens");

             apiTokens.clear();

             for (Object obj : apiTokenList) {

             apiTokens.add(new ApiToken((JSONObject)obj));

             }

             */


        } catch (Exception e) {}

    }

    public JSONObject toJSONObject() {

        JSONObject userData = new JSONObject();

        userData.put("user_name", userName);
        userData.put("name", name);

        userData.put("is_bot", isBot);

        userData.put("point", point);

        userData.put("chatId", chatId);

        userData.put("ext",ext);

        /*

         JSONArray apiTokenList = new JSONArray();

         for (ApiToken apiToken : apiTokens) {

         apiTokenList.add(apiToken.toJSONObject());

         }

         */

        JSONArray twitterAccountList = new JSONArray();

        for (TwiAccount account : twitterAccounts) {

            twitterAccountList.add(account.toJsonObject());

        }

        //  userData.put("api_tokens", apiTokenList);
        userData.put("twitter_accounts", twitterAccountList);


        return userData;

    }

    public void save() {

        FileUtil.writeUtf8String(toJSONObject().toStringPretty(), userDataFile);

    }

    public void update(Message from) {

        update(from.from());
        chatId = from.chat().id();

    }


    public void update(User from) {

        userName = from.username();
        setName(from.firstName() , from.lastName());
        isBot = from.isBot();

    }

    public TwiAccount findUser(String accountId) {

        return findUser(Long.parseLong(accountId));

    }

    public TwiAccount findUser(long accountId) {

        for (TwiAccount acc : twitterAccounts) {

            if (acc.accountId == accountId) {

                return acc;

            }

        }

        return null;

    }

    public void delete() {

        FileUtil.del(userDataFile);

    }

    @Override
    public boolean equals(Object obj) {

        if (super.equals(obj)) return true;

        if (!(obj instanceof UserData)) return false;

        if (((UserData)obj).id != id) return false;

        return true;

    }

}

