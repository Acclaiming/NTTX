package io.kurumi.nttools.spam;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import io.kurumi.nttools.fragments.Fragment;
import io.kurumi.nttools.fragments.MainFragment;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class SpamList extends JSONObject {
    
    public transient MainFragment main;

    public transient File spamFile;

    public String id;

    public String name;

    public String description;

    public LinkedList<UserSpam> spamUsers = new LinkedList<>();

    public SpamList(Fragment fragment, String id) {

        main = fragment.main;
        this.id = id;

        spamFile = new File(main.dataDir, "twitter_spam/" + id + ".json");
        
        load();
        
    }

    public void load() {

        try {

            JSONObject spam = new JSONObject(FileUtil.readUtf8String(spamFile));

            putAll(spam);

        } catch (Exception e) {}

        name = getStr("name", "未命名");
        description = getStr("description", "暂无简介");

        List<JSONObject> spamUserArray = (List<JSONObject>)(Object)getJSONArray("spam_users");

        spamUsers.clear();

        if (spamUserArray != null) {

            for (JSONObject userSpamObj : spamUserArray) {

                spamUsers.add(new UserSpam(this, userSpamObj));

            }

        }

    }
    
    public void save() {
        
        put("name",name);
        
        put("description",description);
        
        JSONArray spamUserArray = new JSONArray();
        
        for (UserSpam spam : spamUsers) {
            
            spamUserArray.add(spam.toJSONObject());
            
        }
        
        put("spam_users",spamUsers);
        
        FileUtil.writeUtf8String(toStringPretty(),spamFile);
        
    }
    
    public void delete() {
        
        FileUtil.del(spamFile);
        
    }

    @Override
    public boolean equals(Object obj) {

        return super.equals(obj) || ((obj instanceof SpamList) && id.equals(((SpamList)obj).id));

    }

    public static final String nextId(Fragment fragment) {

        File countFile = new File(fragment.main.dataDir, "twitter_spam.count");

        try {

            Long count = Long.parseLong(FileUtil.readUtf8String(countFile));

            count ++;

            FileUtil.writeUtf8String(count.toString(), countFile);

            return count.toString();

        } catch (Exception e) {

            FileUtil.writeUtf8String("0", countFile);

            return "0";

        }



    }

}
