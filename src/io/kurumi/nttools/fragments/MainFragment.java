package io.kurumi.nttools.fragments;


import com.pengrad.telegrambot.model.Update;
import java.io.File;
import cn.hutool.json.JSONObject;
import cn.hutool.core.io.FileUtil;
import java.util.HashMap;
import java.util.Map;

public class MainFragment extends Fragment {
    
    public int serverPort = 19132;
    public String serverDomain;
    public File dataDir;

    public MainFragment(File dataDir) {
        
        super(null);
        main = this;
        this.dataDir = dataDir;
        
        refresh();
        
    }

    @Override
    public String name() { return "main"; }
    
    public Map<String,String> tokens;
    
    public void refresh() {

        try {

            JSONObject botData = new JSONObject(FileUtil.readUtf8String(new File(dataDir,"config.json")));
            
            tokens = (Map<String,String>)((Object)botData.getJSONObject("bot_token"));
            
            serverPort = botData.getInt("local_port", serverPort);
            serverDomain = botData.getStr("server_domain");


        } catch (Exception e) {}

    }

    public void save() {

        JSONObject botData = new JSONObject();

        botData.put("bot_token", new JSONObject(tokens));

        JSONObject server = new JSONObject();

        server.put("local_port",serverPort);
        server.put("domain",serverDomain);

        botData.put("server",server);

        FileUtil.writeUtf8String(botData.toStringPretty(), new File(dataDir,"config.json"));

    }
    
    @Override
    public void processUpdate(Update update) {

        
    }

}
