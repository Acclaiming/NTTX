package io.kurumi.ntt.funcs.twitter.track;

import io.kurumi.ntt.db.*;
import io.kurumi.ntt.funcs.abs.*;
import io.kurumi.ntt.model.*;
import io.kurumi.ntt.model.request.*;
import io.kurumi.ntt.twitter.*;
import io.kurumi.ntt.utils.*;
import java.util.*;

public class TrackUI extends TwitterFunction {

    public static TrackUI INSTANCE = new TrackUI();

    public static Data<TrackSetting> data = new Data<TrackSetting>(TrackSetting.class);

    public static class TrackSetting {

        public long id;

        public boolean followers = false;
        public boolean followingInfo = false;
        public boolean followersInfo = false;

        public boolean hideChange = false;
        
    }

    @Override
    public void functions(LinkedList<String> names) {

        names.add("track");

    }

    final String POINT_SETTING_FOLLOWERS = "tr|f";
    final String POINT_SETTING_FOLLOWERS_INFO = "tr|o";
    final String POINT_SETTING_FOLLOWINGS_INFO = "tr|r";
    final String POINT_SETTING_HIDE_ME = "tr|h";
    
    @Override
    public void points(LinkedList<String> points) {

        super.points(points);

        points.add(POINT_SETTING_FOLLOWERS);
        points.add(POINT_SETTING_FOLLOWINGS_INFO);
        points.add(POINT_SETTING_FOLLOWERS_INFO);
        points.add(POINT_SETTING_HIDE_ME);

    }

    @Override
    public void onFunction(UserData user,Msg msg,String function,String[] params,final TAuth account) {

        final TrackSetting setting = this.data.containsId(account.id) ? this.data.getById(account.id) : new TrackSetting();

        msg.send("一个监听的设置... (做成按钮UI了 (❁´▽`❁)").buttons(makeSettings(setting,account.id)).exec();

    }

    ButtonMarkup makeSettings(final TrackSetting setting,final long accountId) {

        return new ButtonMarkup() {{

                newButtonLine((setting.followers ? "「 关闭" : "「 开启") + " 关注者监听 」",POINT_SETTING_FOLLOWERS,accountId);
                newButtonLine((setting.followingInfo ? "「 关闭" : "「 开启") + " 账号更改监听 (关注中) 」",POINT_SETTING_FOLLOWINGS_INFO,accountId);
                newButtonLine((setting.followersInfo ? "「 关闭" : "「 开启") + " 账号更改监听 (关注者) 」",POINT_SETTING_FOLLOWERS_INFO,accountId);
                newButtonLine((setting.hideChange ? "「 显示" : "「 隐藏") + " 账号更改 (对其他用户) 」",POINT_SETTING_HIDE_ME,accountId);
                
                
            }};

    }

    @Override
    public void onCallback(UserData user,Callback callback,String point,String[] params) {

        long accountId = Long.parseLong(params[0]);

        final TrackSetting setting = this.data.containsId(accountId) ? this.data.getById(accountId) : new TrackSetting();
        
        setting.id = accountId;

		boolean target = true;
		
        switch (point) {

                case POINT_SETTING_FOLLOWERS : target = setting.followers = !setting.followers;break;
                case POINT_SETTING_FOLLOWINGS_INFO : target = setting.followingInfo = !setting.followingInfo;break;
                case POINT_SETTING_FOLLOWERS_INFO : target = setting.followersInfo = !setting.followersInfo;break;
                case POINT_SETTING_HIDE_ME : target = setting.hideChange = !setting.hideChange;break;
                
        }

        if (setting.followers || setting.followingInfo || setting.followersInfo) {

            this.data.setById(accountId,setting);

        } else {

            this.data.deleteById(accountId);

        }

        callback.text("已" + (target ? "开启" : "关闭") + " ~");
        callback.editMarkup(makeSettings(setting,accountId));


    }

}
