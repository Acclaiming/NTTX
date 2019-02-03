package io.kurumi.nttools.spam;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SpamList {
    
    public transient MainFragment main;
    
    public transient File spamFile;
    
    public String uuid;
    
    public String name;
    
    public String description;
    
    public LinkedList<UserSpam> spamUsers;
    
    public SpamList(Fragment fragment,String uuid) {
        
        main = fragment.main;
        this.uuid = uuid;
        
        spamFile = new File(main.dataDir,"twitter_spam/" + uuid + ".json");
        
    }
    
    public void laod() {
        
        JSONObject spam = new JSONObject(FileUtil.readUtf8String(spamFile));

        name = spam.getStr("name");
        description = spam.getStr("description");
        
        List<JSONObject> spamUserArray = (List<JSONObject>)(Object)spam.getJSONArray("spam_users");

        spamUsers.clear();
        
        for(JSONObject userSpamObj : spamUserArray) {
            
            spamUsers.add(new UserSpam(this,userSpamObj));
            
        }
        
    }

    @Override
    public boolean equals(Object obj) {
        // TODO: Implement this method
        return super.equals(obj);
    }
    
}
