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

    public UserArchive(String dirName,long id) { super(dirName,id); }

    public Long createdAt;

    public String name;
    public String screenName;
    public String bio;
    public String photoUrl;

    public Boolean isProtected;

    public Boolean isDisappeared;

    @Override
    protected void init() {
    }

    public void read(User user) {

        createdAt = user.getCreatedAt().getTime();

        name = user.getName();

        screenName = user.getScreenName();

        bio = user.getDescription();

        photoUrl = user.getBiggerProfileImageURL();

        isProtected = user.isProtected();

        isDisappeared = false;

    }

    @Override
    protected void load(JSONObject obj) {

        createdAt = obj.getLong("created_at");
        name = obj.getStr("name");
        screenName = obj.getStr("screen_name");
        bio = obj.getStr("bio");
        photoUrl = obj.getStr("photo_url");
        
        isProtected = obj.getBool("is_protected");
        isDisappeared = obj.getBool("is_disappeared");

    }

    public String getMarkdownURL() {

        return "[" + name + "](" + getURL() + ")";
        
    }

    public String getURL() {

        return "https://twitter.com/" + screenName;

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
