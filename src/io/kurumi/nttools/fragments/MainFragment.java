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

public abstract class MainFragment extends Fragment {
    
    public int serverPort = -1;
    public String serverDomain;
    public File dataDir;

    private HashMap<Long,UserData> userDataCache = new HashMap<>();

    public LinkedList<UserData> getUsers() {

        return new LinkedList<UserData>(userDataCache.values());

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
