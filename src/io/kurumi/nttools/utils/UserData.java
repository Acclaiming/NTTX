package io.kurumi.nttools.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import io.kurumi.nttools.twitter.TwiAccount;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class UserData extends JSONObject {

    private Integer id;
    public File userDataFile;

    public UserData(MainFragment main, Integer id) {

        this.id = id;

        userDataFile = new File(main.dataDir, "/users/" + id + ".json");

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

        return ArrayUtil.contains(admins, userName()) || getBool("is_admin", false);

    }

    public void setAdmin(boolean admin) {

        put("is_admin", admin);

    }

    
    public Integer id() {
        
        return id;
        
    }

    public String name() {

        return getStr("name");

    }

    public void setName(String first, String last) {

        if (last != null && !"".equals(last)) {

            first =  last + " " + first;

        }

        put("name", first);

    }

    public void refresh() {

        try {

            JSONObject userData = new JSONObject(FileUtil.readUtf8String(userDataFile));

            clear();

            putAll(userData);

        } catch (Exception e) {}

    }

    public String userName() {

        return getStr("user_name");

    }

    public void setUserName(String name) {

        put("user_name", name);

    }

    public Boolean isBot() {

        return getBool("is_bot", false);

    }

    public void setIsBot(boolean bot) {

        put("is_bot", bot);

    }
    
    public LinkedList<TwiAccount> getTwitterAccounts() {

        LinkedList<TwiAccount> accounts = new LinkedList<>();

        List<JSONObject> twitterAccountList = (List<JSONObject>)(Object)getJSONArray("twitter_accounts");

        if (twitterAccountList != null) {

            for (JSONObject obj : twitterAccountList) {

                accounts.add(new TwiAccount(obj));

            }

        }

        return accounts;

    }
    

    public void setTwitterAccounts(LinkedList<TwiAccount> accounts) {
    
        JSONArray twitterAccountList = new JSONArray();

        for (TwiAccount account : accounts) {

            twitterAccountList.add(account.toJSONObject());

        }

        put("twitter_accounts", twitterAccountList);

    }
    
    public TwiAccount findUser(String screenName) {

        for (TwiAccount acc : getTwitterAccounts()) {

            if (screenName.equals(acc.screenName)) {

                return acc;

            }

        }

        return null;

    }

    public TwiAccount findUser(long accountId) {

        for (TwiAccount acc : getTwitterAccounts()) {

            if (acc.accountId == accountId) {

                return acc;

            }

        }

        return null;

    }
    
    public CData getPoint() {
        
        JSONObject point = getJSONObject("point");

        if (point == null) return null;
        
        return new CData(point);
        
    }
    
    public void setPoint(CData point) {
        
        put("point",point);
        
    }

    public void save() {

        FileUtil.writeUtf8String(toStringPretty(), userDataFile);

    }

    public void update(Fragment fragment,Message from) {
        update(from.from());
    }


    public void update(User from) {

        setUserName(from.username());
        setName(from.firstName() , from.lastName());
        setIsBot(from.isBot());

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

