package io.kurumi.nttools.fragments;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.pengrad.telegrambot.model.User;
import io.kurumi.nttools.utils.UserData;
import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import io.kurumi.nttools.spam.SpamList;

public abstract class MainFragment extends Fragment {
    
    public int serverPort = -1;
    public String serverDomain;
    public File dataDir;

    private HashMap<Long,UserData> userDataCache = new HashMap<>();

    public LinkedList<UserData> getUsers() {

        return new LinkedList<UserData>(userDataCache.values());

    }
    
    public UserData findUserData(String userName) {
        
        for (UserData user : userDataCache.values()) {
            
            if (userName.equals(user.userName)) return user;
            
        }
        
        return null;
        
    }
    

    public UserData getUserData(User user) {

        UserData ud = getUserData(user.id());
      
        ud.update(user);
        
        return ud;

    }

    public UserData getUserData(long id) {

        if (userDataCache.containsKey(id)) return userDataCache.get(id);

        UserData ud = new UserData(this, id);

        userDataCache.put(id, ud);

        return ud;

    }
    
    private HashMap<String,SpamList> spamListCahche = new HashMap<>();

    public SpamList getSpamList(String id) {
        
        return spamListCahche.get(id);
        
    }
    
    public SpamList deleteSpamList(String id) {

        SpamList list = spamListCahche.remove(id);
        
        list.delete();
        
        return list;

    }
    
    public LinkedList<SpamList> getSpamLists() {
        
        return new LinkedList<SpamList>(spamListCahche.values());
       
    }
    
    public SpamList newSpamList(String name) {
        
        SpamList list = new SpamList(this, SpamList.nextId(this));

        list.name = name;

        list.save();
        
        spamListCahche.put(list.id,list);
        
        return list;
        
    }
    
    public MainFragment(File dataDir) {

        super(null);
        main = this;
        this.dataDir = dataDir;

        File[] ul = new File(main.dataDir, "/users").listFiles();

        if (ul != null) {

            for (File userDataFile : ul) {

                long userId = Long.parseLong(StrUtil.subBefore(userDataFile.getName(), ".json", true));

                if (userDataCache.containsKey(userId)) continue;

                userDataCache.put(userId, new UserData(main, userId));

            }

        }
        
        File[] sl = new File(main.dataDir, "/twitter_spam").listFiles();

        if (sl != null) {

            for (File userDataFile : sl) {

                String listId = StrUtil.subBefore(userDataFile.getName(), ".json", true);

                if (spamListCahche.containsKey(listId)) continue;

                spamListCahche.put(listId,new SpamList(main, listId));

            }

        }
        


        refresh();


    }

    @Override
    public String name() { return "main"; }

    public Map<String,String> tokens;

    public void refresh() {

        try {

            JSONObject botData = new JSONObject(FileUtil.readUtf8String(new File(dataDir, "config.json")));

            tokens = (Map<String,String>)((Object)botData.getJSONObject("bot_token"));

            serverPort = botData.getInt("local_port", serverPort);
            serverDomain = botData.getStr("server_domain");


        } catch (Exception e) {}

    }

    public void save() {

        JSONObject botData = new JSONObject();

        botData.put("bot_token", new JSONObject(tokens));

        botData.put("local_port", serverPort);
        botData.put("server_domain", serverDomain);

        FileUtil.writeUtf8String(botData.toStringPretty(), new File(dataDir, "config.json"));

    } 

}
