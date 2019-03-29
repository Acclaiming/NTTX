package io.kurumi.ntt.twitter.archive;

import cn.hutool.json.JSONObject;
import io.kurumi.ntt.model.data.IdDataModel;
import java.util.LinkedList;
import twitter4j.User;
import java.util.LinkedHashSet;
import cn.hutool.json.JSONArray;
import java.sql.Struct;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import io.kurumi.ntt.utils.*;
import cn.hutool.core.util.ObjectUtil;

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

    public String changeStr;
    
    public boolean read(User user) {

        boolean change = false;
        changeStr = null;

        createdAt = user.getCreatedAt().getTime();

        String nameL = name;

        if (!(name = user.getName()).equals(nameL)) {
            
            changeStr = "名称更改 : " + nameL + " ------> " + name;
            
            change = true;
            
        }

        String screenNameL = screenName;

        if (!(screenName = user.getScreenName()).equals(screenNameL)) {
            
            changeStr = ("".equals(changeStr) ? "" : changeStr + "\n") + "用户名更改 @" + screenNameL + " ------> @" + screenName;
            
            change = true;
            
        }

        String bioL = bio;

        if (!ObjectUtil.equal(bio = user.getDescription(),bioL)) {
            
            changeStr = ("".equals(changeStr) ? "" : changeStr + "\n") + "简介更改 : " + bioL + " \n\n ------> \n\n" + bio;
            
            change = true;
            
        }
        
        String photoL = photoUrl;

        if (!ObjectUtil.equal(photoUrl = user.getBiggerProfileImageURL(),photoL)) {
            
            changeStr = ("".equals(changeStr) ? "" : changeStr + "\n") + "头晕更改 : " + Html.a("媒体文件",photoL) + " ------> " + Html.a("媒体文件",photoUrl);

            change = true;
            
        }
        
        boolean protectL = isProtected;

        if (protectL != (isProtected = user.isProtected())) {
            
            changeStr = ("".equals(changeStr) ? "" : changeStr + "\n") + "保护状态更改 : " + (isProtected ? "开启了锁推" : "关闭了锁推");

            change = true;
           
        }

        isDisappeared = false;
        
        return change;

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

    public String getHtmlURL() {

        return Html.a(name,getURL());

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
