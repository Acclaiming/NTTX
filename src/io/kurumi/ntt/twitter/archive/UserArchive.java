package io.kurumi.ntt.twitter.archive;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import io.kurumi.ntt.model.data.IdDataModel;
import io.kurumi.ntt.twitter.track.UTTask;
import io.kurumi.ntt.utils.Html;
import java.util.HashMap;
import twitter4j.User;
import io.kurumi.ntt.db.BotDB;

public class UserArchive {
    
    public Long id;
    public Long createdAt;

    public String name;
    public String screenName;
    public String bio;
    public String photoUrl;

    public Boolean isProtected;

    public Boolean isDisappeared;
    
    public boolean read(User user) {

        if (user == null && !isDisappeared) {

            isDisappeared = true;

            UTTask.onUserChange(this,"用户被冻结或已停用 :)");

        }
        
        if (user == null && isDisappeared) {
            
            return false;
            
        }

        boolean change = false;
        StringBuilder str = new StringBuilder();
        String split = "\n--------------------------------\n";

        String nameL = name;

        if (isDisappeared)  {

            isDisappeared = false;

            str.append(split).append("用户被取消了冻结/重新启用 :)");

            change = true;

        }

        if (!(name = user.getName()).equals(nameL)) {

            str.append(split).append("名称更改 : ").append(nameL).append(" ------> ").append(name);

            change = true;

        }

        String screenNameL = screenName;

        if (!(screenName = user.getScreenName()).equals(screenNameL)) {

            str.append(split).append("用户名更改 : @").append(screenNameL).append(" ------> @").append(screenName);

            change = true;

        }

        String bioL = bio;

        if (!ObjectUtil.equal(bio = user.getDescription(),bioL)) {

            str.append(split).append("简介更改 : \n\n").append(bioL).append(" \n\n ------> \n\n").append(bio);

            change = true;

        }

        String photoL = photoUrl;

        if (!ObjectUtil.equal(photoUrl = user.getBiggerProfileImageURL(),photoL)) {

            str.append(split).append("头像更改 : " + Html.a("媒体文件",photoL) + " ------> " + Html.a("媒体文件",photoUrl));

            change = true;

        }

        Boolean protectL = isProtected;

        if (protectL != (isProtected = user.isProtected())) {

            str.append(split).append("保护状态更改 : ").append(isProtected ? "开启了锁推" : "关闭了锁推");

            change = true;

        }

        isDisappeared = false;

        if (createdAt == null) {

            createdAt = user.getCreatedAt().getTime();

            change = false;

        }

        if (change) {

            UTTask.onUserChange(this,str.toString());

        }
        
        return change;

    }

    public String urlHtml() {

        return Html.a(name,url());

    }

    public String url() {

        return "https://twitter.com/" + screenName;

    }


}
