package io.kurumi.ntt.twitter.archive;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.model.data.IdDataModel;
import java.util.LinkedList;
import twitter4j.User;
import java.util.LinkedHashSet;
import cn.hutool.json.JSONArray;
import java.sql.Struct;
import cn.hutool.core.util.StrUtil;

public class UserArchive extends IdDataModel {

    public static Factory<UserArchive> INSTANCE = new Factory<UserArchive>(UserArchive.class,"twitter_archives/users");
    
    public UserArchive(String dirName, long id) { super(dirName,id); }
    
    public long createdAt;
    
    public LinkedHashSet<String> name;
    public LinkedHashSet<String> screenName;
    public LinkedHashSet<String> bio;
    public LinkedHashSet<String> photoUrl;

    public boolean isProtected;

    public boolean isDisappeared;

    @Override
    protected void init() {

        name = new LinkedHashSet<>();
        screenName = new LinkedHashSet<>();
        bio = new LinkedHashSet<>();
        photoUrl = new LinkedHashSet<>();

    }

    public void read(User user) {

        createdAt = user.getCreatedAt().getTime();
        
        name.add(user.getName());

        screenName.add(user.getScreenName());

        if (!StrUtil.isBlank(user.getDescription())) {

            bio.add(user.getDescription());

        }
        
        if (!user.isDefaultProfileImage()) {
            
            photoUrl.add(user.getBiggerProfileImageURL());
            
        }

        isProtected = user.isProtected();

        isDisappeared = false;

    }

    @Override
    protected void load(JSONObject obj) {

        createdAt = obj.getLong("created_at");
        name = new LinkedHashSet<>(obj.getJSONArray("name"));
        screenName = new LinkedHashSet<>(obj.getJSONArray("screen_name"));
        bio = new LinkedHashSet<>(obj.getJSONArray("bio"));
        photoUrl = new LinkedHashSet<>(obj.getJSONArray("photo_url"));
        
        isProtected = obj.getBool("is_protected");
        isDisappeared = obj.getBool("is_disappeared");

    }

    @Override
    protected void save(JSONObject obj) {

        obj.put("created_at",createdAt);
        obj.put("name",name);
        obj.put("screen_name",screenName);
        obj.put("bio",bio);
        obj.put("photo_url",photoUrl);

        obj.put("is_protected",isProtected);
        obj.put("is_disappeared",isDisappeared);
        
    }

}
